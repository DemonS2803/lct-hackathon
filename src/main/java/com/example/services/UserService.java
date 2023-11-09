package com.example.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entities.User;
import com.example.enums.UserRole;
import com.example.repositories.UserRepository;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public ArrayList<User> findAll() {
        return (ArrayList<User>) userRepository.findAll();
    }


    public User getUserByLogin(String login) {
        return userRepository.findUserByLogin(login).get();
    }

    public ArrayList<User> getWorkers() {
        return (ArrayList<User>) userRepository.findUsersByRole(UserRole.WORKER);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void saveAll(ArrayList<User> users) {
        userRepository.saveAll(users);
    }

 
    // public User saveNewUser(AuthRegisterDTO inputUser) {
    //     try {
    //         var user = UserEntity.builder()
    //                 .email(inputUser.getEmail())
    //                 .firstName(inputUser.getFirstName())
    //                 .secondName(inputUser.getLastName())
    //                 .login(inputUser.getEmail())
    //                 .role(UserRole.ADMIN)
    //                 .password(DigestUtils.sha256Hex(inputUser.getPassword()))
    //                 .build();
    //         System.out.println(user);
    //         userRepository.save(user);
    //         return user;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return null;
    //     }
    // }

    // public void saveUser(UserEntity user) {
    //     userRepository.save(user);
    // }


}
