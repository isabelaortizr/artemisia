package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.WorkerStateEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Comment("Tabla para almacenar los trabajadores de una empresa")
@Entity
@Table(name = "worker",  indexes = {@Index(name = "idx_worker_id", columnList = "worker_id")})
public class Worker {
    @Comment("Identificador del registros")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WORKER_ID_GENERATOR")
    @SequenceGenerator(name = "WORKER_ID_GENERATOR", sequenceName = "SEQ_WORKER_ID", allocationSize = 1, initialValue = 1)
    @Column(name = "worker_id")
    private Long id;

    @Comment("Campo para almacenar el nombre del producto")
    @Column(name = "name", length = 60, nullable = false)
    private String name;

    @Comment("Cargo del trabajador, como programador, gerente, etc.")
    @Column(name = "occupation", length = 100, nullable = false)
    private String occupation;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private WorkerStateEntity state;

    @Comment("Campo para almacenar el salario del trabajador")
    @Column(name = "salary", precision = 3)
    private double salary;

    @JsonIgnore
    @Comment("Identificador de la empresa a la que pertence")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private Company company;


}
