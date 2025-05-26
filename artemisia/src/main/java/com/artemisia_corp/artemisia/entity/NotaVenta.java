package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "nota_venta")
public class NotaVenta {
    @Comment("Identificador de la nota de venta")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTA_VENTA_ID_GENERATOR")
    @SequenceGenerator(name = "NOTA_VENTA_ID_GENERATOR", sequenceName = "seq_nota_venta_id", allocationSize = 1)
    private Long id;

    @Comment("Usuario que esta realizando la compra")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User buyer;

    @Comment("Direccion a la cual se debe enviar el producto")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_address", nullable = false)
    private Address buyerAddress;

    @Comment("Estado actual de la venta, en carrito o completada")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_venta", nullable = false)
    private VentaEstado estadoVenta;

    @Comment("Total de la compra")
    @Column(name = "total_global", nullable = false)
    private Double totalGlobal;

    @Comment("Fecha en la que se creo la compra")
    @Column(nullable = false)
    private LocalDateTime date;
}
