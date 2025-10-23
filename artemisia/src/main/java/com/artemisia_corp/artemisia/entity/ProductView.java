package com.artemisia_corp.artemisia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_VIEW_ID_GENERATOR")
    @SequenceGenerator(name = "PRODUCT_VIEW_ID_GENERATOR", sequenceName = "seq_product_view_id", allocationSize = 1)
    private Long id;

    @Comment("Usuario que vio el producto")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Comment("Producto que fue visto")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Comment("Número de veces que el producto fue visto")
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 1;

    @Comment("Duración total de visualización en segundos")
    @Column(name = "total_view_duration")
    private Integer totalViewDuration = 0;

    @Comment("Última vez que el producto fue visto")
    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    @Comment("Primera vez que el producto fue visto")
    @Column(name = "first_viewed_at", nullable = false)
    private LocalDateTime firstViewedAt;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.firstViewedAt = now;
        this.lastViewedAt = now;
        if (this.createdDate == null) {
            this.createdDate = now;
        }
        this.lastModifiedDate = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
        this.lastViewedAt = LocalDateTime.now();
    }

    public void addViewDuration(Integer durationInSeconds) {
        if (durationInSeconds != null && durationInSeconds > 0) {
            this.totalViewDuration += durationInSeconds;
        }
    }

    @Override
    public String toString() {
        return "ProductView{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", productId=" + (product != null ? product.getId() : null) +
                ", viewCount=" + viewCount +
                ", totalViewDuration=" + totalViewDuration +
                ", lastViewedAt=" + lastViewedAt +
                '}';
    }
}
