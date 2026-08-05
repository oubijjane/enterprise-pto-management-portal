package com.TimeAway.demo.service;

import com.TimeAway.demo.entity.Employee;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface ExcelReportService {
    ByteArrayInputStream exportEmployeesToExcel(List<Employee> employees);
}
