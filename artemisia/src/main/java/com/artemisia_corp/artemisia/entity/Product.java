package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "product")
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_ID_GENERATOR")
    @SequenceGenerator(name = "PRODUCT_ID_GENERATOR", sequenceName = "seq_product_id", allocationSize = 1)
    @Comment("Identificador del producto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller", nullable = false)
    @Comment("Usuario que puso el producto a la venta")
    private User seller;

    @Column(length = 150, nullable = false)
    @Comment("Nombre del producto a la venta")
    private String name;

    @Column(length = 70, nullable = false)
    @Comment("Materiales usados para el producto")
    private String materials;

    @Column(length = 250)
    @Comment("Descripcion base del producto")
    private String description;

    @Column(nullable = false)
    @Comment("Precio del producto")
    private Double price;

    @Column(nullable = false)
    @Comment("Cantidad disponible del producto")
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("Estado actual del producto")
    private ProductStatus status;

    @Column(name = "image_url", length = 250)
    @Comment("Dirección de la imagen")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_techniques", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "technique")
    @Comment("Técnicas utilizadas en la obra")
    private Set<PaintingTechnique> techniques;

    @ElementCollection
    @CollectionTable(name = "product_categories", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    @Comment("Categorías del producto")
    private Set<PaintingCategory> categories;
}