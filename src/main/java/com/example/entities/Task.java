package com.example.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.enums.TaskPriority;
import com.example.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String address;
    private int localeX;
    private int localeY;
    @Enumerated(EnumType.ORDINAL)
    private TaskPriority priority;
    @Enumerated(EnumType.ORDINAL)
    private TaskStatus status;
    private double hoursDuration;
    private double workersScore;
    @ManyToOne
    private User executor;


    public Task(Long id, PartnerPoint point) {
        this.id = id;
        this.address = point.getAddress();
        this.localeX = point.getLocaleX();
        this.localeY = point.getLocaleY();
        this.workersScore = 0;
        this.status = TaskStatus.CREATED;

        if (point.getDaysAfterLastCard() > 7 && point.getApprovedRequest() > 0 || point.getDaysAfterLastCard() > 14) {
            this.priority = TaskPriority.HIGH;
            hoursDuration = 4;
        } else if (point.getDeliveredCardsNumber() > 0 && ((double) point.getDeliveredCardsNumber() / point.getApprovedRequest() < 0.5)) {
            priority = TaskPriority.MEDIUM;
            hoursDuration = 2;
        } else if (point.getConnectionDate().getDayOfYear() - LocalDateTime.now().getDayOfYear() == 1 || !point.isCardsAndMaterialsDelivered()) {
            priority = TaskPriority.LOW;
            hoursDuration = 1.5;
        }
    }

    // public Task getCopy() {
    //     var copy = new Task();
    //     copy.setId(id);
    //     copy.setAddress(address);
    //     copy.setExecutor(executor);
    //     copy.setPriority(priority);
    //     copy.setStatus(status);
    //     return copy;
    // }


    @Override
    public String toString() {
        return "Task # " + id + "(" + localeX + "," + localeY + ") " + priority + " | executor: " + (executor == null ? "NEXT TIME MAYBE" : executor.getName());
    }

    public void addScore(double a) {
        this.workersScore += a;
    }
    

}
