package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.UserRegistorEntity;
import com.example.demo.service.UserRegistorEntityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api")
public class UserController {

    @Autowired
    UserRegistorEntityService userRegistorEntityService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping
    public String getAllUsers() {
        return "Hello World";
    }

    @GetMapping("find")
    public String findUse(@RequestParam String param) {
        return new String("Hello World");
    }

    @PostMapping("/user-register")
    public ResponseEntity<String> registor(@RequestBody UserRegistorEntity userRegistorDetails) {
        userRegistorDetails.setPassword(passwordEncoder.encode(userRegistorDetails.getPassword()));
        userRegistorEntityService.save(userRegistorDetails);
        return ResponseEntity.ok("User Registor successfully");
    }

    @GetMapping("/users")
    public String getUserDetailString() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Fetched user Detail Successfully";
    }

    @PostMapping("/generate-token")
    public String postMethodName(@RequestBody String entity) {
        return "User Registor successfully";
    }
    

}
