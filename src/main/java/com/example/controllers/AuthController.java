package com.example.controllers;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.example.configuration.JwtUtils;
import com.example.dto.LoginRequestDTO;
import com.example.dto.ResponceDTO;
import com.example.entities.User;
import com.example.services.UserService;

import java.io.UnsupportedEncodingException;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@CrossOrigin("*")
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AlgoPackClient algoPackClient;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Validated LoginRequestDTO loginRequest) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        try {
            System.out.println("password: " + DigestUtils.sha256Hex("password"));
            System.out.println(loginRequest.getLogin() + " " + loginRequest.getPassword());
            System.out.println(DigestUtils.sha256Hex(loginRequest.getPassword()));
            User user = userService.getUserByLogin(loginRequest.getLogin());
            Authentication authentication = authenticateUser(loginRequest.getLogin(), DigestUtils.sha256Hex(loginRequest.getPassword()));
            System.out.println("authenticated " + user + " | " + user.getRole());
            String jwt = jwtUtils.generateJwtToken(loginRequest.getLogin(), user.getRole().name());
            System.out.println(jwt);
            return new ResponseEntity<>(new ResponceDTO(jwt, user), HttpStatus.ACCEPTED);
        } catch (Exception e) { 
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    public Authentication authenticateUser(String login, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, password));
        System.out.println("hello!");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println(23424);
        return authentication;
    }

    @GetMapping("/api")
    public ResponseEntity<?> getApiData() {
        return new ResponseEntity<>(algoPackClient.get("https://moex.com/iss/datashop/algopack/eq/tradestats"), HttpStatus.valueOf(200));
    }



}
