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
@Table(name = "alumni")
public class Alumni {
    @Comment("Identificador del registros")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ALUMNI_ID_GENERATOR")
    @SequenceGenerator(name = "ALUMNI_ID_GENERATOR", sequenceName = "SEQ_ALUMNI_ID", allocationSize = 1, initialValue = 1)
    @Column(name = "alumni_id")
    private Long id;

    @Comment("Campo para almacenar el nombre del alumno")
    @Column(name = "name", length = 150, nullable = false)
    private String complete_name;

    @Comment("Campo para almacenar el ci del alumno")
    @Column(name = "ci", nullable = true)
    private int ci;
}
