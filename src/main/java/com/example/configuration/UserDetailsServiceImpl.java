package com.example.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.services.UserService;

import java.util.List;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {


    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
//        System.out.println("in userDetailsService");
//        System.out.println("user(" + login + ") from db: " + userService.getUserByLogin(login));
        com.example.entities.User user = userService.getUserByLogin(login);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        System.out.println("encoded userFromDB pawws: " + user.getPassword());
        List<GrantedAuthority> authority = List.of(new SimpleGrantedAuthority(user.getRole().name()));

        return new User(user.getLogin(), user.getPassword(), true, true, true, true, authority);
    }

}

