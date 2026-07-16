package com.TimeAway.demo.dto;

import com.TimeAway.demo.entity.Employee;
import com.TimeAway.demo.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class VacationRequestDto {
    private Long id;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private RequestStatus status;
    private EmployeeDTO employeeDTO;
    private LocalDateTime submittedAt = LocalDateTime.now();
    private Boolean approvedByResponsible;
    private BigDecimal numberOfDays =  BigDecimal.ZERO;
    private String halfDayType = "FULL_DAY";

}
