package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.ClientStateEntiy;
import com.artemisia_corp.artemisia.entity.enums.CompanyStateEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Comment("Tabla para almacenar los clientes")
@Entity
@Table(name = "client",  indexes = {@Index(name = "idx_client_id", columnList = "client_id")})
public class Client {
    @Comment("Identificador del registros")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WORKER_ID_GENERATOR")
    @SequenceGenerator(name = "CLIENT_ID_GENERATOR", sequenceName = "SEQ_CLIENT_ID", allocationSize = 1, initialValue = 1)
    @Column(name = "client_id")
    private Long id;

    @Comment("Campo para almacenar el nombre del cliente")
    @Column(name = "complete_name", length = 150, nullable = false)
    private String complete_name;

    @Comment("Campo para almacenar el nit del cliente")
    @Column(name = "nit", nullable = true)
    private int nit;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private ClientStateEntiy state;


}
