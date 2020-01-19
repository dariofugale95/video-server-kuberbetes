package com.castagnolofugale.videomanagementservice;

import com.castagnolofugale.videomanagementservice.model.User;
import com.castagnolofugale.videomanagementservice.repository.ReactiveUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VMSUserDetailsService implements UserDetailsService {

    @Autowired
    ReactiveUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username).block();

        if (user == null)
            throw new UsernameNotFoundException("Utente non trovato");

        return new org.springframework.security.core.userdetails.User(
                username,
                user.getPassword(),
                true,
                true,
                true,
                true,
                getAuth(user.getRoles())
        );

    }

    @Bean
    private BCryptPasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }

    private List<GrantedAuthority> getAuth(List<String> roles){
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (final String role : roles)
            authorities.add(new SimpleGrantedAuthority(role));

        return authorities;
    }
}


