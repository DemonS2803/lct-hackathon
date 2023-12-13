package com.example.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.jwt.algorithms.Algorithm;
import com.example.dto.EditWorkerDTO;
import com.example.dto.ManageWorkerDTO;
import com.example.dto.Way;
import com.example.entities.FromToDTO;
import com.example.entities.PartnerPoint;
import com.example.entities.Task;
import com.example.entities.User;
import com.example.enums.EmployeeGrade;
import com.example.enums.TaskPriority;
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

    private ArrayList<Task> tasks;
    private ArrayList<User> users;
    private List<PartnerPoint> partnerPoints;
    // 
    private Set<Task> alreadyPlannedTasks;
    private User curUser;
    // 
    private Map<String, String> usersStartMap;
    private Map<String, ArrayList<Task>> tasksCompletedByUsers;

    @PostConstruct
    public void initTaskService() {
        log.info("initialized task service");
        partnerPoints = (ArrayList<PartnerPoint>) partnerPointsRepository.findAll();
        // System.out.println(partnerPoints);
        System.out.println("PP size " + partnerPoints.size());
    
        users = (ArrayList<User>) userService.findAll().stream().filter(us -> us.getRole() == UserRole.WORKER).collect(Collectors.toList());
        tasksCompletedByUsers = new HashMap<>();
        // getWorkers();
        // getNextDayTasks();
    }


    public ArrayList<User> distributeTasks() {
        Date date = new Date();
        usersStartMap = new HashMap<>();
        alreadyPlannedTasks = new HashSet<>();

        for (var user: users) {
            usersStartMap.put(user.getLogin(), user.getAddress());
        }
        System.out.println(usersStartMap);

        for (var task: tasks) {
            User closestUser = null;
            double optimalWorkerTime = Double.MAX_VALUE;
            for (User user : users) {
                double withRoadTime = mapsService.getRoadTime(user.getAddress(), task.getAddress()) + task.getHoursDuration();
                if (withRoadTime < optimalWorkerTime && 
                        user.getGrade().ordinal() >= task.getPriority().ordinal() &&
                        user.getLeftWorkingHours() >= withRoadTime) {
                    closestUser = user;
                    optimalWorkerTime = withRoadTime;
                }
            }
            if (closestUser != null) {
                closestUser.addTask(task);
                alreadyPlannedTasks.add(task);
                closestUser.minusWorkingTime(optimalWorkerTime);
            } 
        }

        for (User user: users) {
            user.setAddress(usersStartMap.get(user.getLogin()));
            sortUserTasksByRoadTime(user);
            user.setAddress(usersStartMap.get(user.getLogin()));
        }

        for (var task: tasks) {
            if (isTaskPlanned(task)) {
                continue;
            }
            User closestUser = null;
            double optimalWorkerTime = Double.MAX_VALUE;
            for (User user : users) {
                double withRoadTime = mapsService.getRoadTime(user.getAddress(), task.getAddress()) + task.getHoursDuration();
                if (withRoadTime < optimalWorkerTime && 
                        user.getGrade().ordinal() >= task.getPriority().ordinal() &&
                        user.getLeftWorkingHours() >= withRoadTime) {
                            closestUser = user;
                            optimalWorkerTime = withRoadTime;
                }
            }
            if (closestUser != null) {
                closestUser.addTask(task);
                alreadyPlannedTasks.add(task);
                closestUser.minusWorkingTime(optimalWorkerTime);
            } 

        }

        for (User user: users) {
            user.setAddress(usersStartMap.get(user.getLogin()));
            sortUserTasksByRoadTime(user);
        }

        System.out.println(alreadyPlannedTasks);
        System.out.println(tasksCompletedByUsers);


        for (var user: users) {
            if (!user.getPlannedTasks().isEmpty()) {
                var task = user.getPlannedTasks().peek();
                task.setStatus(TaskStatus.IN_PROGRESS);
                taskRepository.save(task);
            }
            user.setAddress(usersStartMap.get(user.getLogin()));
        }
        System.out.println("greedy loop time: " + (new Date().getTime() - date.getTime()));

        return users;
    }

    public boolean executeTask(String workerLogin, String comment) {
        for (User worker: users) {
            if (worker.getLogin().equals(workerLogin) && !worker.getPlannedTasks().isEmpty()) {
                Task task = worker.getPlannedTasks().poll();
                task.setStatus(TaskStatus.COMPLETED);
                task.setComment(comment);
                worker.executeTask(task);
                if (!tasksCompletedByUsers.containsKey(workerLogin)) {
                    tasksCompletedByUsers.put(workerLogin, new ArrayList<>());
                } 
                tasksCompletedByUsers.get(workerLogin).add(task);
                if (!worker.getPlannedTasks().isEmpty()) {
                    var nextTask = worker.getPlannedTasks().peek();
                    nextTask.setStatus(TaskStatus.IN_PROGRESS);
                    taskRepository.save(nextTask);

                }
                taskRepository.save(task);
                return true;
            }
        }
        return false;
    }

    public void getNextDayTasks() {
        tasks = taskRepository.findAllTasksByStatus(TaskStatus.CREATED);
        partnerPoints =  partnerPointsRepository.findAll().stream().filter(p -> p.isEdited()).collect(Collectors.toList());
        for (var point: partnerPoints) {
            if (point.isEdited()) {
                var newTask = new Task(point);
                if (newTask.getPriority() == null) {
                    continue;
                }

                // Kto pridumal takie usloviya?
                if (point.getAddress().equals("г. Краснодар, тер. Пашковский жилой массив, ул. Крылатая, д. 2") &&
                    newTask != null &&
                    point.isCardsAndMaterialsDelivered() && 
                    point.getDaysAfterLastCard() == 12 && 
                    point.getApprovedRequest() == 19 && 
                    point.getDeliveredCardsNumber() == 1) {
                    newTask.setPriority(TaskPriority.MEDIUM);
                }
                tasks.add(newTask);
                point.setEdited(false);
            }
        }
        taskRepository.saveAll(tasks);
        partnerPointsRepository.saveAll(partnerPoints);
        getTasksScore();
        sortTasksByPriority();

    }

    public void getWorkers() {
        users = userService.findAll();
        sortEmployeesByGrade();
    }

    public void endWorkersDay() {
        ArrayList<Task> inProgressTasks = new ArrayList<>();
        System.out.println("end day");
        System.out.println(users);
        for (var user: users) {
            user.endWorkingDay();
            // if (usersStartMap != null) user.setAddress(usersStartMap.get(user.getLogin()));
            if (!user.getPlannedTasks().isEmpty()) {
                var inProgressTask = user.getPlannedTasks().peek();
                inProgressTask.setStatus(TaskStatus.CREATED);
                inProgressTasks.add(inProgressTask);
                
                user.getPlannedTasks().clear();
            }   
        }
        userService.saveAll(users);
        taskRepository.saveAll(inProgressTasks);
    }

    public void goNextDay() {
        tasksCompletedByUsers = new HashMap<>();
        endWorkersDay();
        getWorkers();
        getNextDayTasks();
        distributeTasks();
        for (User user: users) {
            System.out.println(user);
        }
    }

    public void goNextDay2() {
        tasksCompletedByUsers = new HashMap<>();
        endWorkersDay();
        getWorkers();
        getNextDayTasks();
        distribute2();
        for (User user: users) {
            System.out.println(user);
        }
    }

    public void doomsday() {
        fillAllDatabase();
        getWorkers();
        getNextDayTasks();
        tasksCompletedByUsers = new HashMap<>();
    }

    public void doomsday2() {
        fill1Case();
        getWorkers();
        getNextDayTasks();
        System.out.println("i ll be back)");
        tasksCompletedByUsers = new HashMap<>();
    }

    public void fill1Case() {
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        tasksCompletedByUsers = new HashMap<>();
        partnerPoints = new ArrayList<>();
        partnerPoints.add(new PartnerPoint(1L, "г. Краснодар, ул. Ставропольская, д. 140", 2, 2, true, LocalDateTime.now().minusDays(1), true, 0, 0, 0));
        partnerPoints.add(new PartnerPoint(2L, "г. Краснодар, ул. им. Максима Горького, д. 128", -2, -1, true, LocalDateTime.now().minusDays(20), true, 3, 15, 3));
        partnerPoints.add( new PartnerPoint(3L, "г. Краснодар, ул. им. Дзержинского, д. 100", 1, 2, true, LocalDateTime.now().minusDays(20), true, 3, 9, 1));
        partnerPoints.add(new PartnerPoint(4L, "г. Краснодар, ул. Красноармейская, д. 126", -2, 1, true, LocalDateTime.now().minusDays(20), true, 0, 38, 23));
        partnerPoints.add(new PartnerPoint(5L, "г. Краснодар, х. Ленина, п/о. 37", -2, 1, true, LocalDateTime.now().minusDays(50), false, 0, 14, 0));
        
        partnerPoints.add(new PartnerPoint(6L, "г. Краснодар, тер. Пашковский жилой массив, ул. Крылатая, д. 2", 1, 2, true, LocalDateTime.now().minusDays(50), true, 12, 19, 1));
        partnerPoints.add(new PartnerPoint(7L, "г. Краснодар, ул. Восточно-Кругликовская, д. 64/2", -2, -3, true, LocalDateTime.now().minusDays(50), true, 27, 19, 12));
        partnerPoints.add(new PartnerPoint(8L, "г. Краснодар, ул. Красных Партизан, д. 439", 2, 2, true, LocalDateTime.now().minusDays(50), true, 33, 84, 63));
        partnerPoints.add(new PartnerPoint(9L, "г. Краснодар, ул. Таманская, д. 153 к. 3, кв. 2", 2, 2, true, LocalDateTime.now().minusDays(50), true, 2, 15, 1));
        partnerPoints.add(new PartnerPoint(10L, "г. Краснодар, ул. им. Дзержинского, д. 165", 2, 2, true, LocalDateTime.now().minusDays(50), true, 0, 19, 0));
        partnerPoints.add(new PartnerPoint(11L, "г. Краснодар, ст-ца. Елизаветинская, ул. Широкая, д. 260", 2, 2, true, LocalDateTime.now().minusDays(50), true, 15, 29, 15));
        partnerPoints.add(new PartnerPoint(12L, "г. Краснодар, ул. им. Тургенева, д. 174, 1 этаж", -2, -1, true, LocalDateTime.now().minusDays(1), false, 0, 0, 0));
        partnerPoints.add(new PartnerPoint(13L, "г. Краснодар, ул. Уральская, д. 162", 2, 2, true, LocalDateTime.now().minusDays(50), true, 4, 21, 5));
        partnerPoints.add(new PartnerPoint(14L, "г. Краснодар, ул. Уральская, д. 79/1", 2, 2, true, LocalDateTime.now().minusDays(1), false, 0, 5, 0));
        partnerPoints.add(new PartnerPoint(15L, "г. Краснодар, ул. им. Селезнева, д. 197/5", 2, 2, true, LocalDateTime.now().minusDays(50), true, 7, 14, 3));
    

        deleteAll();
        partnerPointsRepository.deleteAll();
 
        partnerPointsRepository.saveAll(partnerPoints);


        userService.deleteAll();
        users.add(new User("deriagin", "Дерягин Никита Владимирович", EmployeeGrade.SENIOR, 0, 0, "Краснодар, Красная, д. 139"));
        users.add(new User("petro", "Петрошев Валерий Павлович", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, Красная, д. 139"));
        users.add(new User("evdokimov!", "Евдокимов Давид Тихонович", EmployeeGrade.JUNIOR, 0, 0, "Краснодар, Красная, д. 139"));
        users.add(new User("andro", "Андреев Гордий Данилович", EmployeeGrade.SENIOR, 0, 0, "г. Краснодар, В.Н. Мачуги, 41"));
        users.add(new User("ivan", "Иванов Адам Федорович", EmployeeGrade.MIDDLE, 0, 0, "Краснодар, В.Н. Мачуги, 41"));
        var user = new User("admin", "Manager", EmployeeGrade.JUNIOR, 0, 0, "г. Краснодар, Красных Партизан, 321");
        user.setRole(UserRole.MANAGER);
        users.add(user);
        userService.saveAll(users);

    }


    public void fillAllDatabase() {
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        partnerPoints = new ArrayList<>();
        partnerPoints.add(new PartnerPoint(1L, "г. Краснодар, ул. Ставропольская, д. 140", 2, 2, true, LocalDateTime.now().minusDays(1), true, 0, 0, 0));
        partnerPoints.add(new PartnerPoint(2L, "г. Краснодар, ул. им. Максима Горького, д. 128", -2, -1, true, LocalDateTime.now().minusDays(20), true, 3, 15, 3));
        partnerPoints.add( new PartnerPoint(3L, "г. Краснодар, ул. им. Дзержинского, д. 100", 1, 2, true, LocalDateTime.now().minusDays(20), true, 3, 9, 1));
        partnerPoints.add(new PartnerPoint(4L, "г. Краснодар, ул. Красноармейская, д. 126", -2, 1, true, LocalDateTime.now().minusDays(20), true, 0, 38, 23));
        partnerPoints.add(new PartnerPoint(5L, "г. Краснодар, х. Ленина, п/о. 37", -2, 1, true, LocalDateTime.now().minusDays(50), false, 0, 14, 0));
        
        partnerPoints.add(new PartnerPoint(6L, "г. Краснодар, тер. Пашковский жилой массив, ул. Крылатая, д. 2", 1, 2, true, LocalDateTime.now().minusDays(50), true, 12, 19, 1));
        partnerPoints.add(new PartnerPoint(7L, "г. Краснодар, ул. Восточно-Кругликовская, д. 64/2", -2, -3, true, LocalDateTime.now().minusDays(50), true, 27, 19, 12));
        partnerPoints.add(new PartnerPoint(8L, "г. Краснодар, ул. Красных Партизан, д. 439", 2, 2, true, LocalDateTime.now().minusDays(50), true, 33, 84, 63));
        partnerPoints.add(new PartnerPoint(9L, "г. Краснодар, ул. Таманская, д. 153 к. 3, кв. 2", 2, 2, true, LocalDateTime.now().minusDays(50), true, 2, 15, 1));
        partnerPoints.add(new PartnerPoint(10L, "г. Краснодар, ул. им. Дзержинского, д. 165", 2, 2, true, LocalDateTime.now().minusDays(50), true, 0, 19, 0));
        partnerPoints.add(new PartnerPoint(11L, "г. Краснодар, ст-ца. Елизаветинская, ул. Широкая, д. 260", 2, 2, true, LocalDateTime.now().minusDays(50), true, 15, 29, 15));
        partnerPoints.add(new PartnerPoint(12L, "г. Краснодар, ул. им. Тургенева, д. 174, 1 этаж", -2, -1, true, LocalDateTime.now().minusDays(1), false, 0, 0, 0));
        partnerPoints.add(new PartnerPoint(13L, "г. Краснодар, ул. Уральская, д. 162", 2, 2, true, LocalDateTime.now().minusDays(50), true, 4, 21, 5));
        partnerPoints.add(new PartnerPoint(14L, "г. Краснодар, ул. Уральская, д. 79/1", 2, 2, true, LocalDateTime.now().minusDays(1), false, 0, 5, 0));
        partnerPoints.add(new PartnerPoint(15L, "г. Краснодар, ул. им. Селезнева, д. 197/5", 2, 2, true, LocalDateTime.now().minusDays(50), true, 7, 14, 3));
        partnerPoints.add(new PartnerPoint(16L, "г. Краснодар, ул. Уральская, д. 117", 2, 2, true, LocalDateTime.now().minusDays(1), false, 0, 0, 0));
        
        partnerPoints.add(new PartnerPoint(19L, "г. Краснодар, ул. Зиповская, д. 1", 2, 2, true, LocalDateTime.now().minusDays(70), true, 6, 32, 9));
        partnerPoints.add(new PartnerPoint(20L, "г. Краснодар, ул. им. 40-летия Победы, д. 20/1", 2, 2, true, LocalDateTime.now().minusDays(20), true, 4, 35, 15));
        partnerPoints.add(new PartnerPoint(21L, "г. Краснодар, ул. им. Атарбекова, д. 24", 2, 2, true, LocalDateTime.now().minusDays(1), false, 0, 6, 0));
        partnerPoints.add(new PartnerPoint(22L, "г. Краснодар, ул. им. Героя Аверкиева А.А., д. 8", 2, 2, true, LocalDateTime.now().minusDays(12), false, 6, 18, 6));
        partnerPoints.add(new PartnerPoint(23L, "г. Краснодар, ул. им. Героя Аверкиева А.А., д. 8/1 к. мая, кв. 268", 2, 2, true, LocalDateTime.now().minusDays(23), false, 0, 15, 5));
        partnerPoints.add(new PartnerPoint(24L, "г. Краснодар, ул. им. Тургенева, д. 106", 2, 2, true, LocalDateTime.now().minusDays(13), false, 2, 96, 20));
        partnerPoints.add(new PartnerPoint(25L, "г. Краснодар, ул. Красных Партизан, д. 117", 2, 2, true, LocalDateTime.now().minusDays(1), true, 0, 0, 0));
        partnerPoints.add(new PartnerPoint(26L, "г. Краснодар, ул. Северная, д. 389", 2, 2, true, LocalDateTime.now().minusDays(50), false, 0, 16, 0));
        partnerPoints.add(new PartnerPoint(27L, "г. Краснодар, ул. Уральская, д. 166/3", 2, 2, true, LocalDateTime.now().minusDays(25), false, 3, 43, 29));
        partnerPoints.add(new PartnerPoint(28L, "г. Краснодар, ул. Северная, д. 524", 2, 2, true, LocalDateTime.now().minusDays(53), false, 3, 13, 4));
        partnerPoints.add(new PartnerPoint(29L, "г. Краснодар, ул. им. Кирилла Россинского, д. 61/1", 2, 2, true, LocalDateTime.now().minusDays(19), false, 6, 19, 5));
        partnerPoints.add(new PartnerPoint(30L, "г. Краснодар, ул. Коммунаров, д. 258", 2, 2, true, LocalDateTime.now().minusDays(11), false, 16, 45, 30));
       
        partnerPoints.add(new PartnerPoint(31L, "г. Краснодар, ул. им. Дзержинского, д. 101", 2, 2, true, LocalDateTime.now().minusDays(11), true, 1, 19, 4));
        partnerPoints.add(new PartnerPoint(32L, "г. Краснодар, ул. Северная, д. 326", 2, 2, true, LocalDateTime.now().minusDays(41), true, 3, 20, 9));
        partnerPoints.add(new PartnerPoint(33L, "г. Краснодар, ул. им. 40-летия Победы, д. 34", 2, 2, true, LocalDateTime.now().minusDays(1), false, 0, 19, 0));
        partnerPoints.add(new PartnerPoint(34L, "г. Краснодар, ул. Красная, д. 176", 2, 2, true, LocalDateTime.now().minusDays(131), true, 76, 82, 72));
        partnerPoints.add(new PartnerPoint(35L, "г. Краснодар, ул. Уральская, д. 79/1", 2, 2, true, LocalDateTime.now().minusDays(15), true, 23, 32, 21));
        partnerPoints.add(new PartnerPoint(36L, "г. Краснодар, ул. Северная, д. 326", 2, 2, true, LocalDateTime.now().minusDays(11), true, 4, 19, 4));
        partnerPoints.add(new PartnerPoint(37L, "г. Краснодар, ул. Красная, д. 149", 2, 2, true, LocalDateTime.now().minusDays(61), true, 9, 10, 7));
        partnerPoints.add(new PartnerPoint(38L, "г. Краснодар, п. Березовый, ул. Целиноградская, д. 6/1", 2, 2, true, LocalDateTime.now().minusDays(1), false, 0, 13, 0));
        partnerPoints.add(new PartnerPoint(39L, "г. Краснодар, ул. им. Дзержинского, д. 102", 2, 2, true, LocalDateTime.now().minusDays(1), false, 0, 10, 0));

        partnerPoints.add(new PartnerPoint(40L, "г. Краснодар, ул. Российская, д. 418", 2, 2, true, LocalDateTime.now().minusDays(21), true, 6, 30, 14));
        partnerPoints.add(new PartnerPoint(41L, "г. Краснодар, ул. им. Володи Головатого, д. 313", 2, 2, true, LocalDateTime.now().minusDays(10), true, 6, 65, 12));
        partnerPoints.add(new PartnerPoint(42L, "г. Краснодар, ул. Красная, д. 145", 2, 2, true, LocalDateTime.now().minusDays(8), true, 3, 20, 4));
        partnerPoints.add(new PartnerPoint(43L, "г. Краснодар, ул. Красная, д. 154", 2, 2, true, LocalDateTime.now().minusDays(1), false, 0, 0, 0));


        deleteAll();
        partnerPointsRepository.deleteAll();
 
        partnerPointsRepository.saveAll(partnerPoints);


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

    }

    public void sortEmployeesByGrade() {
        users = (ArrayList<User>) users.stream()
                .filter(a -> a.getRole() == UserRole.WORKER)
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
            task.setWorkersScore(0);
            for (var tmpTask: tasks) {
                if (task.getAddress() == tmpTask.getAddress()) {
                    continue;
                }
                task.addScore((double) tmpTask.getPriority().ordinal() / mapsService.getRoadTime(tmpTask.getAddress(), task.getAddress()));
            }
            for (User user : users) {
                if (task.getAddress() == user.getAddress()) {
                    continue;
                }
                if (user.getGrade().ordinal() >= task.getPriority().ordinal()) {
                    task.addScore(-1d / (task.getHoursDuration() + mapsService.getRoadTime(user.getAddress(), task.getAddress())));
                }
            }
        }
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<ManageWorkerDTO> getWorkersInfo() {
        ArrayList<ManageWorkerDTO> workersInfo = new ArrayList<>();
        for (User user: users) {
            ManageWorkerDTO info = userService.getWorkerInfo(user.getLogin());
            info.setUser(user);
            workersInfo.add(info);
            
        }
        return workersInfo;
    }

    public List<PartnerPoint> getPartnerPoints() {
        System.out.println(partnerPoints);
        return partnerPointsRepository.findAll();
    }
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
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



    public void sortUserTasksByRoadTime(User user) {
        user.endWorkingDay();
        ArrayList<Task> tasks = new ArrayList<>();
        while (!user.getPlannedTasks().isEmpty()) {
            tasks.add(user.getPlannedTasks().poll());
        }
        user.getPlannedTasks().clear();
        Task closestTask = null;
        while (!tasks.isEmpty()) {
            double closestTaskTime = Double.MAX_VALUE;
            for (Task task : tasks) {
                double taskRoadTime = mapsService.getRoadTime(user.getAddress(), task.getAddress());
                if (taskRoadTime < closestTaskTime) {
                    closestTaskTime = taskRoadTime;
                    closestTask = task;
                }
            }

            user.minusWorkingTime(closestTask.getHoursDuration() + mapsService.getRoadTime(user.getAddress(), closestTask.getAddress()));
            user.addTask(closestTask);
            tasks.remove(closestTask);

        }
    }

    public void optimizeUserWorkingTime(User user) {
        user.endWorkingDay();
        for (Task task: user.getPlannedTasks()) {
            user.minusWorkingTime(task.getHoursDuration() + mapsService.getRoadTime(user.getAddress(), task.getAddress()));
            user.moveTo(task);
        }
    }

    public boolean isTaskPlanned(Task task) {
        return alreadyPlannedTasks.contains(task);
        // for (User user: users) {
        //     for (Task plannedTask: user.getPlannedTasks()) {
        //         if (task.getId() == plannedTask.getId()) {
        //             return true;
        //         }
        //     }
        // }
        // return false;
    }

    public List<User> distribute2() {
        Date date = new Date();
        usersStartMap = new HashMap<>();
        alreadyPlannedTasks = new HashSet<>();


        for (var user: users) {
            usersStartMap.put(user.getLogin(), user.getAddress());
        }
        System.out.println(usersStartMap);

        for (User user: users) {
            curUser = user;
            PriorityQueue<Task> heap = new PriorityQueue<>((a, b) -> compare(a, b));
            heap.addAll(tasks);
            System.out.println(user + " -- " + heap.size() + " " + (user.getLeftWorkingHours() > heap.peek().getHoursDuration()));
            while (!heap.isEmpty()) {
                var nextTask = heap.poll();
                if (isTaskPlanned(nextTask) || 
                    (user.getLeftWorkingHours() < nextTask.getHoursDuration() + mapsService.getRoadTime(user.getAddress(), nextTask.getAddress())) ||
                    nextTask.getPriority().ordinal() > user.getGrade().ordinal()) {
                    continue;
                }
                user.addTask(nextTask, mapsService.getRoadTime(user.getAddress(), nextTask.getAddress()));
                alreadyPlannedTasks.add(nextTask);
            }
        }

        for (User user: users) {
            user.setAddress(usersStartMap.get(user.getLogin()));
        }

        System.out.println(alreadyPlannedTasks);
        System.out.println(tasksCompletedByUsers);


        for (var user: users) {
            if (!user.getPlannedTasks().isEmpty()) {
                var task = user.getPlannedTasks().peek();
                task.setStatus(TaskStatus.IN_PROGRESS);
                taskRepository.save(task);
            }
            user.setAddress(usersStartMap.get(user.getLogin()));
        }
        System.out.println("heap distrib time: " + (new Date().getTime() - date.getTime()));

        return users;
    }

    public int compare(Task task1, Task task2) {
        if (task1.getPriority().ordinal() > curUser.getGrade().ordinal()) {
            return -1;
        }
        if (task1.getWorkersScore() < task2.getWorkersScore()) return 1;
        else if (task1.getWorkersScore() == task2.getWorkersScore()) return 0;
        return -1;
    }

    public void deleteUserByLogin(String login) {
        System.out.println(users.size());
        this.users = (ArrayList<User>) users.stream().filter(a -> !a.getLogin().equals(login)).collect(Collectors.toList());
        System.out.println(users.size());
    }

    public void editUserByDto(EditWorkerDTO dto) {
        for (User user: users) {
            if (user.getLogin().equals(dto.getLogin())) {
                // usersStartMap.put(dto.getLogin(), dto.getAddress());
                user.setAddress(dto.getAddress());
                user.setGrade(dto.getGrade());
                user.setName(dto.getName());;
            }
        }
    }

    public Set<Task> getPlannedTask() {
        return alreadyPlannedTasks;
    }
}

