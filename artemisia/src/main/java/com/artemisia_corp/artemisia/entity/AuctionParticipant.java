package com.artemisia_corp.artemisia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "auction_participant",
        uniqueConstraints = @UniqueConstraint(name = "uk_auction_participant", columnNames = {"auction_id", "participant_id"}))
public class AuctionParticipant extends AuditableEntity {

    @Comment("Identificador de la participación en la subasta")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AUCTION_PARTICIPANT_ID_GENERATOR")
    @SequenceGenerator(name = "AUCTION_PARTICIPANT_ID_GENERATOR", sequenceName = "seq_auction_participant_id", allocationSize = 1)
    private Long id;

    @Comment("Subasta en la que participa")
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Comment("Usuario comprador que participa pujando en la subasta")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @Comment("Último monto con el que pujó el participante")
    @Column(name = "bid_amount", nullable = false)
    private Double bidAmount;

    @Comment("Fecha y hora de la última puja realizada")
    @Column(name = "bid_date", nullable = false)
    private LocalDateTime bidDate;
}
