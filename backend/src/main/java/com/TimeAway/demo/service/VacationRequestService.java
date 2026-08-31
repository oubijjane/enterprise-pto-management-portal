package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.VacationRequestDto;
import com.TimeAway.demo.entity.VacationRequest;
import com.TimeAway.demo.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VacationRequestService {
    VacationRequestDto getVacationRequest(long id);
    List<VacationRequestDto> getVacationRequestsByStatus(@Param("status") RequestStatus status);
    List<VacationRequestDto> getVacationRequests();
    Page<VacationRequestDto> findByEmployeeId(int page, int size, Integer id);
    VacationRequestDto approvedByResponsible(long id, String responsible);
    VacationRequestDto rejectByResponsible(long id, String responsible);
    Page<VacationRequestDto> findByStatusAndDepartmentId(RequestStatus status,String loginName,
                                                         Integer page, int size);
    Page<VacationRequestDto> findAffectedRequestsByStatusAndDepartment( long id,
                                                         int page, int size);
    Page<VacationRequestDto> getVacationRequests(int page, int size);
    Page<VacationRequestDto> getCurrentApprovedRequests(int page, int size);
    Page<VacationRequestDto> getVacationRequestsByStatus(int page, int size, RequestStatus status);
    Long getCountOfRequestsByStatusAndYear( RequestStatus status);
    Page<VacationRequestDto> getNonRejectedStatus(int page, int size);
    Page<VacationRequestDto> getPendingRequests(int page, int size);
    VacationRequest addVacationRequest(VacationRequestDto vacationRequest, int employeeId);
    VacationRequest updateVacationRequest(VacationRequestDto vacationRequest);
    VacationRequest updateVacationRequestStatus(VacationRequestDto vacationRequest);
    VacationRequest approveRequest(long id);
    VacationRequest rejectRequest(long id);
    VacationRequest cancelPendingVacationRequest(long id,String loginName);
    VacationRequest requestCancellation(long id,String loginName);
    VacationRequest approveCancellation(long id);
    VacationRequest rejectCancellation(long id);
    VacationRequest cancellationRequest(long id,String loginName);
}
