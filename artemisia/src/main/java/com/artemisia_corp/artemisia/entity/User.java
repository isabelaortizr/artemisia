package com.artemisia_corp.artemisia.entity;

import com.artemisia_corp.artemisia.entity.enums.StateEntity;
import com.artemisia_corp.artemisia.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableEntity implements UserDetails {
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

    @Comment("Estado del usuario")
    @Column(name = "status", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private StateEntity status;


    @Comment("Rol del usuaio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.status == StateEntity.ACTIVE;
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
