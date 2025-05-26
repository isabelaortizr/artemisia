package com.artemisia_corp.artemisia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "log")
public class Logs {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DETAIL_ID_GENERATOR")
    @SequenceGenerator(name = "DETAIL_ID_GENERATOR", sequenceName = "seq_detail_id", allocationSize = 1)
    private Long id;

    @Column(name = "level", length = 30, nullable = false)
    private String level;

    @Comment("Comentario dado para el log")
    @Column(name = "message", length = 2000, nullable = false)
    private String message;

    @Comment("Fecha en la que se realizo el log")
    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
}
