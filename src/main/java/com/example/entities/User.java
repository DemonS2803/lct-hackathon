package com.example.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import com.example.enums.EmployeeGrade;
import com.example.enums.TaskStatus;
import com.example.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "users")
// @JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String login;
    @JsonIgnore
    private String password;
    private String name;
    @Enumerated(EnumType.ORDINAL)
    private UserRole role;
    @Enumerated(EnumType.ORDINAL)
    private EmployeeGrade grade;
    private String address;
    private int localeX;
    private int localeY;
    private double leftWorkingHours;
    // @OneToMany
    // @JoinTable(name = "planned_tasks_workers",
    //             joinColumns = {@JoinColumn(name = "task_id")},
    //             inverseJoinColumns = {@JoinColumn(name = "user_id")})
    @Transient
    private Queue<Task> plannedTasks = new LinkedList<>();
    // @Transient
    // private ArrayList<Task> completedTasks = new ArrayList<>();

    public User(String login, String name, EmployeeGrade grade, int localeX, int localeY, String address) {
        this.login = login;
        this.name = name;
        this.password = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";
        this.role = UserRole.WORKER;
        this.grade = grade;
        this.address = address;
        this.localeX = localeX;
        this.localeY = localeY;
        this.leftWorkingHours = 9;
        this.plannedTasks = new LinkedList<>();
    }

    // must be deleted
    public double getTaskTimeWithRoad(Task task) {
        double roadTime = Math.sqrt((localeX - task.getLocaleX()) * (localeX - task.getLocaleX())
                + (localeY - task.getLocaleY()) * (localeY - task.getLocaleY()));

        if (task.getHoursDuration() + roadTime > leftWorkingHours || task.getPriority().ordinal() > this.grade.ordinal())
            return Double.MAX_VALUE;
        return task.getHoursDuration() + roadTime;
    }

    // must be deleted
    public void addTask(Task task) {
        this.leftWorkingHours -= getTaskTimeWithRoad(task);
        this.plannedTasks.offer(task);
        moveTo(task);
    }

    // public void appendCopiedTask(Task task) {
    //     this.plannedTasks.offer(task.getCopy());
    // }

    public void addTask(Task task, double timeWithRoad) {
        this.leftWorkingHours -= timeWithRoad;
        this.plannedTasks.offer(task);
    }

    public void moveTo(Task task) {
        this.localeX = task.getLocaleX();
        this.localeY = task.getLocaleY();
    }

    public void executeTask(Task task) {
        // completedTasks.add(task);
        localeX = task.getLocaleX();
        localeY = task.getLocaleY();
        task.setExecutor(this);
    }

    @Override
    public String toString() {
        return "Worker{ name=" + name + ", grade=" + grade + "}";
    }
}
