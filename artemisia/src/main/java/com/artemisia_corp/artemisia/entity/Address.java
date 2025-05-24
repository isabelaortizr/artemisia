package com.artemisia_corp.artemisia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {
    @Comment("Identificador de la direccion")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADDRESS_ID_GENERATOR")
    @SequenceGenerator(name = "ADDRESS_ID_GENERATOR", sequenceName = "seq_address_id", allocationSize = 1)
    private Long addressId;

    @Comment("Direccion dada")
    @Column(length = 250, nullable = false)
    private String direction;

    @Comment("Usuario al que pertenece la direccion")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

}
