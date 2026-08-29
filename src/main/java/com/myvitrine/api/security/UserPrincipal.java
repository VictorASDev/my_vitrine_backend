package com.myvitrine.api.security;

import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Adapta a entidade de dominio {@link User} ao contrato UserDetails do
 * Spring Security, expondo tambem o id e o profileType (necessarios para
 * gerar as claims do JWT apos a autenticacao).
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public UUID getId() {
        return user.getId();
    }

    public ProfileType getProfileType() {
        return user.getProfileType();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getProfileType().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
