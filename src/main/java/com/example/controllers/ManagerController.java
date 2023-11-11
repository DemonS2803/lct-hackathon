package com.example.controllers;

import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entities.FromToDTO;
import com.example.enums.UserRole;
import com.example.services.MapsService;
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
    @Autowired
    private MapsService mapsService;



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

    @PutMapping("/go_next_day")
    public ResponseEntity<?> goNextDay() {
        try {
            taskService.goNextDay();
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    @PutMapping("/doomsday")
    public ResponseEntity<?> goDoomsday() {
        taskService.doomsday();
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

    @PutMapping("/doomsday2")
    public ResponseEntity<?> goDoomsday2() {
        taskService.doomsday2();
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

    @GetMapping("/fill_1")
    public ResponseEntity<?> fillData1Case() {
        taskService.fill1Case();
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }


     @GetMapping("/fill_all")
    public ResponseEntity<?> fillDataAllCase() {
        taskService.fillAllDatabase();
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }


    
}
