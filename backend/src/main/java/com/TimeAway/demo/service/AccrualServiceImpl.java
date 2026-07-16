package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.EmployeeDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class AccrualServiceImpl implements AccrualService {
    final private EmployeeService employeeService;

    public AccrualServiceImpl(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Casablanca")
    @Transactional
    public void runMonthlyAccrual() {

        List<EmployeeDTO> employeeDTOS = employeeService.getAllEmployees();
        LocalDate now = LocalDate.now();

        for (EmployeeDTO emp : employeeDTOS) {
            LocalDate lastDate = emp.getLastAccrualDate();


            // 1. Determine the target date for their NEXT accrual
            LocalDate targetDate = (lastDate == null) ? now : lastDate.plusMonths(1);

            // 2. Calculate the Annual Cap: Accrual Rate * 12
            BigDecimal annualCap = emp.getAccrualRatePerMonth().multiply(new BigDecimal("12"));

            // 3. The Catch-Up Loop
            boolean accrualProcessed = false;

            while (!targetDate.isAfter(now)) {
                // Calculate what the balance WOULD be
                BigDecimal proposedBalance = emp.getNextYearVacationDays().add(emp.getAccrualRatePerMonth());

                // Enforce the limit: Take the smaller of the proposed balance or the cap
                BigDecimal newBalance = proposedBalance.min(annualCap);

                emp.setNextYearVacationDays(newBalance);

                // Advance the target date by exactly 1 month
                emp.setLastAccrualDate(targetDate);

                // Prepare for the next loop iteration
                targetDate = targetDate.plusMonths(1);
                accrualProcessed = true;

            }

            // 4. Only save if an update actually happened
            if (accrualProcessed) {
                employeeService.updateNextYearVacationDays(emp);
                System.out.println("Ledger synchronized up to date for " + emp.getFirstName());
            }
        }
    }

// Run every single day at midnight
    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Casablanca")
    @Transactional
    public void processMidYearRolloverSafe() {
        // 1. Get the current date in your specific timezone
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Casablanca"));
        int currentYear = today.getYear();

        // 2. Define the threshold: June 30th of the current year
        LocalDate rolloverThreshold = LocalDate.of(currentYear, 1, 1);

        // 3. If we haven't reached June 30th yet, just exit silently.
        if (today.isBefore(rolloverThreshold)) {
            return;
        }

        List<EmployeeDTO> employees = employeeService.getAllEmployees();

        for (EmployeeDTO emp : employees) {

            // 4. The Safety Gate: Did they already get this year's rollover?
            if (emp.getLastRolloverYear() != null && emp.getLastRolloverYear() == currentYear) {
                continue; // Already processed! Move to the next employee.
            }

            // 5. Perform your exact Three-Bucket shift logic
            employeeService.updateYearlyPTOS(emp);

            System.out.println("Mid-year ledger shift successfully processed for " + emp.getFirstName());
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Casablanca")
    @Transactional
    public void resetLastYearPTOS() {

        LocalDate today = LocalDate.now(ZoneId.of("Africa/Casablanca"));
        int currentYear = today.getYear();
        LocalDate rolloverThreshold = LocalDate.of(currentYear, 7, 1);

        if (today.isBefore(rolloverThreshold)) {
            return;
        }
        System.out.println("reset last year shift successfully processed for " + today);
        employeeService.resetLastYearPTOS();
    }
    /*@Scheduled(fixedRate = 50000)
    @Transactional
    public void quickDirtyAccrualTest() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();

        for (EmployeeDTO emp : employees) {
            BigDecimal newBalance = emp.getNextYearVacationDays().add(emp.getAccrualRatePerMonth());
            emp.setNextYearVacationDays(newBalance);
            System.out.println(newBalance);
            employeeService.updateNextYearVacationDays(emp);

            System.out.println("QUICK TEST: Added " + emp.getAccrualRatePerMonth()
                    + " days to " + emp.getFirstName() + " " + emp.getLastName() + " " + emp.getNextYearVacationDays());
        }
    }*/
}
