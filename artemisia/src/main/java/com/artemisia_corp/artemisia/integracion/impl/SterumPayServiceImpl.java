package com.artemisia_corp.artemisia.integracion.impl;

import com.artemisia_corp.artemisia.crypto.CryptoRSA;
import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.OrderDetail;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.exception.OperationException;
import com.artemisia_corp.artemisia.integracion.SterumPayService;
import com.artemisia_corp.artemisia.integracion.impl.dtos.*;
import com.artemisia_corp.artemisia.repository.NotaVentaRepository;
import com.artemisia_corp.artemisia.repository.OrderDetailRepository;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import com.artemisia_corp.artemisia.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class SterumPayServiceImpl implements SterumPayService {
    private String jwtToken = null;

    @Value("${stereum-pay.url-base}")
    private String urlBase;
    @Value("${artemisia.redirect-url}")
    private String myUrlBase;
    @Value("${stereum-pay.username}")
    private String username;
    @Value("${stereum-pay.api-key}")
    private String apiKey;
    @Value("${stereum-pay.clave-integracion-usuario}")
    private String claveIntegracionUsuario;
    @Value("${stereum-pay.connect-timeout}")
    private int connectTimeout;
    @Value("${stereum-pay.read-timeout}")
    private int readTimeout;

    @Autowired
    @Lazy
    private CryptoRSA cryptoRSA;
    @Autowired
    @Lazy
    private NotaVentaService notaVentaService;
    @Autowired
    @Lazy
    private OrderDetailService orderDetailService;
    @Autowired
    @Lazy
    private NotaVentaRepository notaVentaRepository;
    @Autowired
    @Lazy
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private LogsService logsService;

    @Override
    public StereumAuthResponse obtenerTokenAutenticacion() {
        log.info("Inicio login sterum");
        RestClient restClient = create();
        ResponseEntity<StereumAuthResponse> response;
        StereumAuthRequest dto = new StereumAuthRequest();
        try {
            log.info("Nombre usuario: {}", username);
            dto.setUsername(username);
            dto.setPassword(cryptoRSA.rsaEncryptionOaepSha256(claveIntegracionUsuario));

            response = restClient.post()
                    .uri(urlBase + "/api/v1/auth/token")
                    .header("Authorization", "Basic " + apiKey)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .body(dto)
                    .retrieve()
                    .toEntity(StereumAuthResponse.class);
        } catch (Exception e) {
            log.error("Error al obtener la empresa artemisia", e);
            throw new OperationException("Error al obtener la empresa en artemisia");
        }

        if (response.getStatusCode().value() != 200) {
            log.error("Artemisia retorno error al recuperar la empresa: {}", response.getStatusCode().value());
            throw new OperationException("Artemisia retorno error al recuperar la empresa");
        }
        jwtToken = response.getBody().getAccessToken();

        return response.getBody();
    }

    @Override
    public StereumPagaResponseDto crearCargoCobro(StereumPagaDto chargeDto, Long userId) {
        if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 1L)) obtenerTokenAutenticacion();
        RestClient restClient = create();
        ResponseEntity<StereumPagaResponseDto> response;

        String uuid = getUUID();
        chargeDto.setIdempotencyKey(uuid);
        chargeDto.setCallback(this.urlBase);

        log.info("ChargeDto: {}", chargeDto);

        try {
            response = restClient.post()
                    .uri(urlBase + "/api/v1/transactions/create-charge")
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("x-api-key", apiKey)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .body(chargeDto)
                    .retrieve()
                    .toEntity(StereumPagaResponseDto.class);
        } catch (Exception e) {
            log.error("Error mientras se creaba el cobro", e);
            throw new OperationException("No se pudo crear el cobro");
        }

        StereumPagaResponseDto body = response.getBody();

        notaVentaService.ingresarIdTransaccion(body.getId(), notaVentaService.getActiveCartByUserId(userId).getId());

        return body;
    }

    @Override
    public EstadoResponseDto obtenerEstadoCobro(String idTransaccion) {
        if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 1L))
            obtenerTokenAutenticacion();
        RestClient restClient = create();
        ResponseEntity<EstadoResponseDto> response;

        try {
            response = restClient.get()
                    .uri(urlBase + String.format("/api/v1/transactions/%s/verify", idTransaccion))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toEntity(EstadoResponseDto.class);
        } catch (Exception e) {
            log.error("Error mientras se consultaba el estado del cobro", e);
            throw new OperationException("Error consulting transaction status.");
        }

        return response.getBody();
    }

    @Override
    public NotaVentaResponseDto conversionBob(ConversionDto conversionDto) {
        if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 1L))
            obtenerTokenAutenticacion();

        boolean isBob = false;
        NotaVentaResponseDto cartDto = notaVentaService.getActiveCartByUserId(conversionDto.getUserId());
        NotaVenta cart = notaVentaRepository.findById(cartDto.getId())
                .orElseThrow(() -> {
                    log.error("Error al obtener la carta de nota venta");
                    logsService.error("Error al obtener la carta de nota venta");
                    return new NotDataFoundException("Cart not found");
                });
        List<OrderDetailResponseDto> orderDetails = orderDetailService.getOrderDetailsByNotaVenta(cart.getId());

        CurrencyConversionResponseDto conversion;
        if (conversionDto.getTargetCurrency().contains("BOB")) {
            isBob = true;
            conversion = convertAmount(
                    new CurrencyConversionDto(
                            "BOB",
                            conversionDto.getOriginCurrency(),
                            1.0
                    ));
        } else {
            conversion = convertAmount(
                    new CurrencyConversionDto(
                            conversionDto.getOriginCurrency(),
                            conversionDto.getTargetCurrency(),
                            cart.getTotalGlobal()
                    ));
        }

        for (OrderDetailResponseDto detailDto : orderDetails) {
            OrderDetail detail = orderDetailRepository.findById(detailDto.getId())
                    .orElseThrow(() -> {
                        log.error("Error al obtener la carta de nota venta");
                        logsService.error("Error al obtener la carta de nota venta");
                        return new NotDataFoundException("Order detail not found");
                    });

            double convertedTotal = detail.getTotal();
            if (isBob)
                convertedTotal = detailDto.getTotal() * conversion.getExchangeRate();
            else
                convertedTotal = detailDto.getTotal() / conversion.getExchangeRate();

            detail.setTotal(convertedTotal);
            orderDetailRepository.save(detail);
        }

        if (isBob)
            cart.setTotalGlobal(cart.getTotalGlobal() * conversion.getExchangeRate());
        else
            cart.setTotalGlobal(cart.getTotalGlobal() / conversion.getExchangeRate());
        notaVentaRepository.save(cart);

        return notaVentaService.getNotaVentaById(cart.getId());
    }

    private CurrencyConversionResponseDto convertAmount(CurrencyConversionDto conversionEntity) {
        RestClient restClient = create();
        ResponseEntity<CurrencyConversionResponseDto> response;

        try {
            response = restClient.get()
                    .uri(urlBase + String.format("/api/v1/currency/convert?country=%s&from=%s&to=%s&amount=%s",
                            conversionEntity.getCountry(), conversionEntity.getSourceCurrency(),
                            conversionEntity.getTargetCurrency(), conversionEntity.getAmount()))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toEntity(CurrencyConversionResponseDto.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error converting amount", e);
            throw new OperationException("Failed to convert currency");
        }
    }

    @Override
    public EstadoResponseDto cancelarCargo(String idTransaccion) {
        if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 1L)) obtenerTokenAutenticacion();
        RestClient restClient = create();
        ResponseEntity<EstadoResponseDto> response;

        try {
            response = restClient.post()
                    .uri(urlBase + String.format("/api/v1/transactions/%s/cancel", idTransaccion))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toEntity(EstadoResponseDto.class);
        } catch (Exception e) {
            log.error("Error canceling transaction", e);
            throw new OperationException("Failed to cancel transaction.");
        }

        return response.getBody();
    }

    private RestClient create() {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        clientHttpRequestFactory.setReadTimeout(Duration.ofMillis(readTimeout));
        return RestClient.builder().requestFactory(clientHttpRequestFactory).build();
    }

    private String getUUID() {
        StringBuilder uuidBuilder = new StringBuilder();
        while (uuidBuilder.length() < 50) {
            String uuid = UUID.randomUUID().toString().replace("-", ""); // 32 caracteres
            uuidBuilder.append(uuid);
        }
        return uuidBuilder.substring(0, 50);
    }
}
