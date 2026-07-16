package com.TimeAway.demo.restController;

import com.TimeAway.demo.dto.VacationRequestDto;
import com.TimeAway.demo.entity.VacationRequest;
import com.TimeAway.demo.enums.RequestStatus;
import com.TimeAway.demo.service.VacationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/request")
public class VacationRequestController {

    private VacationRequestService vacationRequestService;

    @Autowired
    public VacationRequestController(VacationRequestService vacationRequestService) {
        this.vacationRequestService = vacationRequestService;
    }

    @PostMapping("{id}")
    public ResponseEntity<VacationRequest> createVacationRequest(@RequestBody VacationRequestDto vacationRequest, @PathVariable int id) {
        return new  ResponseEntity<>(vacationRequestService.addVacationRequest(vacationRequest, id), HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<VacationRequestDto> getVacationRequest(@PathVariable long id) {
        return new   ResponseEntity<>(vacationRequestService.getVacationRequest(id), HttpStatus.OK);
    }

    @GetMapping("/pendings")
    public ResponseEntity<Page<VacationRequestDto>> getVacationRequest( @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(vacationRequestService.getPendingRequests(page, size), HttpStatus.OK);
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<Page<VacationRequestDto>> getEmployeeRequests( @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                         @PathVariable int id) {
        return new ResponseEntity<>(vacationRequestService.findByEmployeeId(page, size, id), HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countRequestByStatusForCurrentYear( @RequestParam RequestStatus status) {
        return new ResponseEntity<>(vacationRequestService.getCountOfRequestsByStatusAndYear(status), HttpStatus.OK);
    }

    @GetMapping("/non-rejected")
    public ResponseEntity<Page<VacationRequestDto>> getVacationRequestNonRejected( @RequestParam(defaultValue = "1") int page,
                                                                                   @RequestParam(defaultValue = "10") int size) {
        return new   ResponseEntity<>(vacationRequestService.getNonRejectedStatus(page, size), HttpStatus.OK);
    }

    @GetMapping("all")
    public ResponseEntity<Page<VacationRequestDto>> getAllVacationRequest(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(vacationRequestService.getVacationRequests(page, size), HttpStatus.OK);
    }
    @GetMapping("status")
    public ResponseEntity<Page<VacationRequestDto>> getAllVacationRequestByStatus(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) RequestStatus status) {
        return new ResponseEntity<>(vacationRequestService.getVacationRequestsByStatus(page, size, status), HttpStatus.OK);
    }

    @GetMapping("department")
    public ResponseEntity<Page<VacationRequestDto>> getAllVacationRequestByStatusByDepartment(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        return new ResponseEntity<>(vacationRequestService.findByStatusAndDepartmentId(status,principal.getName()
                , page, size), HttpStatus.OK);
    }
    @GetMapping("department&status/{id}")
    public ResponseEntity<Page<VacationRequestDto>> getAllVacationRequestByStatusByDepartmentByDate(
            @PathVariable long id ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        return new ResponseEntity<>(vacationRequestService.findAffectedRequestsByStatusAndDepartment(id
                , page, size), HttpStatus.OK);
    }

    @PutMapping()
    public ResponseEntity<VacationRequest> updateVacationRequest(@RequestBody VacationRequestDto vacationRequest) {
        return new   ResponseEntity<>(vacationRequestService.updateVacationRequest(vacationRequest), HttpStatus.OK);
    }
    @PutMapping("/approved/{id}")
    public ResponseEntity<VacationRequest>  approveRequest(@PathVariable long id) {
        return new ResponseEntity<>(vacationRequestService.approveRequest(id), HttpStatus.OK);
    }

    @PutMapping("/rejected/{id}")
    public ResponseEntity<VacationRequest>  rejectRequest(@PathVariable long id) {
        return new ResponseEntity<>(vacationRequestService.rejectRequest(id), HttpStatus.OK);
    }

    @PutMapping("/cancelPendingVacationRequest/{id}")
    public ResponseEntity<VacationRequest>  cancelPendingVacationRequest(@PathVariable long id, Principal principal) {
        return new ResponseEntity<>(vacationRequestService.cancelPendingVacationRequest(id, principal.getName()), HttpStatus.OK);
    }

    @PutMapping("/cancellation/{id}")
    public ResponseEntity<VacationRequest> cancellationRequest(@PathVariable long id, Principal principal) {
        return new ResponseEntity<>(vacationRequestService.cancellationRequest(id, principal.getName()), HttpStatus.OK);
    }

    @PutMapping("/approvedByResponsible/{id}")
    public ResponseEntity<VacationRequestDto>  approvedByResponsible(@PathVariable long id, Principal principal) {
        return new ResponseEntity<>(vacationRequestService.approvedByResponsible(id, principal.getName()), HttpStatus.OK);
    }

    @PutMapping("/rejectByResponsible/{id}")
    public ResponseEntity<VacationRequestDto>  rejectByResponsible(@PathVariable long id, Principal principal) {
        return new ResponseEntity<>(vacationRequestService.rejectByResponsible(id, principal.getName()), HttpStatus.OK);
    }

    @PutMapping("/approveCancellation/{id}")
    public ResponseEntity<VacationRequest> approveCancellation(@PathVariable long id) {
        return new ResponseEntity<>(vacationRequestService.approveCancellation(id), HttpStatus.OK);
    }

    @PutMapping("/rejectCancellation/{id}")
    public ResponseEntity<VacationRequest> rejectCancellation(@PathVariable long id) {
        return new ResponseEntity<>(vacationRequestService.rejectCancellation(id), HttpStatus.OK);
    }
}
