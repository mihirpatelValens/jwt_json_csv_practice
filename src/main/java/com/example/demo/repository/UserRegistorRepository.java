package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.UserRegistorEntity;

@Repository
public interface UserRegistorRepository extends JpaRepository<UserRegistorEntity, Long> {

    Optional<UserRegistorEntity> findByUsername(String username);

}
