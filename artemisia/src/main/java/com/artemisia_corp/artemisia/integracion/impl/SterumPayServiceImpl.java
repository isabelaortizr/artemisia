package com.artemisia_corp.artemisia.integracion.impl;

import com.artemisia_corp.artemisia.crypto.CryptoRSA;
import com.artemisia_corp.artemisia.exception.OperationException;
import com.artemisia_corp.artemisia.integracion.SterumPayService;
import com.artemisia_corp.artemisia.integracion.impl.dtos.*;
import com.artemisia_corp.artemisia.service.NotaVentaService;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class SterumPayServiceImpl implements SterumPayService {
    private String jwtToken = null;

    @Value("${stereum-pay.url-base}")
    private String urlBase;
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

        chargeDto.setIdempotencyKey(this.getUUID());
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
    public EstadoResponseDto obtenerEstadoCobro(String id_transaccion) {
        if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 1L)) obtenerTokenAutenticacion();
        RestClient restClient = create();
        ResponseEntity<EstadoResponseDto> response;

        try {
            response = restClient.get()
                    .uri(urlBase + String.format("/api/v1/transactions/%s/verify", id_transaccion))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toEntity(EstadoResponseDto.class);
        } catch (Exception e) {
            log.error("Error converting to BOB", e);
            throw new OperationException("Failed to convert currency to BOB");
        }

        return response.getBody();
    }

    @Override
    public CurrencyConversionResponseDto conversionBob (CurrencyConversionDto conversionEntity) {
        if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 1L)) obtenerTokenAutenticacion();
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
        } catch (Exception e) {
            log.error("Error converting to BOB", e);
            throw new OperationException("Failed to convert currency to BOB");
        }

        return response.getBody();
    }

    @Override
    public EstadoResponseDto cancelarCargo(String id_transaccion) {
        if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 1L)) obtenerTokenAutenticacion();
        RestClient restClient = create();
        ResponseEntity<EstadoResponseDto> response;

        try {
            response = restClient.post()
                    .uri(urlBase + String.format("/api/v1/transactions/%s/cancel", id_transaccion))
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
