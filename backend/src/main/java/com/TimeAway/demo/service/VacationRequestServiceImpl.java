package com.TimeAway.demo.service;

import com.TimeAway.demo.dao.EmployeeRepository;
import com.TimeAway.demo.dao.VacationRequestRepository;
import com.TimeAway.demo.dto.*;
import com.TimeAway.demo.entity.Employee;
import com.TimeAway.demo.entity.VacationRequest;
import com.TimeAway.demo.enums.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VacationRequestServiceImpl implements VacationRequestService {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final VacationRequestRepository vacationRequestRepository;
    private final HolidayService holidayService;
    private final EmailService emailService;

    @Autowired
    public VacationRequestServiceImpl(EmployeeService employeeService,
                                      VacationRequestRepository vacationRequestRepository,
                                      HolidayService holidayService, EmployeeRepository employeeRepository,
                                      EmailService emailService) {
        this.employeeService = employeeService;
        this.vacationRequestRepository = vacationRequestRepository;
        this.holidayService = holidayService;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
    }
    @Override
    public VacationRequestDto getVacationRequest(long id) {
        VacationRequest request = vacationRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find VacationRequest with the id: " + id));

        return mapToDTO(request);
    }

    @Override
    public Page<VacationRequestDto> findByEmployeeId(int page, int size, Integer id) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<VacationRequestDto> requests = vacationRequestRepository.findAllByEmployeeId(id, pageable)
                .map(request -> {

                    return mapToDTO(request);
                });
        return requests;

    }

    @Override
    public VacationRequestDto approvedByResponsible(long id, String responsible ) {
            VacationRequest vacationRequest1 = vacationRequestRepository.findByIdWithEmployeeAndDepartment(id)
                    .orElseThrow(() -> new RuntimeException("Could not find VacationRequest with id: " + id));
        if(vacationRequest1.getStatus()!=RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Action denied. You can only directly reject or approve requests that are currently pending. " +
                            "This request is currently: " + vacationRequest1.getStatus()
            );
        }

        if(vacationRequest1.getApprovedByResponsible()!= null) {
            throw new IllegalStateException(
                    "Action denied. You can only directly reject or approve requests that are currently not reviewed. " +
                            "This request is currently: " + vacationRequest1.getApprovedByResponsible()
            );
        }
        String res= "";
        boolean isDepartHasResp =vacationRequest1.getEmployee().getDepartment() != null &&  vacationRequest1.getEmployee().getDepartment().getResponsible() !=null;
        System.out.println(isDepartHasResp);
        if(isDepartHasResp) {
            System.out.println(res);
            res = vacationRequest1.getEmployee().getDepartment().getResponsible().getLoginName();
            System.out.println(res);
        }
        if(!isDepartHasResp || responsible.equals(res)) {
            vacationRequest1.setApprovedByResponsible(true);
        } else {
            throw new IllegalStateException("you can't approve the request outside of your department");
        }
        VacationRequest savedVacationRequest = vacationRequestRepository.save(vacationRequest1);


        return mapToDTO(savedVacationRequest);
    }

    @Override
    public VacationRequestDto rejectByResponsible(long id, String responsible) {
        VacationRequest vacationRequest1 = vacationRequestRepository.findByIdWithEmployeeAndDepartment(id)
                .orElseThrow(() -> new RuntimeException("Could not find VacationRequest with id: " + id));
        if(vacationRequest1.getStatus()!=RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Action denied. You can only directly reject or approve requests that are currently pending. " +
                            "This request is currently: " + vacationRequest1.getStatus()
            );
        }

        if(vacationRequest1.getApprovedByResponsible()!= null) {
            throw new IllegalStateException(
                    "Action denied. You can only directly reject or approve requests that are currently not reviewed. " +
                            "This request is currently: " + vacationRequest1.getApprovedByResponsible()
            );
        }
        String res= "";
        boolean isDepartHasResp =vacationRequest1.getEmployee().getDepartment() != null &&  vacationRequest1.getEmployee().getDepartment().getResponsible() !=null;
        System.out.println(isDepartHasResp);
        if(isDepartHasResp) {
            System.out.println(res);
            res = vacationRequest1.getEmployee().getDepartment().getResponsible().getLoginName();
            System.out.println(res);
        }
        if(!isDepartHasResp || responsible.equals(res)) {
            vacationRequest1.setApprovedByResponsible(false);
        } else {
            throw new IllegalStateException("you can't approve the request outside of your department");
        }
        VacationRequest savedVacationRequest = vacationRequestRepository.save(vacationRequest1);


        return mapToDTO(savedVacationRequest);
    }

    @Override
    public Page<VacationRequestDto> findByStatusAndDepartmentId(RequestStatus status,String loginName,
                                                                Integer page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Employee employee = employeeService.findLoginName(loginName);
        long departmentId = employee.getDepartment().getId();
        System.out.println(departmentId + " " + status);
        return vacationRequestRepository.findByStatusAndDepartmentId(status,departmentId,pageable)
                .map(v -> mapToDTO(v));
    }

    @Override
    public Page<VacationRequestDto> findAffectedRequestsByStatusAndDepartment(long  id,
                                                                              int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        VacationRequest vacationRequest = vacationRequestRepository
                .findById(id).orElseThrow(() -> new RuntimeException("Could not find VacationRequest with id: " + id));
        Long departmentId = vacationRequest.getEmployee().getDepartment().getId();
        LocalDate fromDate = vacationRequest.getFromDate();
        LocalDate toDate = vacationRequest.getToDate();
        return vacationRequestRepository.findAffectedRequestsByStatusAndDepartment(null, departmentId,
                fromDate, toDate, pageable) .map(v -> mapToDTO(v));
    }

    private VacationRequest getVacationRequestById(long id) {
        return vacationRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find VacationRequest with the id: " + id));
    }
@Override
    public Page<VacationRequestDto> getVacationRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<VacationRequestDto> requests = vacationRequestRepository.findAll(pageable)
                .map(request -> mapToDTO(request));
        return requests;

    }

    @Override
    public Page<VacationRequestDto> getVacationRequestsByStatus(int page, int size, RequestStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        if (status ==  null) {
            return getVacationRequests(page, size);
        }
        Page<VacationRequestDto> requests = vacationRequestRepository.findByStatus(status,pageable)
                .map(request -> mapToDTO(request));
        return requests;
    }

    @Override
    public Long getCountOfRequestsByStatusAndYear(RequestStatus status) {
        LocalDateTime yearStart = LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0);
        return vacationRequestRepository.countByStatusAndYear(status, yearStart);
    }

    @Override
    public Page<VacationRequestDto> getNonRejectedStatus(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<VacationRequestDto> requests = vacationRequestRepository.findByNotThisStatus(RequestStatus.REJECTED, pageable)
                .map(request -> mapToDTO(request));
        return requests;
    }

    @Override
    public Page<VacationRequestDto> getPendingRequests(int page, int size) {
        Pageable pageable = PageRequest.of(size, page, Sort.by("id").descending());
        Page<VacationRequestDto> requests = vacationRequestRepository.findByStatus(RequestStatus.PENDING, pageable)
                .map(request -> mapToDTO(request));
        return requests;
    }

    @Override
    public VacationRequest addVacationRequest(VacationRequestDto vacationRequest, int employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(
                () -> new RuntimeException("Could not find Employee with id: " + employeeId));

        VacationRequest newVacationRequest = new VacationRequest();
        newVacationRequest.setEmployee(employee);
        newVacationRequest.setFromDate(vacationRequest.getFromDate());
        newVacationRequest.setToDate(vacationRequest.getToDate());
        newVacationRequest.setReason(vacationRequest.getReason());
        newVacationRequest.setSubmittedAt(vacationRequest.getSubmittedAt());
        newVacationRequest.setStatus(vacationRequest.getStatus());
        newVacationRequest.setHalfDayType(vacationRequest.getHalfDayType());
        List<HolidayDTO> holidayDTOS = holidayService.findAllHolidays();
        BigDecimal numberOfDays = calculateRequestedDays(vacationRequest.getFromDate(), vacationRequest.getToDate(),
                holidayDTOS, vacationRequest.getHalfDayType());
        newVacationRequest.setNumberOfDays(numberOfDays);
        VacationRequest savedVacationRequest = vacationRequestRepository.save(newVacationRequest);
        VacationRequestDto request = mapToDTO(savedVacationRequest);
        String message = "Une nouvelle demande de congé a été soumise";
        String statusUpdate = "Nouvelle demande de congé";
        sendEmail(savedVacationRequest,  message, statusUpdate);
        return savedVacationRequest;
    }

    @Override
    public VacationRequest updateVacationRequest(VacationRequestDto vacationRequest) {
        VacationRequest exictingVacationRequest = getVacationRequestById(vacationRequest.getId());
        exictingVacationRequest.setReason(vacationRequest.getReason());
        exictingVacationRequest.setStatus(vacationRequest.getStatus());
        exictingVacationRequest.setFromDate(vacationRequest.getFromDate());
        exictingVacationRequest.setToDate(vacationRequest.getToDate());
        exictingVacationRequest.setHalfDayType(vacationRequest.getHalfDayType());

        List<HolidayDTO> holidayDTOS = holidayService.findAllHolidaysByDateRange(
                vacationRequest.getFromDate(),
                vacationRequest.getToDate()
        );
        BigDecimal numberOfDays = calculateRequestedDays(
                vacationRequest.getFromDate(),
                vacationRequest.getToDate(),
                holidayDTOS,
                vacationRequest.getHalfDayType()
        );
        exictingVacationRequest.setNumberOfDays(numberOfDays);

        return vacationRequestRepository.save(exictingVacationRequest);
    }

    @Override
    public VacationRequest updateVacationRequestStatus(VacationRequestDto vacationRequest) {
        VacationRequest exictingVacationRequest = getVacationRequestById(vacationRequest.getId());
        validateStatusTransition(exictingVacationRequest.getStatus(),  vacationRequest.getStatus());
        exictingVacationRequest.setStatus(vacationRequest.getStatus());
        return vacationRequestRepository.save(exictingVacationRequest);
    }

    @Override
    @Transactional
    public VacationRequest approveRequest(long id) {
        VacationRequest vacationRequest = vacationRequestRepository.findByIdWithEmployee(id)
                .orElseThrow(() -> new RuntimeException("Could not find VacationRequest with the id: " + id));
        if(vacationRequest.getStatus()!=RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Action denied. You can only directly cancel or approve requests that are currently PENDING. " +
                            "This request is currently: " + vacationRequest.getStatus()
            );
        }
        List<HolidayDTO> holidayDTOS = holidayService.findAllHolidaysByDateRange(vacationRequest.getFromDate(),vacationRequest.getToDate());
        BigDecimal requestedDays = calculateRequestedDays(vacationRequest.getFromDate(), vacationRequest.getToDate(), holidayDTOS, vacationRequest.getHalfDayType());
        approveNewRequest(vacationRequest.getEmployee(),requestedDays);
        return updateRequestStatus(vacationRequest, RequestStatus.APPROVED);
    }

    private void sendEmail(VacationRequest vacationRequest, String messageToBeSent, String statusUpdate) {
        List<String> emails = new ArrayList<>();
        long id = 1;
        List<Employee>  employees = employeeRepository.findEmployeeByRoleId(id);
        for (Employee employee : employees) {
            if(employee.getEmail() != null && !employee.getEmail().isEmpty()) {
                emails.add(employee.getEmail());
                System.out.println("this is the email");
            }
        }


        if(vacationRequest.getEmployee().getEmail() != null && !vacationRequest.getEmployee().getEmail().isEmpty()) {

            emails.add(vacationRequest.getEmployee().getEmail());

        }

        emailService.sendNotificationWithMessage(mapToDTO(vacationRequest), emails, messageToBeSent, statusUpdate);
    }

    @Override
    public VacationRequest rejectRequest(long id) {
        VacationRequest vacationRequest = getVacationRequestById(id);
        return updateRequestStatus(vacationRequest, RequestStatus.REJECTED);
    }


    @Override
    public VacationRequest cancelPendingVacationRequest(long id, String loginName) {
        VacationRequest VacationRequest = findVacationRequestByIdWithEmployee(id);
        verifyOwner(VacationRequest, loginName);
        if(VacationRequest.getStatus()!=RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Action denied. You can only directly cancel requests that are currently PENDING. " +
                            "This request is currently: " + VacationRequest.getStatus()
            );
        }
        return updateRequestStatus(VacationRequest, RequestStatus.CANCELLED);
    }

    @Override
    public VacationRequest requestCancellation(long id, String loginName) {
        VacationRequest VacationRequest = findVacationRequestByIdWithEmployee(id);
        return updateRequestStatus(VacationRequest, RequestStatus.CANCELLATION_REQUESTED);
    }

    @Override
    @Transactional
    public VacationRequest approveCancellation(long id) {
        VacationRequest vacationRequest = getVacationRequestById(id);
        if(vacationRequest.getStatus()!=RequestStatus.CANCELLATION_REQUESTED) {
            throw new IllegalStateException(
                    "Action denied. You can only directly cancel requests that are currently PENDING. " +
                            "This request is currently: " + vacationRequest.getStatus()
            );
        }
        refundCancelledRequest(vacationRequest.getEmployee(),vacationRequest.getNumberOfDays());
        vacationRequest.setStatus(RequestStatus.CANCELLED);
        return vacationRequestRepository.save(vacationRequest);
    }

    @Override
    public VacationRequest rejectCancellation(long id) {
        VacationRequest vacationRequest = getVacationRequestById(id);
        if(vacationRequest.getStatus()!=RequestStatus.CANCELLATION_REQUESTED) {
            throw new IllegalStateException(
                    "Action denied. You can only directly cancel requests that are currently requesting cancellation. " +
                            "This request is currently: " + vacationRequest.getStatus()
            );
        }
        vacationRequest.setStatus(RequestStatus.APPROVED);
        return vacationRequestRepository.save(vacationRequest);
    }

    @Override
    public VacationRequest cancellationRequest(long id, String loginName) {
        VacationRequest VacationRequest = findVacationRequestByIdWithEmployee(id);
        verifyOwner(VacationRequest, loginName);
        validateStatusTransition(VacationRequest.getStatus(),  RequestStatus.CANCELLATION_REQUESTED);
        return updateRequestStatus(VacationRequest, RequestStatus.CANCELLATION_REQUESTED);
    }


    private void validateStatusTransition(RequestStatus current, RequestStatus next) {
        boolean isValid = switch (current) {
            case PENDING -> next == RequestStatus.APPROVED || next == RequestStatus.REJECTED || next == RequestStatus.CANCELLED;
            case APPROVED -> next == RequestStatus.CANCELLATION_REQUESTED;
            case CANCELLATION_REQUESTED -> next == RequestStatus.APPROVED || next == RequestStatus.CANCELLED;
            // REJECTED and CANCELLED are terminal states, they cannot transition to anything
            default -> false;
        };

        if (!isValid) {
            throw new IllegalStateException(
                    "Invalid status transition. Cannot move request from " + current + " to " + next
            );
        }
    }
    private VacationRequest updateRequestStatus(VacationRequest exictingVacationRequest, RequestStatus status) {
        validateStatusTransition(exictingVacationRequest.getStatus(), status);
        exictingVacationRequest.setStatus(status);
        VacationRequest savedVacationRequest = vacationRequestRepository.save(exictingVacationRequest);
        String message = "";
        if(status == RequestStatus.APPROVED) {
            message = "demande de congé est approuvée";
        } else if(status == RequestStatus.REJECTED) {
            message = "demande de congé est rejetée";
        }
        sendEmail(savedVacationRequest, message, message);
        return savedVacationRequest;
    }

    private void verifyOwner(VacationRequest vacationRequest, String loginName) {
        Employee owner = vacationRequest.getEmployee();

        if(owner == null || owner.getLoginName() == null || owner.getLoginName().isEmpty()) {
            throw new IllegalStateException("Corrupted record: Vacation request has no valid owner.");
        }

        if(!owner.getLoginName().equals(loginName)) {
            throw new AccessDeniedException("Action denied. You do not have ownership of this vacation request.");
        }
    }

    private VacationRequest findVacationRequestByIdWithEmployee(long id) {
        return vacationRequestRepository.findByIdWithEmployee(id)
                .orElseThrow(() -> new RuntimeException("Could not find VacationRequest with the id: " + id));
    }

    private void approveNewRequest(Employee employee, BigDecimal requestedDays) {
        // 1. The Guardrail
        if (employee.getRemainingDays().compareTo(requestedDays) < 0) {
            throw new IllegalStateException("Insufficient PTO: Remaining days cannot be less than requested days.");
        }


        BigDecimal availableRollover = employee.getRollOverDays();

        // NOTE: If you are using an expiration date, you would add a check here:
        // if (LocalDate.now().isAfter(employee.getRolloverExpirationDate())) { availableRollover = BigDecimal.ZERO; }

        // 3. FIFO Ledger Math
        if (availableRollover.compareTo(requestedDays) >= 0) {

            // SCENARIO A: The Rollover Wallet covers the entire vacation.
            // We do not touch the Standard Wallet at all.
            BigDecimal updatedLastYearUsed = employee.getLastYearUsedVacationDays().add(requestedDays);
            employee.setLastYearUsedVacationDays(updatedLastYearUsed);

        } else if (availableRollover.compareTo(BigDecimal.ZERO) > 0) {

            // SCENARIO B: The Rollover Wallet covers SOME of the vacation (Spillover).
            BigDecimal remainingToPay = requestedDays.subtract(availableRollover);

            // Empty the Rollover Wallet entirely
            BigDecimal updatedLastYearUsed = employee.getLastYearUsedVacationDays().add(availableRollover);
            employee.setLastYearUsedVacationDays(updatedLastYearUsed);

            // Charge the remainder to the Standard Wallet
            BigDecimal updatedThisYearUsed = employee.getUsedVacationDays().add(remainingToPay);
            employee.setUsedVacationDays(updatedThisYearUsed);

        } else {

            // SCENARIO C: The Rollover Wallet is completely empty (or expired).
            // Charge 100% of the request to the Standard Wallet.
            BigDecimal updatedThisYearUsed = employee.getUsedVacationDays().add(requestedDays);
            employee.setUsedVacationDays(updatedThisYearUsed);

        }

        // 4. Save the ledger
        employeeService.updateDays(employee);
    }
    BigDecimal calculateRequestedDays(LocalDate start, LocalDate end, List<HolidayDTO> holidayDTOs, String halfDayType) {
        String normalizedHalfDayType = halfDayType == null ? "FULL_DAY" : halfDayType.trim().toUpperCase();
        if (!"FULL_DAY".equals(normalizedHalfDayType) && !"AM".equals(normalizedHalfDayType) && !"PM".equals(normalizedHalfDayType)) {
            throw new IllegalArgumentException("Unsupported half-day type: " + halfDayType);
        }

        if (!"FULL_DAY".equals(normalizedHalfDayType) && !start.equals(end)) {
            throw new IllegalArgumentException("Half-day PTO can only be requested for a single day.");
        }

        if ("FULL_DAY".equals(normalizedHalfDayType)) {
            BigDecimal actualDaysToCharge = BigDecimal.ZERO;
            LocalDate currentDate = start;

            while (!currentDate.isAfter(end)) {
                DayOfWeek day = currentDate.getDayOfWeek();
                boolean isWeekend = (day == DayOfWeek.SUNDAY);

                final LocalDate checkDate = currentDate;
                boolean isHoliday = holidayDTOs.stream()
                        .anyMatch(h -> isDateInsideHoliday(checkDate, h.date(), h.numberOfDays()));

                if (!isWeekend && !isHoliday) {
                    actualDaysToCharge = actualDaysToCharge.add(BigDecimal.ONE);
                }

                currentDate = currentDate.plusDays(1);
            }

            return actualDaysToCharge;
        }

        DayOfWeek day = start.getDayOfWeek();
        boolean isWeekend = (day == DayOfWeek.SUNDAY);
        boolean isHoliday = holidayDTOs.stream()
                .anyMatch(h -> isDateInsideHoliday(start, h.date(), h.numberOfDays()));

        if (isWeekend || isHoliday) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal("0.5");
    }

    private BigDecimal removeHolidaysFromRequest(LocalDate start, LocalDate end, List<HolidayDTO> holidayDTOs) {
        return calculateRequestedDays(start, end, holidayDTOs, "FULL_DAY");
    }
    private boolean isDateInsideHoliday(LocalDate checkDate, LocalDate holidayStart, BigDecimal numberOfDays) {
        if (holidayStart == null || numberOfDays == null || numberOfDays.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // We subtract 1 because a 1-day holiday starts and ends on the SAME day.
        // E.g., A 2-day holiday starting on March 20th means we add 1 day to get the end date (March 21st).
        long daysToAdd = numberOfDays.longValue() - 1;
        LocalDate holidayEnd = holidayStart.plusDays(daysToAdd);

        // Check if the date is on or after the start, AND on or before the end
        return !checkDate.isBefore(holidayStart) && !checkDate.isAfter(holidayEnd);
    }

    private void adjustEmployeeBalance(Employee employee, BigDecimal difference) {
        // POSITIVE DELTA: The employee gets days back
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            refundCancelledRequest(employee, difference);
        }
        // NEGATIVE DELTA: The employee owes days
        else if (difference.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal extraDaysToCharge = difference.abs();
            chargeExtraDays(employee, extraDaysToCharge);
        }
    }
    private void refundCancelledRequest(Employee employee, BigDecimal refundDays) {

        BigDecimal currentYearUsed = employee.getUsedVacationDays();

        // SCENARIO A: The Standard Wallet can absorb the entire refund.
        // Meaning we only need to refund standard days.
        if (currentYearUsed.compareTo(refundDays) >= 0) {

            BigDecimal updatedThisYearUsed = currentYearUsed.subtract(refundDays);
            employee.setUsedVacationDays(updatedThisYearUsed);

        }
        // SCENARIO B: Spillover! We must refund Standard down to zero,
        // and push the remaining refund into the Rollover wallet.
        else {

            // 1. Calculate how much of the refund spills over
            BigDecimal spilloverRefund = refundDays.subtract(currentYearUsed);

            // 2. Reset the Standard Used bucket to exactly zero
            employee.setUsedVacationDays(BigDecimal.ZERO);

            // 3. Apply the remaining refund to the Rollover Used bucket
            BigDecimal updatedLastYearUsed = employee.getLastYearUsedVacationDays().subtract(spilloverRefund);

            // 4. Final Safety Clamp (Protects against corrupted database rows)
            if (updatedLastYearUsed.compareTo(BigDecimal.ZERO) < 0) {
                updatedLastYearUsed = BigDecimal.ZERO;
            }

            employee.setLastYearUsedVacationDays(updatedLastYearUsed);
        }

        employeeService.updateDays(employee);
    }

    private void chargeExtraDays(Employee employee, BigDecimal chargeDays) {
        BigDecimal totalRolloverAllowance = employee.getLastYearVacationDays();
        BigDecimal currentRolloverUsed = employee.getLastYearUsedVacationDays();
        BigDecimal remainingRollover = totalRolloverAllowance.subtract(currentRolloverUsed);

        // Rollover Wallet has enough space to take the charge
        if (remainingRollover.compareTo(chargeDays) >= 0) {
            employee.setLastYearUsedVacationDays(currentRolloverUsed.add(chargeDays));
        }
        // Spillover: Max out Rollover, push the rest of the charge to Standard
        else {
            employee.setLastYearUsedVacationDays(totalRolloverAllowance);

            BigDecimal spilloverCharge = chargeDays.subtract(remainingRollover);
            employee.setUsedVacationDays(employee.getUsedVacationDays().add(spilloverCharge));
        }

        employeeService.updateDays(employee);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testingMethode(HolidayUpdatedEvent event) {

        // 1. Fixed the math: Subtract 1 so a 1-day holiday ends on the same day it starts
        LocalDate oldHoldEnd = event.oldDate().plusDays(event.oldNumberOfDays().longValue() - 1);

        // 2. Fixed the typo: Using newNumberOfDays for the new date calculation
        LocalDate newHoldEnd = event.newDate().plusDays(event.newNumberOfDays().longValue() - 1);

        List<HolidayDTO> holidayDTOS = holidayService.findAllHolidays();

        vacationRequestRepository.findAffectedRequests(event.oldDate(), oldHoldEnd, event.newDate(), newHoldEnd)
                .forEach(request -> {
                    BigDecimal newNumberOfDays = removeHolidaysFromRequest(request.getFromDate(), request.getToDate(), holidayDTOS);

                    // GATE 1: Skip entirely if the math didn't actually change anything
                    if (newNumberOfDays.compareTo(request.getNumberOfDays()) == 0) {
                        return; // Acts like 'continue' inside a forEach loop
                    }

                    BigDecimal difference = request.getNumberOfDays().subtract(newNumberOfDays);

                    System.out.println("affected requests:----------------- ");
                    System.out.println(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName() + "\n"
                            + "Request ID: " + request.getId() + "\n"
                            + "old total days: " + request.getNumberOfDays() + "\n"
                            + request.getFromDate() + " to " + request.getToDate() + "\n"
                            + "new total days: " + newNumberOfDays);

                    // GATE 2: Actually save the new true cost to the database!
                    request.setNumberOfDays(newNumberOfDays);
                    vacationRequestRepository.save(request); // <-- CRITICAL

                    // GATE 3: Only touch the wallet if the days were already physically deducted
                    if (request.getStatus() == RequestStatus.APPROVED) {
                        adjustEmployeeBalance(request.getEmployee(), difference);
                    }
                    else if (request.getStatus() == RequestStatus.PENDING) {
                        // (Optional) Add your auto-cancel logic here if the new cost exceeds their available balance!
                        System.out.println("Request is PENDING. Updated request cost, but bypassed wallet adjustment.");
                    }
                });
    }

    private VacationRequestDto mapToDTO(VacationRequest request) {
        VacationRequestDto vacationRequestDto = new VacationRequestDto();
        vacationRequestDto.setId(request.getId());
        vacationRequestDto.setFromDate(request.getFromDate());
        vacationRequestDto.setToDate(request.getToDate());
        vacationRequestDto.setReason(request.getReason());
        vacationRequestDto.setStatus(request.getStatus());
        vacationRequestDto.setSubmittedAt(request.getSubmittedAt());
        vacationRequestDto.setNumberOfDays(request.getNumberOfDays());
        vacationRequestDto.setHalfDayType(request.getHalfDayType());
        vacationRequestDto.setApprovedByResponsible(request.getApprovedByResponsible());
        EmployeeDTO employeeDTO = new EmployeeDTO();
        Employee employee = request.getEmployee();
        employeeDTO.setId(employee.getId());
        employeeDTO.setFirstName(employee.getFirstName());
        employeeDTO.setLastName(employee.getLastName());
        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setId(employee.getDepartment().getId());
        departmentDTO.setName(employee.getDepartment().getName());
        EmployeeDTO responsibleDTO = new EmployeeDTO();

        if(request.getEmployee().getDepartment() != null && request.getEmployee().getDepartment().getResponsible() != null) {
            responsibleDTO.setId(request.getEmployee().getDepartment().getResponsible().getId());
            responsibleDTO.setFirstName(request.getEmployee().getDepartment().getResponsible().getFirstName());
            responsibleDTO.setLastName(request.getEmployee().getDepartment().getResponsible().getLastName());
            departmentDTO.setResponsible(responsibleDTO);
            employeeDTO.setDepartmentDTO(departmentDTO);
        }

        employeeDTO.setRemainingVacationDays(employee.getRemainingDays());
        vacationRequestDto.setEmployeeDTO(employeeDTO);

        return vacationRequestDto;
    }

}
