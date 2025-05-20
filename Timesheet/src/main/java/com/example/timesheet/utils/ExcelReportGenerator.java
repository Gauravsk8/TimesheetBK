package com.example.timesheet.utils;

import com.example.timesheet.models.DailyTimeSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelReportGenerator {

    public static void generateExcel(String baseDir, String monthLabel, String projectName, String managerName,
                                     String employeeName, List<DailyTimeSheet> entries) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Timesheet");

        // Add header info
        int rowIdx = 0;
        sheet.createRow(rowIdx++).createCell(0).setCellValue("Project Name");
        sheet.getRow(rowIdx - 1).createCell(1).setCellValue(projectName);

        sheet.createRow(rowIdx++).createCell(0).setCellValue("Project Manager");
        sheet.getRow(rowIdx - 1).createCell(1).setCellValue(managerName);

        sheet.createRow(rowIdx++).createCell(0).setCellValue("Month");
        sheet.getRow(rowIdx - 1).createCell(1).setCellValue(monthLabel);

        rowIdx++; // empty row

        sheet.createRow(rowIdx++).createCell(0).setCellValue("Name");
        sheet.getRow(rowIdx - 1).createCell(1).setCellValue(employeeName);

        // Table header
        Row header = sheet.createRow(rowIdx++);
        String[] columns = {"S.No", "Date", "Days", "Task Description", "Time Worked (in Hr)"};
        for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

        int count = 1;
        for (DailyTimeSheet dts : entries) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(count++);
            LocalDate date = dts.getWorkDate().toLocalDate();
            row.createCell(1).setCellValue(date.toString());
            row.createCell(2).setCellValue(date.format(dayFormatter));
            row.createCell(3).setCellValue(dts.getDescription());
            row.createCell(4).setCellValue(dts.getHoursSpent());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Create folders
        String folderPath = baseDir + "/" + monthLabel + "/" + projectName;
        new File(folderPath).mkdirs();

        String fileName = folderPath + "/" + employeeName.replace(" ", "_") + "_" + monthLabel + ".xlsx";
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            workbook.write(fos);
        }

        workbook.close();
    }
}

