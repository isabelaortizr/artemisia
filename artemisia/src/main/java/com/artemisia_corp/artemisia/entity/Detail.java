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
@Table(name = "detail")
public class Detail {
    @Comment("Identificador del detalle")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DETAIL_ID_GENERATOR")
    @SequenceGenerator(name = "DETAIL_ID_GENERATOR", sequenceName = "seq_detail_id", allocationSize = 1)
    private Long id;

    @Comment("Agrupamiento de una compra bajo un mismo id para consegir el usuario de compra y venta, estado y fecha")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private NotaVenta group;

    @Comment("Producto que se esta comprando")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Comment("Usuario al que le estan comprando")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Users seller;

    @Comment("Nombre actual del producto que se esta comprando")
    @Column(name = "product_name", length = 250, nullable = false)
    private String productName;

    @Comment("Cantidad del producto que se esta comprando")
    @Column(nullable = false)
    private Integer quantity;

    @Comment("Total pagado por el producto")
    @Column(nullable = false)
    private Double total;

}
