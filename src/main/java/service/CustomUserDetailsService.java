package service;

import Repository.UserRepository;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            System.out.println("CustomUserDetailsService - Loading user for email: " + email);
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                System.err.println("User not found with email: " + email);
                throw new UsernameNotFoundException("User not found with email: " + email);
            }
            
            // Create authorities based on user role
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
            System.out.println("User found with email: " + email + ", role: " + user.getRole().name());
            
            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(authority)
            );
        } catch (Exception e) {
            System.err.println("Error loading user by username: " + e.getMessage());
            e.printStackTrace();
            throw new UsernameNotFoundException("Error loading user: " + e.getMessage(), e);
        }
    }
} 