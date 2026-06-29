package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserRegistorEntity;
import com.example.demo.repository.UserRegistorRepository;

@Service
public class UserRegistorEntityService implements UserDetailsService {

    private final UserRegistorRepository userRegistorRepository;

    @Autowired
    UserRegistorEntityService(UserRegistorRepository userRegistorRepository) {
        this.userRegistorRepository = userRegistorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRegistorRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    public UserRegistorEntity save(UserRegistorEntity userRegistorEntity) {
        return userRegistorRepository.save(userRegistorEntity);
    }
}
