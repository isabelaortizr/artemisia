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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_ID_GENERATOR")
    @SequenceGenerator(name = "LOG_ID_GENERATOR", sequenceName = "seq_log_id", allocationSize = 1)
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


    /*
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_ID_GENERATOR")
    @SequenceGenerator(name = "LOG_ID_GENERATOR", sequenceName = "seq_log_id", allocationSize = 1)
    private Long id;

    @Comment("Si es nivel de info, warning o error")
    @Column(name = "level", length = 30, nullable = false)
    private String level;

    @Comment("Tabla en la que se trabaja")
    @Column(name = "table_name", length = 100, nullable = false)
    private String tableName;

    @Comment("Si es un insert, update, etc.")
    @Column(name = "operation_type", length = 20, nullable = false)
    private String operationType;

    @Comment("ID de lo que se esta insertando, editando u borrando")
    @Column(name = "id_value", length = 20, nullable = false)
    private String idValue;

    @Comment("Informacion que contenia previamente")
    @Column(name = "old_info", length = 2000, nullable = false)
    private String oldInfo;

    @Comment("Infrmacion nueva")
    @Column(name = "new_info", length = 2000, nullable = false)
    private String newInfo;
     */
}
