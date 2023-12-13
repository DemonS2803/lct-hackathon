package com.example.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CreateUserDTO;
import com.example.dto.EditPartnerPointDTO;
import com.example.dto.EditWorkerDTO;
import com.example.dto.ManageWorkerDTO;
import com.example.entities.FromToDTO;
import com.example.entities.PartnerPoint;
import com.example.entities.User;
import com.example.enums.UserRole;
import com.example.services.ApachePOIExcelWrite;
import com.example.services.MapsService;
import com.example.services.PartnerPointService;
import com.example.services.TaskService;
import com.example.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    @Autowired
    private PartnerPointService partnerPointService;
    @Autowired 
    private ApachePOIExcelWrite apachePOIExcelWrite;



    @GetMapping("/get_workers")
    public ResponseEntity<?> getWorkers() {
        try {
            // var users = taskService.getUsers().stream().filter(a -> a.getRole().equals(UserRole.WORKER)).collect(Collectors.toList());
            return new ResponseEntity<>(taskService.getWorkersInfo(), HttpStatusCode.valueOf(200));
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

    @PutMapping("/go_next_day2")
    public ResponseEntity<?> goNextDay2() {
        try {
            taskService.goNextDay2();
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

    @PostMapping("/point") 
    public ResponseEntity<?> createNewPartnerPoint(@RequestBody @Validated PartnerPoint point) {
        System.out.println("incoming: " + point);
        if (partnerPointService.createNewPartnerPoint(point)) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/point")
    public ResponseEntity<?> editPartnerPoint(@RequestBody @Validated EditPartnerPointDTO dto) {
        // System.out.println("incoming: " + dto);
        if (partnerPointService.editPartnerPoint(dto)) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/point/{id}")
    public ResponseEntity<?> deletePartnerPoint(@PathVariable Long id) {
        if (partnerPointService.deletePoint(id)) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/worker")
    public ResponseEntity<?> createWorker(@RequestBody @Validated CreateUserDTO userDto) {
        System.out.println("incoming: " + userDto);
        if (userService.createNewWorker(userDto)) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/worker")
    public ResponseEntity<?> editWorker(@RequestBody @Validated EditWorkerDTO dto) {
        
        System.out.println("incoming: " + dto);
        if (userService.editWorker(dto)) {
            taskService.editUserByDto(dto);
            System.out.println(userService.getUserByLogin(dto.getLogin()));
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/worker/{login}")
    public ResponseEntity<?> deleteWorker(@PathVariable String login) {
        System.out.println("incoming to del: " + login);
        if (userService.deleteWorkerByLogin(login)) {
            taskService.deleteUserByLogin(login);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/info/{login}")
    public ResponseEntity<?> getUserInfo(@PathVariable String login) {
        return new ResponseEntity<>(userService.getWorkerInfo(login), HttpStatus.OK);
    }

    @GetMapping("/stats/{login}")
    public ResponseEntity<?> getUserStatsExcel(@PathVariable String login, HttpServletRequest request) {
        try {
            String fileName = apachePOIExcelWrite.generateExcelFile(login);
            Path filePath = Paths.get(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(request.getServletContext().getMimeType(resource.getFile().getAbsolutePath())))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/planned")
    public ResponseEntity<?> getPlanned() {
        return new ResponseEntity<>(taskService.getPlannedTask(), HttpStatusCode.valueOf(200));
    }

    
}
