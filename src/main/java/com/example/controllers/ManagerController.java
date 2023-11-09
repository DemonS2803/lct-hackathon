package com.example.controllers;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.enums.UserRole;
import com.example.services.TaskService;
import com.example.services.UserService;

@RestController
@RequestMapping("/manage")
@CrossOrigin("*")
public class ManagerController {

    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;



    @GetMapping("/get_workers")
    public ResponseEntity<?> getWorkers() {
        try {
            var users = taskService.getUsers().stream().filter(a -> a.getRole().equals(UserRole.WORKER)).collect(Collectors.toList());
            System.out.println(users.size());
            return new ResponseEntity<>(users, HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            // e.printStackTrace();            
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));

        }
    }

    @GetMapping("/distribute")
    public ResponseEntity<?> distributeTasks() {
        var tasks = taskService.distributeTasks();
        return new ResponseEntity<>(tasks, HttpStatusCode.valueOf(200));
    }

    @GetMapping("/get_tasks")
    public ResponseEntity<?> getTasks() {
        return new ResponseEntity<>(taskService.getTasks(), HttpStatusCode.valueOf(200));
    }

    @GetMapping("/get_tasks_history")
    public ResponseEntity<?> getTasksHistory() {
        return new ResponseEntity<>(taskService.getAllTasks(), HttpStatusCode.valueOf(200));
    }

    @GetMapping("/get_partner_points")
    public ResponseEntity<?> getPartnerPoints() {
        return new ResponseEntity<>(taskService.getPartnerPoints(), HttpStatusCode.valueOf(200));
    }

    @GetMapping("/fill_1")
    public ResponseEntity<?> fillData1Case() {
        taskService.fill1Case();
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

     @GetMapping("/fill_2")
    public ResponseEntity<?> fillData2Case() {
        taskService.fill2Case();
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

     @GetMapping("/fill_all")
    public ResponseEntity<?> fillDataAllCase() {
        taskService.fillAllDatabase();
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

    
}
