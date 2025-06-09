package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "product")
public class Product extends AuditableEntity {
    @Comment("Identificador del producto")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_ID_GENERATOR")
    @SequenceGenerator(name = "PRODUCT_ID_GENERATOR", sequenceName = "seq_product_id", allocationSize = 1)
    private Long productId;

    @Comment("Usuario que puso el producto a la venta")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller", nullable = false)
    private User seller;

    @Comment("Nombre del producto a la venta")
    @Column(length = 150, nullable = false)
    private String name;

    @Comment("Tecnica utilizada para la pintura")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaintingTechnique technique;

    @Comment("Materiales usados para el producto")
    @Column(length = 70, nullable = false)
    private String materials;

    @Comment("Descripcion base del producto")
    @Column(length = 250)
    private String description;

    @Comment("Precio de la producto")
    @Column(nullable = false)
    private Double price;

    @Comment("Cantidad disponible del producto")
    @Column(nullable = false)
    private Integer stock;

    @Comment("Estado actula del producto")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Comment("Direccion de la imagen")
    @Column(length = 250, nullable = false)
    private String image;

    @Comment("Categoria del producto")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaintingCategory category;
}
