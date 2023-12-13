package com.example.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.mapping.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dto.CreateUserDTO;
import com.example.dto.EditWorkerDTO;
import com.example.dto.ManageWorkerDTO;
import com.example.entities.Task;
import com.example.entities.User;
import com.example.enums.EmployeeGrade;
import com.example.enums.TaskPriority;
import com.example.enums.TaskStatus;
import com.example.enums.UserRole;
import com.example.repositories.TaskRepository;
import com.example.repositories.UserRepository;


import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;

    public ArrayList<User> findAll() {
        return (ArrayList<User>) userRepository.findAll();
    }


    public User getUserByLogin(String login) {
        return userRepository.findUserByLogin(login);
    }

    public ArrayList<User> getWorkers() {
        return (ArrayList<User>) userRepository.findUsersByRole(UserRole.WORKER);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void saveAll(ArrayList<User> users) {
        userRepository.saveAll(users);
    }

    public boolean createNewWorker(CreateUserDTO userDto) {
        try {
            var us = userRepository.findUserByLogin(userDto.getLogin());
            System.out.println(us);
            if (us != null) {
                throw new RuntimeException();
            }
            System.out.println("vse taki creating");
            User user = new User().builder()
                                .address(userDto.getAddress())
                                .login(userDto.getLogin())
                                .grade(EmployeeGrade.valueOf(userDto.getGrade()))
                                .name(userDto.getName())
                                .role(UserRole.WORKER)
                                .password("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8")
                                .build();
            System.out.println("saving: user");
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            System.out.println("user " + userDto.getLogin() + " already excists mb");
            e.printStackTrace();
            return false;
        }
    }

    public boolean editWorker(EditWorkerDTO dto) {
        try {
            User user = userRepository.findUserByLogin(dto.getLogin());
            System.out.println("found user: " + user);
            user.setAddress(dto.getAddress());
            user.setGrade(dto.getGrade());
            user.setName(dto.getName());;
            userRepository.save(user);
            System.out.println(userRepository.save(user));
            System.out.println("edited user: " + user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteWorkerByLogin(String login) {
        try {
            var us = userRepository.findUserByLogin(login);
            if (us == null) {
                throw new RuntimeException();
            }
            ArrayList<Task> completedTasks = taskRepository.findAllTasksByStatusAndExecutorLogin(TaskStatus.COMPLETED, login);
            for (Task task: completedTasks) {
                taskRepository.delete(task);
            }
            System.out.println("deleting: " + us);
            userRepository.delete(us);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ManageWorkerDTO getWorkerInfo(String login) {
        ManageWorkerDTO dto = new ManageWorkerDTO(userRepository.findUserByLogin(login));
        dto.setCompletedTasks(taskRepository.findAllTasksByStatusAndExecutorLogin(TaskStatus.COMPLETED, login));
        for (var task: dto.getCompletedTasks()) {
            task.setExecutor(null);
            if (task.getPriority().equals(TaskPriority.HIGH)) {
                dto.addHigh();
            } else if (task.getPriority().equals(TaskPriority.MEDIUM)) {
                dto.addMedium();
            } else if (task.getPriority().equals(TaskPriority.LOW)) {
                dto.addLow();
            }
        }
        return dto;
    }
}
