package com.example.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.jwt.algorithms.Algorithm;
import com.example.dto.FromToDTO;
import com.example.dto.Way;
import com.example.entities.PartnerPoint;
import com.example.entities.Task;
import com.example.entities.User;
import com.example.enums.EmployeeGrade;
import com.example.enums.TaskStatus;
import com.example.enums.UserRole;
import com.example.repositories.PartnerPointsRepository;
import com.example.repositories.TaskRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.From;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private MapsService mapsService;
    @Autowired
    private UserService userService;
    @Autowired
    private PartnerPointsRepository partnerPointsRepository;

    private ArrayList<Task> nextDayTasks;
    private ArrayList<Task> tasks;
    private ArrayList<User> users;
    private ArrayList<PartnerPoint> partnerPoints;
    private Map<String, String> usersStartMap;
    private Map<String, ArrayList<Task>> tasksCompletedByUsers;

    @PostConstruct
    public void initTaskService() {
        log.info("initialized task service");
        partnerPoints = (ArrayList<PartnerPoint>) partnerPointsRepository.findAll();
        nextDayTasks = new ArrayList<>();
        tasks = taskRepository.findAllTasksByStatus(TaskStatus.CREATED);
        users = userService.findAll();

        distributeTasks();
    }


    public ArrayList<User> distributeTasks() {
        sortEmployeesByGrade();
        sortTasksByPriority();
        getTasksScore();
        usersStartMap = new HashMap<>();
        tasksCompletedByUsers = new HashMap<>();

        for (var user: users) {
            usersStartMap.put(user.getLogin(), user.getLocaleX() + " " + user.getLocaleY());
        }
        System.out.println(usersStartMap);
        // for (var task: tasks) {
        //     User closestUser = null;
            // double closestWorkerDistance = Double.MAX_VALUE;
        //     for (User user : users) {
        //         double roadTime = mapsService.gotTime(user.getAddress(), task.getAddress());
        //         // task.addScore();
        //         if (task.getHoursDuration() + roadTime < closestWorkerDistance) {
        //             closestUser = user;
        //             closestWorkerDistance = task.getHoursDuration() + roadTime;
        //         }
        //     }
        //     if (closestUser != null)
        //         closestUser.addTask(task);
        //     else
        //         nextDayTasks.add(task);
        // }

        for (var task: tasks) {
            User closestUser = null;
            double closestWorkerDistance = Double.MAX_VALUE;
            for (User user : users) {
                task.addScore(user.getTaskTimeWithRoad(task));
                if (user.getTaskTimeWithRoad(task) < closestWorkerDistance) {
                    closestUser = user;
                    closestWorkerDistance = user.getTaskTimeWithRoad(task);
                }
            }
            if (closestUser != null)
                closestUser.addTask(task);
            else
                nextDayTasks.add(task);
        }
        for (var user: users) {
            if (!user.getPlannedTasks().isEmpty()) {
                var task = user.getPlannedTasks().peek();
                task.setStatus(TaskStatus.IN_PROGRESS);
                taskRepository.save(task);
            }
            user.setLocaleX(Integer.parseInt(usersStartMap.get(user.getLogin()).split(" ")[0]));
            user.setLocaleX(Integer.parseInt(usersStartMap.get(user.getLogin()).split(" ")[1]));
        }
        return users;

    }

    public boolean executeTask(String workerLogin) {
        for (User worker: users) {
            if (worker.getLogin().equals(workerLogin) && !worker.getPlannedTasks().isEmpty()) {
                Task task = worker.getPlannedTasks().poll();
                task.setStatus(TaskStatus.COMPLETED);
                worker.executeTask(task);
                // worker.getCompletedTasks().add(task);
                if (!tasksCompletedByUsers.containsKey(workerLogin)) {
                    tasksCompletedByUsers.put(workerLogin, new ArrayList<>());
                } 
                tasksCompletedByUsers.get(workerLogin).add(task);
                System.out.println(tasksCompletedByUsers);
                var nextTask = worker.getPlannedTasks().peek();
                nextTask.setStatus(TaskStatus.IN_PROGRESS);
                taskRepository.save(nextTask);
                taskRepository.save(task);
                return true;
            }
        }
        return false;
    }

    // public Queue<Task> getTasksByLogin(String login) {
    //     for (User worker: users) {
    //         if (worker.getLogin().equals(login)) {
    //             return worker.getPlannedTasks();
    //         }
    //     }
    //     return null;
    // }



    public void fill1Case() {
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        partnerPoints = new ArrayList<>();
        partnerPoints.add(new PartnerPoint(1L, "г. Краснодар, ул. Ставропольская, д. 140", 2, 2, LocalDateTime.now().minusDays(1), true, 0, 0, 0));
        // partnerPoints.add(new PartnerPoint(2L, "г. Краснодар, ул. Ставропольская, д. 140", -2, -1, LocalDateTime.now().minusDays(20), true, 3, 15, 3));
        // partnerPoints.add( new PartnerPoint(3L, "г. Краснодар, ул. Ставропольская, д. 140", 1, 2, LocalDateTime.now().minusDays(20), true, 3, 9, 1));
        // partnerPoints.add(new PartnerPoint(4L, "г. Краснодар, ул. Красноармейская, д. 126", -2, 1, LocalDateTime.now().minusDays(20), true, 0, 38, 23));
        partnerPoints.add(new PartnerPoint(5L, "г. Краснодар, х. Ленина, п/о. 37", -2, 1, LocalDateTime.now().minusDays(50), false, 0, 14, 0));
        
        partnerPoints.add(new PartnerPoint(6L, "г. Краснодар, тер. Пашковский жилой массив, ул. Крылатая, д. 2", 1, 2, LocalDateTime.now().minusDays(50), true, 12, 19, 1));
        partnerPoints.add(new PartnerPoint(7L, "г. Краснодар, ул. Восточно-Кругликовская, д. 64/2", -2, -3, LocalDateTime.now().minusDays(50), true, 27, 19, 12));
        // partnerPoints.add(new PartnerPoint(8L, "г. Краснодар, ул. Красных Партизан, д. 439", 2, 2, LocalDateTime.now().minusDays(50), true, 33, 84, 63));
        // partnerPoints.add(new PartnerPoint(9L, "г. Краснодар, ул. Таманская, д. 153 к. 3, кв. 2", 2, 2, LocalDateTime.now().minusDays(50), true, 2, 15, 1));
        // partnerPoints.add(new PartnerPoint(10L, "г. Краснодар, ул. им. Дзержинского, д. 165", 2, 2, LocalDateTime.now().minusDays(50), true, 0, 19, 0));
        // partnerPoints.add(new PartnerPoint(11L, "г. Краснодар, ст-ца. Елизаветинская, ул. Широкая, д. 260", 2, 2, LocalDateTime.now().minusDays(50), true, 15, 29, 15));
        partnerPoints.add(new PartnerPoint(12L, "г. Краснодар, ул. им. Тургенева, д. 174, 1 этаж", -2, -1, LocalDateTime.now().minusDays(1), false, 0, 0, 0));
        // partnerPoints.add(new PartnerPoint(13L, "г. Краснодар, ул. Уральская, д. 162", 2, 2, LocalDateTime.now().minusDays(50), true, 4, 21, 5));
        partnerPoints.add(new PartnerPoint(14L, "г. Краснодар, ул. Уральская, д. 79/1", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 5, 0));
        // partnerPoints.add(new PartnerPoint(15L, "г. Краснодар, ул. им. Селезнева, д. 197/5", 2, 2, LocalDateTime.now().minusDays(50), true, 7, 14, 3));
        // partnerPoints.add(new PartnerPoint(16L, "г. Краснодар, ул. Уральская, д. 117", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 0, 0));
        
        // partnerPoints.add(new PartnerPoint(19L, "г. Краснодар, ул. Зиповская, д. 1", 2, 2, LocalDateTime.now().minusDays(70), true, 6, 32, 9));
        // partnerPoints.add(new PartnerPoint(20L, "г. Краснодар, ул. им. 40-летия Победы, д. 20/1", 2, 2, LocalDateTime.now().minusDays(20), true, 4, 35, 15));
        // partnerPoints.add(new PartnerPoint(21L, "г. Краснодар, ул. им. Атарбекова, д. 24", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 6, 0));
        // partnerPoints.add(new PartnerPoint(22L, "г. Краснодар, ул. им. Героя Аверкиева А.А., д. 8", 2, 2, LocalDateTime.now().minusDays(12), false, 6, 18, 6));
        // partnerPoints.add(new PartnerPoint(23L, "г. Краснодар, ул. им. Героя Аверкиева А.А., д. 8/1 к. мая, кв. 268", 2, 2, LocalDateTime.now().minusDays(23), false, 0, 15, 5));
        // partnerPoints.add(new PartnerPoint(24L, "г. Краснодар, ул. им. Тургенева, д. 106", 2, 2, LocalDateTime.now().minusDays(13), false, 2, 96, 20));
        // partnerPoints.add(new PartnerPoint(25L, "г. Краснодар, ул. Красных Партизан, д. 117", 2, 2, LocalDateTime.now().minusDays(1), true, 0, 0, 0));
        // partnerPoints.add(new PartnerPoint(26L, "г. Краснодар, ул. Северная, д. 389", 2, 2, LocalDateTime.now().minusDays(50), false, 0, 16, 0));
        // partnerPoints.add(new PartnerPoint(27L, "г. Краснодар, ул. Уральская, д. 166/3", 2, 2, LocalDateTime.now().minusDays(25), false, 3, 43, 29));
        // partnerPoints.add(new PartnerPoint(28L, "г. Краснодар, ул. Северная, д. 524", 2, 2, LocalDateTime.now().minusDays(53), false, 3, 13, 4));
        // partnerPoints.add(new PartnerPoint(29L, "г. Краснодар, ул. им. Кирилла Россинского, д. 61/1", 2, 2, LocalDateTime.now().minusDays(19), false, 6, 19, 5));
        // partnerPoints.add(new PartnerPoint(30L, "г. Краснодар, ул. Коммунаров, д. 258", 2, 2, LocalDateTime.now().minusDays(11), false, 16, 45, 30));
       
        // partnerPoints.add(new PartnerPoint(31L, "г. Краснодар, ул. им. Дзержинского, д. 100", 2, 2, LocalDateTime.now().minusDays(11), true, 1, 19, 4));
        // partnerPoints.add(new PartnerPoint(32L, "г. Краснодар, ул. Северная, д. 326", 2, 2, LocalDateTime.now().minusDays(41), true, 3, 20, 9));
        // partnerPoints.add(new PartnerPoint(33L, "г. Краснодар, ул. им. 40-летия Победы, д. 34", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 19, 0));
        // partnerPoints.add(new PartnerPoint(34L, "г. Краснодар, ул. Красная, д. 176", 2, 2, LocalDateTime.now().minusDays(131), true, 76, 82, 72));
        // partnerPoints.add(new PartnerPoint(35L, "г. Краснодар, ул. Уральская, д. 79/1", 2, 2, LocalDateTime.now().minusDays(15), true, 23, 32, 21));
        // partnerPoints.add(new PartnerPoint(36L, "г. Краснодар, ул. Северная, д. 326", 2, 2, LocalDateTime.now().minusDays(11), true, 4, 19, 4));
        // partnerPoints.add(new PartnerPoint(37L, "г. Краснодар, ул. Красная, д. 149", 2, 2, LocalDateTime.now().minusDays(61), true, 9, 10, 7));
        // partnerPoints.add(new PartnerPoint(38L, "г. Краснодар, п. Березовый, ул. Целиноградская, д. 6/1", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 13, 0));
        // partnerPoints.add(new PartnerPoint(39L, "г. Краснодар, ул. им. Дзержинского, д. 100", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 10, 0));

        // partnerPoints.add(new PartnerPoint(40L, "г. Краснодар, ул. Российская, д. 418", 2, 2, LocalDateTime.now().minusDays(21), true, 6, 30, 14));
        // partnerPoints.add(new PartnerPoint(41L, "г. Краснодар, ул. им. Володи Головатого, д. 313", 2, 2, LocalDateTime.now().minusDays(10), true, 6, 65, 12));
        // partnerPoints.add(new PartnerPoint(42L, "г. Краснодар, ул. Красная, д. 145", 2, 2, LocalDateTime.now().minusDays(8), true, 3, 20, 4));
        // partnerPoints.add(new PartnerPoint(43L, "г. Краснодар, ул. Красная, д. 154", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 0, 0));


        // deleteAll();
        long i = 1L;
        for (var point: partnerPoints) {
            tasks.add(new Task(i++, point));
        }
        System.out.println("tasks before filter: " + tasks.size());
        sortTasksByPriority();
        System.out.println("tasks after filter: " + tasks.size());

        // userService.deleteAll();
        // users.add(new User("deriagin", "Дерягин Никита Владимирович", EmployeeGrade.SENIOR, 0, 0, "Краснодар, Красная, д. 139"));
        // users.add(new User("petro", "Петрошев Валерий Павлович", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, Красная, д. 139"));
        // users.add(new User("evdokimov!", "Евдокимов Давид Тихонович", EmployeeGrade.JUNIOR, 0, 0, "Краснодар, Красная, д. 139"));
        users.add(new User("ivan", "Андреев Гордий Данилович", EmployeeGrade.SENIOR, 0, 0, "г. Краснодар, В.Н. Мачуги, 41"));
        // users.add(new User("ivan", "Иванов Адам Федорович", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, В.Н. Мачуги, 41"));
        // users.add(new User("bobsley", "Бобылёв Ипполит Альбертович", EmployeeGrade.JUNIOR, 0, 0, "Краснодар, В.Н. Мачуги, 41"));
        // users.add(new User("belya123", "Беляева Евгения Антоновна", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, Красных Партизан, 321"));
        users.add(new User("bobsley", "Николаев Азарий Платонович", EmployeeGrade.JUNIOR, 0, 0, "г. Краснодар, Красных Партизан, 321"));
        // var user = new User("admin", "Manager", EmployeeGrade.JUNIOR, 0, 0, "г. Краснодар, Красных Партизан, 321");
        // user.setRole(UserRole.MANAGER);
        // users.add(user);
        // userService.saveAll(users);
        sortEmployeesByGrade();
    }

    public void fill2Case() {
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        partnerPoints = new ArrayList<>();
        partnerPoints.add(new PartnerPoint(14L, "г. Краснодар, ул. Уральская, д. 79/1", 2, 0, LocalDateTime.now().minusDays(1), false, 0, 5, 0));
        partnerPoints.add(new PartnerPoint(5L, "г. Краснодар, х. Ленина, п/о. 37", -2, 1, LocalDateTime.now().minusDays(50), false, 0, 14, 0));
        
        partnerPoints.add(new PartnerPoint(6L, "г. Краснодар, тер. Пашковский жилой массив, ул. Крылатая, д. 2", 1, 0, LocalDateTime.now().minusDays(50), true, 12, 19, 1));
        partnerPoints.add(new PartnerPoint(7L, "г. Краснодар, ул. Восточно-Кругликовская, д. 64/2", -1, 1, LocalDateTime.now().minusDays(50), true, 27, 19, 12));
        
        long i = 1L;
        for (var point: partnerPoints) {
            tasks.add(new Task(i++, point));
        }
        System.out.println("tasks before filter: " + tasks.size());
        sortTasksByPriority();
        System.out.println("tasks after filter: " + tasks.size());

        // userService.deleteAll();
        // users.add(new User("deriagin", "Дерягин Никита Владимирович", EmployeeGrade.SENIOR, 0, 0, "Краснодар, Красная, д. 139"));
        // users.add(new User("petro", "Петрошев Валерий Павлович", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, Красная, д. 139"));
        // users.add(new User("evdokimov!", "Евдокимов Давид Тихонович", EmployeeGrade.JUNIOR, 0, 0, "Краснодар, Красная, д. 139"));
        users.add(new User("ivan", "Андреев Гордий Данилович", EmployeeGrade.SENIOR, 0, 0, "г. Краснодар, В.Н. Мачуги, 41"));
        // users.add(new User("ivan", "Иванов Адам Федорович", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, В.Н. Мачуги, 41"));
        // users.add(new User("bobsley", "Бобылёв Ипполит Альбертович", EmployeeGrade.JUNIOR, 0, 0, "Краснодар, В.Н. Мачуги, 41"));
        // users.add(new User("belya123", "Беляева Евгения Антоновна", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, Красных Партизан, 321"));
        users.add(new User("bobsley", "Николаев Азарий Платонович", EmployeeGrade.SENIOR, 2, 1, "г. Краснодар, Красных Партизан, 321"));
        // var user = new User("admin", "Manager", EmployeeGrade.JUNIOR, 0, 0, "г. Краснодар, Красных Партизан, 321");
        // user.setRole(UserRole.MANAGER);
        // users.add(user);
        // userService.saveAll(users);

        sortEmployeesByGrade();
    }

    public void fillAllDatabase() {
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        partnerPoints.add(new PartnerPoint(1L, "г. Краснодар, ул. Ставропольская, д. 140", 2, 2, LocalDateTime.now().minusDays(1), true, 0, 0, 0));
        partnerPoints.add(new PartnerPoint(2L, "г. Краснодар, ул. Ставропольская, д. 140", -2, -1, LocalDateTime.now().minusDays(20), true, 3, 15, 3));
        partnerPoints.add( new PartnerPoint(3L, "г. Краснодар, ул. Ставропольская, д. 140", 1, 2, LocalDateTime.now().minusDays(20), true, 3, 9, 1));
        partnerPoints.add(new PartnerPoint(4L, "г. Краснодар, ул. Красноармейская, д. 126", -2, 1, LocalDateTime.now().minusDays(20), true, 0, 38, 23));
        partnerPoints.add(new PartnerPoint(5L, "г. Краснодар, х. Ленина, п/о. 37", -2, 1, LocalDateTime.now().minusDays(50), false, 0, 14, 0));
        
        partnerPoints.add(new PartnerPoint(6L, "г. Краснодар, тер. Пашковский жилой массив, ул. Крылатая, д. 2", 1, 2, LocalDateTime.now().minusDays(50), true, 12, 19, 1));
        partnerPoints.add(new PartnerPoint(7L, "г. Краснодар, ул. Восточно-Кругликовская, д. 64/2", -2, -3, LocalDateTime.now().minusDays(50), true, 27, 19, 12));
        partnerPoints.add(new PartnerPoint(8L, "г. Краснодар, ул. Красных Партизан, д. 439", 2, 2, LocalDateTime.now().minusDays(50), true, 33, 84, 63));
        partnerPoints.add(new PartnerPoint(9L, "г. Краснодар, ул. Таманская, д. 153 к. 3, кв. 2", 2, 2, LocalDateTime.now().minusDays(50), true, 2, 15, 1));
        partnerPoints.add(new PartnerPoint(10L, "г. Краснодар, ул. им. Дзержинского, д. 165", 2, 2, LocalDateTime.now().minusDays(50), true, 0, 19, 0));
        partnerPoints.add(new PartnerPoint(11L, "г. Краснодар, ст-ца. Елизаветинская, ул. Широкая, д. 260", 2, 2, LocalDateTime.now().minusDays(50), true, 15, 29, 15));
        partnerPoints.add(new PartnerPoint(12L, "г. Краснодар, ул. им. Тургенева, д. 174, 1 этаж", -2, -1, LocalDateTime.now().minusDays(1), false, 0, 0, 0));
        partnerPoints.add(new PartnerPoint(13L, "г. Краснодар, ул. Уральская, д. 162", 2, 2, LocalDateTime.now().minusDays(50), true, 4, 21, 5));
        partnerPoints.add(new PartnerPoint(14L, "г. Краснодар, ул. Уральская, д. 79/1", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 5, 0));
        partnerPoints.add(new PartnerPoint(15L, "г. Краснодар, ул. им. Селезнева, д. 197/5", 2, 2, LocalDateTime.now().minusDays(50), true, 7, 14, 3));
        partnerPoints.add(new PartnerPoint(16L, "г. Краснодар, ул. Уральская, д. 117", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 0, 0));
        
        partnerPoints.add(new PartnerPoint(19L, "г. Краснодар, ул. Зиповская, д. 1", 2, 2, LocalDateTime.now().minusDays(70), true, 6, 32, 9));
        partnerPoints.add(new PartnerPoint(20L, "г. Краснодар, ул. им. 40-летия Победы, д. 20/1", 2, 2, LocalDateTime.now().minusDays(20), true, 4, 35, 15));
        partnerPoints.add(new PartnerPoint(21L, "г. Краснодар, ул. им. Атарбекова, д. 24", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 6, 0));
        partnerPoints.add(new PartnerPoint(22L, "г. Краснодар, ул. им. Героя Аверкиева А.А., д. 8", 2, 2, LocalDateTime.now().minusDays(12), false, 6, 18, 6));
        partnerPoints.add(new PartnerPoint(23L, "г. Краснодар, ул. им. Героя Аверкиева А.А., д. 8/1 к. мая, кв. 268", 2, 2, LocalDateTime.now().minusDays(23), false, 0, 15, 5));
        partnerPoints.add(new PartnerPoint(24L, "г. Краснодар, ул. им. Тургенева, д. 106", 2, 2, LocalDateTime.now().minusDays(13), false, 2, 96, 20));
        partnerPoints.add(new PartnerPoint(25L, "г. Краснодар, ул. Красных Партизан, д. 117", 2, 2, LocalDateTime.now().minusDays(1), true, 0, 0, 0));
        partnerPoints.add(new PartnerPoint(26L, "г. Краснодар, ул. Северная, д. 389", 2, 2, LocalDateTime.now().minusDays(50), false, 0, 16, 0));
        partnerPoints.add(new PartnerPoint(27L, "г. Краснодар, ул. Уральская, д. 166/3", 2, 2, LocalDateTime.now().minusDays(25), false, 3, 43, 29));
        partnerPoints.add(new PartnerPoint(28L, "г. Краснодар, ул. Северная, д. 524", 2, 2, LocalDateTime.now().minusDays(53), false, 3, 13, 4));
        partnerPoints.add(new PartnerPoint(29L, "г. Краснодар, ул. им. Кирилла Россинского, д. 61/1", 2, 2, LocalDateTime.now().minusDays(19), false, 6, 19, 5));
        partnerPoints.add(new PartnerPoint(30L, "г. Краснодар, ул. Коммунаров, д. 258", 2, 2, LocalDateTime.now().minusDays(11), false, 16, 45, 30));
       
        partnerPoints.add(new PartnerPoint(31L, "г. Краснодар, ул. им. Дзержинского, д. 100", 2, 2, LocalDateTime.now().minusDays(11), true, 1, 19, 4));
        partnerPoints.add(new PartnerPoint(32L, "г. Краснодар, ул. Северная, д. 326", 2, 2, LocalDateTime.now().minusDays(41), true, 3, 20, 9));
        partnerPoints.add(new PartnerPoint(33L, "г. Краснодар, ул. им. 40-летия Победы, д. 34", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 19, 0));
        partnerPoints.add(new PartnerPoint(34L, "г. Краснодар, ул. Красная, д. 176", 2, 2, LocalDateTime.now().minusDays(131), true, 76, 82, 72));
        partnerPoints.add(new PartnerPoint(35L, "г. Краснодар, ул. Уральская, д. 79/1", 2, 2, LocalDateTime.now().minusDays(15), true, 23, 32, 21));
        partnerPoints.add(new PartnerPoint(36L, "г. Краснодар, ул. Северная, д. 326", 2, 2, LocalDateTime.now().minusDays(11), true, 4, 19, 4));
        partnerPoints.add(new PartnerPoint(37L, "г. Краснодар, ул. Красная, д. 149", 2, 2, LocalDateTime.now().minusDays(61), true, 9, 10, 7));
        partnerPoints.add(new PartnerPoint(38L, "г. Краснодар, п. Березовый, ул. Целиноградская, д. 6/1", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 13, 0));
        partnerPoints.add(new PartnerPoint(39L, "г. Краснодар, ул. им. Дзержинского, д. 100", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 10, 0));

        partnerPoints.add(new PartnerPoint(40L, "г. Краснодар, ул. Российская, д. 418", 2, 2, LocalDateTime.now().minusDays(21), true, 6, 30, 14));
        partnerPoints.add(new PartnerPoint(41L, "г. Краснодар, ул. им. Володи Головатого, д. 313", 2, 2, LocalDateTime.now().minusDays(10), true, 6, 65, 12));
        partnerPoints.add(new PartnerPoint(42L, "г. Краснодар, ул. Красная, д. 145", 2, 2, LocalDateTime.now().minusDays(8), true, 3, 20, 4));
        partnerPoints.add(new PartnerPoint(43L, "г. Краснодар, ул. Красная, д. 154", 2, 2, LocalDateTime.now().minusDays(1), false, 0, 0, 0));

        partnerPointsRepository.saveAll(partnerPoints);

        deleteAll();
        long i = 1L;
        for (var point: partnerPoints) {
            tasks.add(new Task(i++, point));
        }
        taskRepository.saveAll(tasks);
        System.out.println("tasks before filter: " + tasks.size());
        System.out.println("tasks after filter: " + tasks.size());

        userService.deleteAll();
        users.add(new User("deriagin", "Дерягин Никита Владимирович", EmployeeGrade.SENIOR, 0, 0, "Краснодар, Красная, д. 139"));
        users.add(new User("petro", "Петрошев Валерий Павлович", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, Красная, д. 139"));
        users.add(new User("evdokimov!", "Евдокимов Давид Тихонович", EmployeeGrade.JUNIOR, 0, 0, "Краснодар, Красная, д. 139"));
        users.add(new User("andro", "Андреев Гордий Данилович", EmployeeGrade.SENIOR, 0, 0, "г. Краснодар, В.Н. Мачуги, 41"));
        users.add(new User("ivan", "Иванов Адам Федорович", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, В.Н. Мачуги, 41"));
        users.add(new User("bobsley", "Бобылёв Ипполит Альбертович", EmployeeGrade.JUNIOR, 0, 0, "Краснодар, В.Н. Мачуги, 41"));
        users.add(new User("belya123", "Беляева Евгения Антоновна", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, Красных Партизан, 321"));
        users.add(new User("nikolas", "Николаев Азарий Платонович", EmployeeGrade.JUNIOR, 0, 0, "г. Краснодар, Красных Партизан, 321"));
        var user = new User("admin", "Manager", EmployeeGrade.JUNIOR, 0, 0, "г. Краснодар, Красных Партизан, 321");
        user.setRole(UserRole.MANAGER);
        users.add(user);
        userService.saveAll(users);

        // tasks.add(new Task(20L, 1, 1, LocalDateTime.MIN, true, 4, 35, 15));
        // tasks.add(new Task(21L, 2, 2, LocalDateTime.now().minusDays(1), false, 0, 6, 0));
        // tasks.add(new Task(22L, 3, 1, LocalDateTime.MIN, true, 6, 18, 6));
        // tasks.add(new Task(23L, 4, 1, LocalDateTime.MIN, true, 0, 15, 5));
        // tasks.add(new Task(24L, 5, 1, LocalDateTime.MIN, true, 2, 96, 20));
        // tasks.add(new Task(25L, -2, 1, LocalDateTime.now().minusDays(1), false, 0, 0, 0));
        // tasks.add(new Task(26L, 7, 1, LocalDateTime.MIN, true, 0, 16, 0));

        // tasks.add(new Task(27L, 1, 1, LocalDateTime.MIN, true, 3, 43, 29));
        // tasks.add(new Task(28L, 2, 2, LocalDateTime.MIN, true, 3, 13, 4));
        // tasks.add(new Task(29L, 3, 3, LocalDateTime.MIN, true, 6, 19, 5));
        // tasks.add(new Task(30L, 1, 2, LocalDateTime.MIN, true, 16, 45, 30));
        // tasks.add(new Task(31L, 5, 5, LocalDateTime.MIN, true, 1, 19, 4));
        // tasks.add(new Task(32L, 6, 6, LocalDateTime.MIN, true, 3, 20, 9));
        // tasks.add(new Task(33L, -2, -1, LocalDateTime.now().minusDays(1), false, 0, 19, 0));

        // tasks.add(new Task(34L, -2, -3, LocalDateTime.MIN, true, 76, 82, 72));
        // tasks.add(new Task(35L, 2, 2, LocalDateTime.MIN, true, 23, 32, 21));
        // tasks.add(new Task(36L, 3, 3, LocalDateTime.MIN, true, 4, 19, 4));
        // tasks.add(new Task(37L, 4, 4, LocalDateTime.MIN, true, 9, 10, 7));
        // tasks.add(new Task(38L, 5, 5, LocalDateTime.now().minusDays(1), false, 0, 13, 0));
        // tasks.add(new Task(39L, 6, 6, LocalDateTime.now().minusDays(1), false, 0, 10, 0));
        // tasks.add(new Task(40L, 7, 7, LocalDateTime.MIN, true, 6, 30, 14));

        // tasks.add(new Task(41L, 7, 77, LocalDateTime.MIN, true, 6, 65, 12));
        // tasks.add(new Task(42L, 7, 75, LocalDateTime.MIN, true, 3, 20, 4));
        // tasks.add(new Task(43L, 7, 545, LocalDateTime.now().minusDays(1), false, 0, 0, 0));


        // users.add(new User(1L, "Deryagin", EmployeeGrade.SENIOR, 0, 0));
        // users.add(new User(2L, "Petroshev", EmployeeGrade.MIDDLE, 0, 0));
        // users.add(new User(3L, "Evdokimov", EmployeeGrade.JUNIOR, 0, 0));
        // users.add(new User(4L, "Andreev", EmployeeGrade.SENIOR, 0, 0));
        // users.add(new User(5L, "Ivanov", EmployeeGrade.MIDDLE, 0, 0));
        // users.add(new User(6L, "Bobylev", EmployeeGrade.JUNIOR, 0, 0));
        // users.add(new User(7L, "Belyaeva", EmployeeGrade.MIDDLE, 11, 11));
        // users.add(new User(8L, "Nikolaev", EmployeeGrade.JUNIOR,  11, 11));
    }

    public void sortEmployeesByGrade() {
        users = (ArrayList<User>) users.stream()
                .sorted((a, b) -> b.getGrade().ordinal() - a.getGrade().ordinal())
                .collect(Collectors.toList());
    }

    public void sortTasksByPriority() {
        tasks = (ArrayList<Task>) tasks.stream()
                .filter(task -> task.getPriority() != null)
                .sorted((a, b) -> b.getPriority().ordinal() == a.getPriority().ordinal() ? (int) Math.ceil(b.getWorkersScore() - a.getWorkersScore()) : b.getPriority().ordinal() - a.getPriority().ordinal())
                .collect(Collectors.toList());
    }

    public void getTasksScore() {
        for (var task: tasks) {
            for (var tmpTask: tasks) {
                task.addScore((double) tmpTask.getPriority().ordinal() / calcDist(tmpTask, task));
            }
            for (User user : users) {
                if (user.getGrade().ordinal() >= task.getPriority().ordinal()) {
                    task.addScore(-1d / user.getTaskTimeWithRoad(task));
                }
            }
        }
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<PartnerPoint> getPartnerPoints() {
        return partnerPoints;
    }
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public ArrayList<Task> getAllTasks() {
        return (ArrayList<Task>) taskRepository.findAll();
    }

    public void deleteAll() {
        taskRepository.deleteAll();
    }

    public String getUserAddressByLogin(String login) {
        for (var user: users) {
            if (user.getLogin().equals(login)) {
                return user.getAddress();
            }
        }
        return "in holidays";
    }

    public ArrayList<Task> getTasksByLogin(String login) {
        ArrayList<Task> tasks = new ArrayList<>();
        for (var user: users) {
            if (user.getLogin().equals(login)) {
                tasks.addAll(user.getPlannedTasks());
            }
        }
        if (tasksCompletedByUsers.containsKey(login)) {
            tasks.addAll(tasksCompletedByUsers.get(login));
        }
        
        return tasks;
    }

    public double calcDist(Task t1, Task t2) {
        return Math.sqrt((t1.getLocaleX() - t2.getLocaleX()) * (t1.getLocaleX() - t2.getLocaleX()) + (t1.getLocaleY() - t2.getLocaleY()) * (t1.getLocaleY() - t2.getLocaleY()));
    }
}
