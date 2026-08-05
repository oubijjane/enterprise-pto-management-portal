package com.TimeAway.demo.service;

import com.TimeAway.demo.entity.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class ExcelReportServiceImpl implements ExcelReportService {
    @Override
    public ByteArrayInputStream exportEmployeesToExcel(List<Employee> employees) {
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
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd-mm-yyyy hh:mm:ss"));

            // 2. Create Header Row
            String[] columns = {"Non & Prénom", "Libelle","Matricule", "Companie",
                    "Destination", "Transport", "Status", "Commentaire",
                    "dernière mise à jour","Numéro de commande", "Num Dossier"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 3. Fill Data
           /* int rowIdx = 1;
            for (Employee employee : employees) {
                Row row = sheet.createRow(rowIdx++);

                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(employee.getCreatedAt());
                dateCell.setCellStyle(dateStyle);
                String fullName =employee.getLastName() + employee.getFirstName();
                row.createCell(1).setCellValue(fullName);
                row.createCell(2).setCellValue(employee.getRegistrationNumber());
                row.createCell(3).setCellValue(employee.getCompany().getCompanyName());
                row.createCell(4).setCellValue(employee.getCity() != null ? employee.getCity().getCityName() : "");

                setSafeStringValue(row,5, (employee.getTransitCompany() != null ? employee.getTransitCompany().getName() : ""));
                row.createCell(6).setCellValue(employee.getStatus().getLabel());
                setSafeStringValue(row,7, employee.getComment());
                Cell dataCell2 = row.createCell(8);
                dataCell2.setCellValue(employee.getUpdatedAt());
                dataCell2.setCellStyle(dateStyle);
                row.createCell(9).setCellValue(employee.getId());
                row.createCell(10).setCellValue(employee.getFileNumber());
            }
*/
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
