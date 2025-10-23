package com.artemisia_corp.artemisia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection
    @CollectionTable(name = "user_preference_vectors",
            joinColumns = @JoinColumn(name = "user_preference_id"))
    @MapKeyColumn(name = "feature")
    @Column(name = "weight")
    private Map<String, Double> preferenceVector = new HashMap<>();

    @Comment("Última actualización del vector")
    @Column(name = "last_updated")
    private java.time.LocalDateTime lastUpdated;

    @Comment("Cluster asignado al usuario")
    @Column(name = "user_cluster")
    private Integer userCluster;

    @PreUpdate
    @PrePersist
    public void updateTimestamp() {
        this.lastUpdated = java.time.LocalDateTime.now();
    }
}
