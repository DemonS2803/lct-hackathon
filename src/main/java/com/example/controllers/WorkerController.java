package com.example.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.WorkerPageDTO;
import com.example.services.TaskService;
import com.example.services.UserService;


@RestController
@RequestMapping("/job")
@CrossOrigin("*")
public class WorkerController {

    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;

    
    @GetMapping("/get_tasks") 
    public ResponseEntity<?> getTasksForWorker() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ResponseEntity<>(new WorkerPageDTO(taskService.getUserAddressByLogin(login), taskService.getTasksByLogin(login)), HttpStatusCode.valueOf(200));
    }



    @PutMapping("/complete_task")
    public ResponseEntity<?> completeTask() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean success = taskService.executeTask(login);
        if (success) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } else return new ResponseEntity<>(HttpStatusCode.valueOf(401));
    }


}
