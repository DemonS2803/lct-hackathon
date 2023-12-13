package com.example.dto;

import com.example.enums.EmployeeGrade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditWorkerDTO {

    private String login;
    private String name;
    private String address;
    private EmployeeGrade grade;
    
}
