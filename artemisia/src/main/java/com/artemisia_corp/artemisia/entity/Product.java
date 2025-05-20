package com.artemisia_corp.artemisia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Comment("Tabla para almacenar los productos de una empresa")
@Entity
@Table(name = "product",  indexes = {@Index(name = "idx_company_id", columnList = "company_id")})
public class Product {
    @Comment("Identificador del registros")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_ID_GENERATOR")
    @SequenceGenerator(name = "PRODUCT_ID_GENERATOR", sequenceName = "SEQ_PRODUCT_ID", allocationSize = 1, initialValue = 1)
    @Column(name = "id")
    private Long id;

    @Comment("Campo para almacenar el nombre del producto")
    @Column(name = "name", length = 60, nullable = false)
    private String name;

    @Comment("Campo para almacenar la descripción del producto")
    @Column(name = "description", length = 255)
    private String description;

    @Comment("Campo para almacenar el precio del producto")
    @Column(name = "price", precision = 20, scale = 10)
    private BigDecimal price;

    @Comment("Campo para almacenar la información del stock")
    @Column(name = "stock")
    private int stock;

    @JsonIgnore
    @Comment("Identificador de la empresa a la que pertence")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private Company company;

}
