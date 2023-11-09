package com.example.controllers;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.FromToDTO;
import com.example.services.MapsService;
import com.example.services.TaskService;

@RestController
@CrossOrigin("*")
@RequestMapping("/maps")
public class MapsController {
    
    @Autowired
    private MapsService mapsService;
    @Autowired
    private TaskService taskService;

    @GetMapping("/get_addresses_for_ya_api")
    public ResponseEntity<?> getAddressesForYaApi() {
        mapsService.fillWaysCashe(taskService.getTasks(), taskService.getUsers());
        return new ResponseEntity<>(mapsService.getWaysRequestSet(), HttpStatusCode.valueOf(200));
    }

    @PostMapping("/fill_maps_cache") 
    public ResponseEntity<?> fillMapsCache(@RequestBody ArrayList<FromToDTO> ways) {
        try {
            mapsService.fillCache(ways);
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }



}
