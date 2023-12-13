package com.example.services;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.apache.poi.sl.draw.geom.Path;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.dto.ManageWorkerDTO;
import com.example.entities.Task;
import com.example.enums.TaskPriority;

import jakarta.annotation.PostConstruct;

@Component
public class ApachePOIExcelWrite {

    @Autowired
    private UserService userService;

    private HashMap<TaskPriority, String> priorityDecode;

    @PostConstruct
    public void initApacheExcelWriter() {
        priorityDecode = new HashMap<>();
        priorityDecode.put(TaskPriority.LOW, "Доставка карт и материалов");
        priorityDecode.put(TaskPriority.MEDIUM, "Обучение агента");
        priorityDecode.put(TaskPriority.HIGH, "Выезд на точку для стимулирования выдач");
    }


    public String generateExcelFile(String login) throws IOException {

        String FILE_NAME = "/servdata/" + login + ".xlsx";
        // String FILE_NAME = "src/main/resources/excel/" + login + ".xlsx";
        java.nio.file.Path path = Paths.get(FILE_NAME);
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ManageWorkerDTO info = userService.getWorkerInfo(login);



        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Сотрудник");
        sheet.setDefaultColumnWidth(30);

        int rowNum = 0;
        System.out.println("Creating excel");
        // WRITE NAME
        Row row = sheet.createRow(rowNum++);
        int colNum = 0;
        Cell cell = row.createCell(colNum++);
        cell.setCellValue("Имя");
        cell = row.createCell(colNum++);
        cell.setCellValue(info.getUser().getName());

        // WRITE ADDRESS
        row = sheet.createRow(rowNum++);
        colNum = 0;
        cell = row.createCell(colNum++);
        cell.setCellValue("Адрес");
        cell = row.createCell(colNum++);
        cell.setCellValue(info.getUser().getAddress());

        // WRITE HIGH
        row = sheet.createRow(rowNum++);
        colNum = 0;
        cell = row.createCell(colNum++);
        cell.setCellValue("Высокий приоритет");
        cell = row.createCell(colNum++);
        cell.setCellValue(info.getHighNumber());

        // WRITE MEDIUM
        row = sheet.createRow(rowNum++);
        colNum = 0;
        cell = row.createCell(colNum++);
        cell.setCellValue("Средний приоритет");
        cell = row.createCell(colNum++);
        cell.setCellValue(info.getMediumNumber());

        // WRITE HIGH
        row = sheet.createRow(rowNum++);
        colNum = 0;
        cell = row.createCell(colNum++);
        cell.setCellValue("Низкий приоритет");
        cell = row.createCell(colNum++);
        cell.setCellValue(info.getLowNumber());


        sheet = workbook.createSheet("Выполненные задачи");
        sheet.setDefaultColumnWidth(30);
        rowNum = 1;
        colNum = 0;
        row = sheet.createRow(rowNum++);
        
        cell = row.createCell(colNum++);
        cell.setCellValue("ID");
        cell = row.createCell(colNum++);
        cell.setCellValue("Адрес");
        cell = row.createCell(colNum++);
        cell.setCellValue("Название");
        cell = row.createCell(colNum++);
        cell.setCellValue("Приоритет");
        cell = row.createCell(colNum++);
        cell.setCellValue("Дата выполнения");
        cell = row.createCell(colNum++);
        cell.setCellValue("Комментарий");

        for (Task task: info.getCompletedTasks()) {
            colNum = 0;
            row = sheet.createRow(rowNum++);
            cell = row.createCell(colNum++);
            cell.setCellValue(task.getId());
            cell = row.createCell(colNum++);
            cell.setCellValue(task.getAddress());
            cell = row.createCell(colNum++);
            cell.setCellValue(priorityDecode.get(task.getPriority()));
            cell = row.createCell(colNum++);
            cell.setCellValue(task.getPriority().toString());
            cell = row.createCell(colNum++);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            cell.setCellValue(task.getExecuted().format(formatter));
            cell = row.createCell(colNum++);
            cell.setCellValue(task.getComment());
        }

        


        try {
            FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
            workbook.write(outputStream);
            workbook.close();
            return FILE_NAME;
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("not correct file");
        return "";
    }
}
