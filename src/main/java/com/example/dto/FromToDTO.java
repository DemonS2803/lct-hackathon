package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FromToDTO {

    private String from;
    private String to;
    private double minutes;

    public FromToDTO(String from, String to) {
        this.from = from;
        this.to = to;
        this.minutes = 0;
    }
    
}