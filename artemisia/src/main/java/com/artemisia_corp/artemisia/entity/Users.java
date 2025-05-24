package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

    @Comment("Identificador del usuario")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_ID_GENERATOR")
    @SequenceGenerator(name = "USER_ID_GENERATOR", sequenceName = "seq_user_id", allocationSize = 1)
    private Long id;

    @Comment("Nombre del usuario")
    @Column(length = 100, nullable = false)
    private String name;

    @Comment("Correo del usuario")
    @Column(length = 255, nullable = false, unique = true)
    private String mail;

    @Comment("Contraseña del usuario")
    @Column(length = 250, nullable = false)
    private String password;

    @Comment("Rol del usuaio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
