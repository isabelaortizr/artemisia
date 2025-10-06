package com.artemisia_corp.artemisia.integracion.impl;

import com.artemisia_corp.artemisia.integracion.impl.dtos.CurrencyConversionDto;
import com.artemisia_corp.artemisia.integracion.impl.dtos.CurrencyConversionResponseDto;
import com.artemisia_corp.artemisia.utils.JWTUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ExchangeRateManager {

    private static ExchangeRateManager instance;

    private final RestClient restClient;
    private final String urlBase;
    private String jwtToken;

    // Cache para tasas específicas que SÍ funcionan
    private final Map<String, Double> rateCache = new ConcurrentHashMap<>();

    @Autowired
    private SterumPayServiceImpl stereumPayService;

    public ExchangeRateManager(
            @Value("${stereum-pay.url-base}") String urlBase,
            @Value("${stereum-pay.connect-timeout:5000}") int connectTimeout,
            @Value("${stereum-pay.read-timeout:30000}") int readTimeout
    ) {
        this.urlBase = urlBase;

        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        clientHttpRequestFactory.setReadTimeout(Duration.ofMillis(readTimeout));

        this.restClient = RestClient.builder()
                .requestFactory(clientHttpRequestFactory)
                .build();

        instance = this;
    }

    public static ExchangeRateManager getInstance() {
        return instance;
    }

    @PostConstruct
    public void init() {
        log.info("Inicializando ExchangeRateManager...");
        // No forzar actualización al inicio, se hará bajo demanda
    }

    /** 🔄 Se ejecuta cada 1 hora para limpiar cache */
    @Scheduled(fixedRate = 3600000)
    public void clearCache() {
        log.info("Limpiando cache de tasas de cambio...");
        rateCache.clear();
    }

    /**
     * Método principal para conversiones - replica la lógica que funcionaba
     */
    public CurrencyConversionResponseDto convertCurrency(CurrencyConversionDto conversionDto) {
        try {
            // Verificar y obtener token si es necesario
            if (jwtToken == null || JWTUtils.isTokenExpired(jwtToken, null, 300000L)) {
                obtenerTokenAutenticacion();
            }

            String cacheKey = generateCacheKey(conversionDto.getSourceCurrency(), conversionDto.getTargetCurrency());

            // Verificar si ya tenemos la tasa en cache
            if (rateCache.containsKey(cacheKey)) {
                Double cachedRate = rateCache.get(cacheKey);
                return createConversionResponse(conversionDto, cachedRate);
            }

            // Si no está en cache, consultar a la API
            ResponseEntity<CurrencyConversionResponseDto> response = restClient.get()
                    .uri(urlBase + String.format("/api/v1/currency/convert?country=%s&from=%s&to=%s&amount=%s",
                            conversionDto.getCountry(), conversionDto.getSourceCurrency(),
                            conversionDto.getTargetCurrency(), conversionDto.getAmount()))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toEntity(CurrencyConversionResponseDto.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CurrencyConversionResponseDto apiResponse = response.getBody();
                Double rate = apiResponse.getExchange_rate();

                if (rate != null && rate > 0) {
                    // Guardar en cache para futuras consultas
                    rateCache.put(cacheKey, rate);
                    log.debug("Tasa obtenida de API: {}→{} = {}",
                            conversionDto.getSourceCurrency(), conversionDto.getTargetCurrency(), rate);

                    return apiResponse;
                } else {
                    throw new RuntimeException("Tasa de cambio inválida recibida de la API");
                }
            } else {
                log.error("Error en API para {}→{}: {}",
                        conversionDto.getSourceCurrency(), conversionDto.getTargetCurrency(),
                        response.getStatusCode());
                throw new RuntimeException("Error en respuesta de la API");
            }

        } catch (Exception e) {
            log.error("Error al convertir moneda {}→{}",
                    conversionDto.getSourceCurrency(), conversionDto.getTargetCurrency(), e);
            throw new RuntimeException("Error al convertir moneda: " + e.getMessage());
        }
    }

    /**
     * Crea una respuesta de conversión basada en una tasa específica
     */
    private CurrencyConversionResponseDto createConversionResponse(CurrencyConversionDto conversionDto, Double rate) {
        CurrencyConversionResponseDto response = new CurrencyConversionResponseDto();
        response.setCountry(conversionDto.getCountry());
        response.setSourceCurrency(conversionDto.getSourceCurrency());
        response.setTargetCurrency(conversionDto.getTargetCurrency());
        response.setAmount(conversionDto.getAmount());
        response.setExchange_rate(rate);
        response.setConverted_amount(conversionDto.getAmount() * rate);
        return response;
    }

    private void obtenerTokenAutenticacion() {
        try {
            this.jwtToken = stereumPayService.obtenerTokenAutenticacion().getAccessToken();
            log.info("Token actualizado para ExchangeRateManager");
        } catch (Exception e) {
            log.error("Error al obtener token para ExchangeRateManager", e);
            throw new RuntimeException("No se pudo obtener token de autenticación");
        }
    }

    /**
     * Genera clave única para el cache
     */
    private String generateCacheKey(String fromCurrency, String toCurrency) {
        return fromCurrency.toUpperCase() + "_TO_" + toCurrency.toUpperCase();
    }

    /**
     * Fuerza la actualización de una tasa específica
     */
    public void refreshRate(String fromCurrency, String toCurrency) {
        String cacheKey = generateCacheKey(fromCurrency, toCurrency);
        rateCache.remove(cacheKey);
        log.info("Tasa {}→{} removida del cache", fromCurrency, toCurrency);
    }

    public boolean isRateCached(String fromCurrency, String toCurrency) {
        return rateCache.containsKey(generateCacheKey(fromCurrency, toCurrency));
    }

    public String getCacheStatus() {
        return String.format("Tasas en cache: %d", rateCache.size());
    }
}