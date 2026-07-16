package com.TimeAway.demo.dto;

import com.TimeAway.demo.entity.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DepartmentDTO {


    private Long id;


    private String name;


    private EmployeeDTO responsible;
}
