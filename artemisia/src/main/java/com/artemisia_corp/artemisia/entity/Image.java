
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
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("Nombre del archivo original")
    @Column(nullable = false)
    private String fileName;

    @Comment("URL segura de Cloudinary")
    @Column(nullable = false, length = 500)
    private String cloudinaryUrl;

    @Comment("Public ID en Cloudinary para poder eliminar la imagen")
    @Column(nullable = false, length = 300)
    private String publicId;

    @Comment("Producto al que pertenece la imagen")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product", nullable = false)
    private Product product;
}