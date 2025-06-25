package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.AddressStatus;
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
public class Address extends AuditableEntity {
    @Comment("Identificador de la dirección")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADDRESS_ID_GENERATOR")
    @SequenceGenerator(name = "ADDRESS_ID_GENERATOR", sequenceName = "seq_address_id", allocationSize = 1)
    private Long id;

    @Comment("Nombre del destinatario")
    @Column(length = 150, nullable = false)
    private String recipientName;

    @Comment("Apellido del destinatario")
    @Column(length = 150, nullable = false)
    private String recipientSurname;

    @Comment("País de la dirección")
    @Column(length = 100, nullable = false)
    private String country;

    @Comment("Ciudad de la dirección")
    @Column(length = 100, nullable = false)
    private String city;

    @Comment("Calle")
    @Column(length = 200, nullable = false)
    private String street;

    @Comment("Número de casa o edificio")
    @Column(length = 50, nullable = false)
    private String houseNumber;

    @Comment("Estado actual de la dirección")
    @Enumerated(EnumType.STRING) // Agrega esta anotación aquí
    @Column(length = 50, nullable = false)
    private AddressStatus status;

    @Comment("Datos extra opcionales")
    @Column(length = 250)
    private String extra;

    @Comment("Usuario al que pertenece la dirección")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}