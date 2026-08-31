package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.VacationRequestDto;
import com.TimeAway.demo.entity.Employee;
import com.TimeAway.demo.entity.VacationRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelReportServiceImpl implements ExcelReportService {
    @Override
    public ByteArrayInputStream exportEmployeesToExcel(List<VacationRequestDto> vacationRequests) {
        System.setProperty("java.awt.headless", "true");
        // SXSSFWorkbook(100) -> keeps only 100 rows in RAM
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Optimization: compress the temporary XML files on disk
            workbook.setCompressTempFiles(true);

            Sheet sheet = workbook.createSheet("Rapport des employees");

            // 1. Create Styles (Header and Date)
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd-mm-yyyy"));

            // 2. Create Header Row
            String[] columns = {"date debut", "date fin","nom employe", "type",
                    "nombre de jour"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 3. Fill Data
           int rowIdx = 1;
            for (VacationRequestDto vacationRequest : vacationRequests) {
                Row row = sheet.createRow(rowIdx++);

                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(vacationRequest.getFromDate());
                dateCell.setCellStyle(dateStyle);
                String fullName =vacationRequest.getEmployeeDTO().getFirstName()
                        + " " + vacationRequest.getEmployeeDTO().getLastName();
                Cell dateCell2 = row.createCell(1);
                dateCell2.setCellValue(vacationRequest.getToDate());
                dateCell2.setCellStyle(dateStyle);
                row.createCell(2).setCellValue(fullName);
                row.createCell(3).setCellValue(vacationRequest.getReason());
                row.createCell(4).setCellValue(vacationRequest.getNumberOfDays().doubleValue());

            }
            // 4. Set Fixed Column Widths (Mandatory for SXSSF as autoSize is slow/restricted)
            for (int i = 0; i < columns.length; i++) {
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(out);

            // IMPORTANT: Delete temporary files from disk
            workbook.dispose();

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erreur génération Excel: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN); // Add borders to make it look like a header instead
        return style;
    }
    private void setSafeStringValue(Row row, int cellIndex, String value) {
        row.createCell(cellIndex).setCellValue(value != null ? value : "");
    }
}
