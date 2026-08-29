package com.myvitrine.api.security;

import com.myvitrine.api.model.User;
import com.myvitrine.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestrutura de seguranca (nao faz parte da camada de
 * dominio "service"). Carrega o usuario pelo e-mail para o fluxo de
 * autenticacao do Spring Security (DaoAuthenticationProvider).
 */
@Component
public class MyVitrineUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyVitrineUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + email));
        return new UserPrincipal(user);
    }
}
