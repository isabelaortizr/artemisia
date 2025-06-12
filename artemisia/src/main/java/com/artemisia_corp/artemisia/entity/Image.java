
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

    @Comment("Nombre del archivo")
    @Column(nullable = false)
    private String fileName;

    @Comment("Contenido de la imagen codificado en Base64")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String base64Data;

    @Comment("Vendedor que subió la imagen")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product_id;
}