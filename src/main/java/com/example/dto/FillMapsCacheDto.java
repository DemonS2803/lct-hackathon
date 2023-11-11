package com.example.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.entities.FromToDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FillMapsCacheDto {

    private ArrayList<FromToDTO> data;
    
}
