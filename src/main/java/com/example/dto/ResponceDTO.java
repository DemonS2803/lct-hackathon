package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.example.entities.User;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResponceDTO {

    private String token;
    private User user;

}
