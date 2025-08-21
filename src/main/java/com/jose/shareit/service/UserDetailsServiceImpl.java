package com.jose.shareit.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jose.shareit.model.User; 
import com.jose.shareit.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email) // use email as the username for authentication
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole()); // create a GrantedAuthority from the user's role (e.g., "ROLE_ADMIN")

        return new org.springframework.security.core.userdetails.User(  // Return a Spring Security User object that the security framework can understand
            user.getEmail(),
            user.getPassword(),
            Collections.singleton(authority)
        );
    }
}