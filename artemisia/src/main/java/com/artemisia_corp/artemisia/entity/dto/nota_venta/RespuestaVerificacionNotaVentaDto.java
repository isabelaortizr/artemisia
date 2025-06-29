package com.artemisia_corp.artemisia.entity.dto.nota_venta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class RespuestaVerificacionNotaVentaDto {
    @JsonProperty("notification_type")
    private String notificationType;
    private String id;
    private TransaccionDto transaction;
    private Long timestamp;
}
