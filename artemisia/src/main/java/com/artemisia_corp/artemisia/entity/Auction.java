package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.AuctionStatus;
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
@Table(name = "auction")
public class Auction extends AuditableEntity {

    @Comment("Identificador de la subasta")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AUCTION_ID_GENERATOR")
    @SequenceGenerator(name = "AUCTION_ID_GENERATOR", sequenceName = "seq_auction_id", allocationSize = 1)
    private Long id;

    @Comment("Obra que se está subastando")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Comment("Usuario que creó la subasta (vendedor de la obra)")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Comment("Usuario ganador de la subasta")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id", nullable = true)
    private User winner;

    @Comment("Nota de venta generada al finalizar la subasta con ganador")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_venta_id", nullable = true)
    private NotaVenta notaVenta;

    @Comment("Estado actual de la subasta")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuctionStatus status;

    @Comment("Fecha y hora de inicio de la subasta")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Comment("Fecha y hora de finalización de la subasta")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Comment("Monto inicial establecido por el vendedor")
    @Column(name = "starting_price", nullable = false)
    private Double startingPrice;

    @Comment("Monto actual (última puja más alta) o final de la subasta")
    @Column(name = "current_price", nullable = false)
    private Double currentPrice;
}
