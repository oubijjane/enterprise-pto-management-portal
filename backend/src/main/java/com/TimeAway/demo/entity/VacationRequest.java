package com.TimeAway.demo.entity;

import com.TimeAway.demo.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Setter
@Getter
public class VacationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference(value = "vacationRequest-employee")
    private Employee employee;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    private String reason;

    @Column
    private Boolean approvedByResponsible;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    @Column
    private BigDecimal numberOfDays;

    @Column(nullable = false)
    private String halfDayType = "FULL_DAY";

    @Version
    private Long version;

    /*
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
     */

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    /*public BigDecimal getNumberOfDays() {
        double days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        return BigDecimal.valueOf(days);
    }*/
}
