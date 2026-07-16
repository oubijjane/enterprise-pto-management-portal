package com.TimeAway.demo.dto;

import jakarta.persistence.Column;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String loginName;
    private String email;
    private String password;
    private List<RoleDTO> role = new ArrayList<>();
    private String phone;
    private BigDecimal thisYearVacationDays;
    private BigDecimal lastYearVacationDays;
    private BigDecimal usedVacationDays;
    private BigDecimal lastYearUsedVacationDays;
    private BigDecimal accrualRatePerMonth;
    private LocalDate hiringDate;
    private LocalDate lastAccrualDate;
    private BigDecimal nextYearVacationDays;
    private Integer lastRolloverYear;
    private BigDecimal remainingVacationDays;
    private List<BackupDto> coverageTeam = new ArrayList<>();
    private DepartmentDTO departmentDTO;


    public record BackupDto(Integer id, String firstName, String lastName) {}

}
