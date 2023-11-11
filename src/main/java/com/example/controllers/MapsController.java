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

import com.example.dto.FillMapsCacheDto;
import com.example.entities.FromToDTO;
import com.example.services.MapsService;
import com.example.services.TaskService;

@RestController
@RequestMapping("/maps")
@CrossOrigin("*")
public class MapsController {
    
    @Autowired
    private MapsService mapsService;
    @Autowired
    private TaskService taskService;

    @GetMapping("/get_addresses_for_ya_api")
    public ResponseEntity<?> getAddressesForYaApi() {
        mapsService.fillWaysRequest(taskService.getTasks(), taskService.getUsers());
        return new ResponseEntity<>(mapsService.getWaysRequestSet(), HttpStatusCode.valueOf(200));
    }

    @PostMapping("/fill_maps_cache") 
    public ResponseEntity<?> fillMapsCache(@RequestBody FillMapsCacheDto data) {
        try {
            System.out.println("fill cahce");
            System.out.println(data);
            mapsService.fillCache(data.getData());
            System.out.println("cache size: " + mapsService.getCache().size());
            System.out.println(mapsService.getCache());
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    @GetMapping("/cache")
    public ResponseEntity<?> getWaysCache() {
        System.out.println();
        System.out.println(mapsService.getCache().size());
        System.out.println(mapsService.getCache());
        return new ResponseEntity<>(mapsService.getCache(), HttpStatus.OK);
    }



}
