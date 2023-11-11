package com.example.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "cached_ways")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FromToDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @JsonProperty(value = "from")
    private String fromPoint;
    @JsonProperty(value = "to")
    private String toPoint;
    private double minutes;

    public FromToDTO(String from, String to) {
        this.fromPoint = from;
        this.toPoint = to;
        this.minutes = 0;
    }
    
}