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
        log.info("Starting currency conversion for user: {}, target currency: {}",
                conversionDto.getUserId(), conversionDto.getTargetCurrency());

        if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 1L)) {
            log.info("Token is null or expired, obtaining new authentication token");
            obtenerTokenAutenticacion();
        }

        boolean isBob = false;
        NotaVentaResponseDto cartDto = notaVentaService.getActiveCartByUserId(conversionDto.getUserId());
        NotaVenta cart = notaVentaRepository.findById(cartDto.getId())
                .orElseThrow(() -> {
                    log.error("Cart not found with ID: {}", cartDto.getId());
                    logsService.error("Error al obtener la carta de nota venta");
                    return new NotDataFoundException("Cart not found");
                });
        List<OrderDetailResponseDto> orderDetails = orderDetailService.getOrderDetailsByNotaVenta(cart.getId());

        CurrencyConversionResponseDto conversion;
        if (conversionDto.getTargetCurrency().contains("BOB")) {
            isBob = true;
            log.info("Converting to BOB. Setting up conversion from BOB to {}", conversionDto.getOriginCurrency());
            conversion = convertAmount(
                    new CurrencyConversionDto(
                            "BOB",
                            conversionDto.getOriginCurrency(),
                            1.0
                    ));
        } else {
            log.info("Converting from {} to {} with amount: {}",
                    conversionDto.getOriginCurrency(), conversionDto.getTargetCurrency(), cart.getTotalGlobal());
            conversion = convertAmount(
                    new CurrencyConversionDto(
                            conversionDto.getOriginCurrency(),
                            conversionDto.getTargetCurrency(),
                            cart.getTotalGlobal()
                    ));
        }

        log.info("Currency conversion completed. Exchange rate: {}", conversion.getExchangeRate());

        cart.setMonedaCarrito(conversionDto.getTargetCurrency());
        cart.setTasaCambio(conversion.getExchangeRate());
        cart.setPreciosConvertidos(true);

        log.debug("Starting order detail conversion for {} items", orderDetails.size());
        int processedItems = 0;
        for (OrderDetailResponseDto detailDto : orderDetails) {
            log.debug("Processing order detail ID: {}", detailDto.getId());
            OrderDetail detail = orderDetailRepository.findById(detailDto.getId())
                    .orElseThrow(() -> {
                        log.error("Order detail not found with ID: {}", detailDto.getId());
                        logsService.error("Error al obtener la carta de nota venta");
                        return new NotDataFoundException("Order detail not found");
                    });

            double originalTotal = detail.getTotal();
            double convertedTotal;

            if (isBob) {
                convertedTotal = detailDto.getTotal() * conversion.getExchangeRate();
                log.debug("Converted order detail {} from {} to {} (BOB conversion)",
                        detailDto.getId(), originalTotal, convertedTotal);
            } else {
                convertedTotal = detailDto.getTotal() / conversion.getExchangeRate();
                log.debug("Converted order detail {} from {} to {} (other currency conversion)",
                        detailDto.getId(), originalTotal, convertedTotal);
            }

            detail.setTotal(convertedTotal);
            orderDetailRepository.save(detail);
            processedItems++;
            log.debug("Successfully updated order detail ID: {}", detailDto.getId());
        }
        log.info("Completed conversion for {} order details", processedItems);

        double originalCartTotal = cart.getTotalGlobal();
        if (isBob) {
            cart.setTotalGlobal(originalCartTotal * conversion.getExchangeRate());
            log.info("Updated cart total from {} to {} (BOB conversion)", originalCartTotal, cart.getTotalGlobal());
        } else {
            cart.setTotalGlobal(originalCartTotal / conversion.getExchangeRate());
            log.info("Updated cart total from {} to {} (other currency conversion)", originalCartTotal, cart.getTotalGlobal());
        }

        log.debug("Saving updated cart with ID: {}", cart.getId());
        notaVentaRepository.save(cart);
        log.info("Cart saved successfully");

        log.debug("Fetching final nota venta response for cart ID: {}", cart.getId());
        NotaVentaResponseDto response = notaVentaService.getNotaVentaById(cart.getId());
        log.info("Currency conversion completed successfully for user: {}", conversionDto.getUserId());

        return response;
    }

    private CurrencyConversionResponseDto convertAmount(CurrencyConversionDto conversionEntity) {
        log.debug("Starting currency conversion: {} to {}, amount: {}",
                conversionEntity.getSourceCurrency(), conversionEntity.getTargetCurrency(), conversionEntity.getAmount());

        RestClient restClient = create();
        ResponseEntity<CurrencyConversionResponseDto> response;

        try {
            String url = urlBase + String.format("/api/v1/currency/convert?country=%s&from=%s&to=%s&amount=%s",
                    conversionEntity.getCountry(), conversionEntity.getSourceCurrency(),
                    conversionEntity.getTargetCurrency(), conversionEntity.getAmount());

            log.debug("Making currency conversion API call to: {}", url);

            response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toEntity(CurrencyConversionResponseDto.class);

            log.info("Currency conversion API call successful. Response status: {}",
                    response.getStatusCode());
            log.debug("Conversion response body: {}", response.getBody());

            return response.getBody();
        } catch (Exception e) {
            log.error("Error converting amount from {} to {}: {}",
                    conversionEntity.getSourceCurrency(), conversionEntity.getTargetCurrency(), e.getMessage(), e);
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
