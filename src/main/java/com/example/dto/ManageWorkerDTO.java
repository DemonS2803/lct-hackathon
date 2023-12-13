package com.example.dto;

import java.util.ArrayList;

import org.hibernate.mapping.List;

import com.example.entities.Task;
import com.example.entities.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageWorkerDTO {

    private User user;
    private ArrayList<Task> completedTasks;
    private int highNumber;
    private int mediumNumber;
    private int lowNumber;

    public ManageWorkerDTO(User user) {
        this.user = user;
        highNumber = 0;
        mediumNumber = 0;
        lowNumber = 0;
    }

    public void addHigh() {
        this.highNumber++;
    }
    public void addMedium() {
        this.mediumNumber++;
    }
    public void addLow() {
        this.lowNumber++;
    }
    
}
