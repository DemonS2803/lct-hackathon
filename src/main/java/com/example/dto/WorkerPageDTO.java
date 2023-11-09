package com.example.dto;

import java.util.ArrayList;

import com.example.entities.Task;
import com.example.entities.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerPageDTO {
    
    private String userAddress;
    private ArrayList<Task> tasks;

}
