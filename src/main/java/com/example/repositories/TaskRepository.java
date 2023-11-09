package com.example.repositories;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entities.Task;
import com.example.enums.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    ArrayList<Task> findAllTasksByStatus(TaskStatus status);
    ArrayList<Task> findAllTasksByStatusAndExecutorLogin(TaskStatus status, String login);

}
