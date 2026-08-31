package com.TimeAway.demo.dao;

import com.TimeAway.demo.dto.VacationRequestDto;
import com.TimeAway.demo.entity.VacationRequest;
import com.TimeAway.demo.enums.HolidayStatus;
import com.TimeAway.demo.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {
    VacationRequest findByEmployeeId(Long employeeId);
    Page<VacationRequest> findAll(Pageable pageable);
    @Override
    List<VacationRequest> findAll();
    @Query("SELECT v FROM VacationRequest v JOIN FETCH v.employee WHERE v.id = :id order by v.submittedAt desc")
    Optional<VacationRequest> findByIdWithEmployee(@Param("id") Long id);

    @Query("SELECT v FROM VacationRequest v " +
            "LEFT JOIN FETCH v.employee e " +
            "LEFT JOIN FETCH e.department " +
            "WHERE v.id = :id")
    Optional<VacationRequest> findByIdWithEmployeeAndDepartment(@Param("id") Long id);

    @Query(
            value = "SELECT v FROM VacationRequest v JOIN FETCH v.employee WHERE v.employee.id = :id order by v.submittedAt desc",
            countQuery = "SELECT count(v) FROM VacationRequest v WHERE v.employee.id = :id"
    )
    Page<VacationRequest> findAllByEmployeeId(@Param("id") Integer id, Pageable pageable);
    @Query("SELECT v FROM VacationRequest v JOIN FETCH v.employee WHERE v.status = :status order by v.submittedAt desc")
    Page<VacationRequest> findByStatus(RequestStatus status,  Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "employee.department"})
    @Query("SELECT v FROM VacationRequest v " +
            "WHERE (:status IS NULL OR v.status = :status) " +
            "AND v.employee.department.id = :departmentId order by v.submittedAt desc")
    Page<VacationRequest> findByStatusAndDepartmentId(
            @Param("status") RequestStatus status,
            @Param("departmentId") Long departmentId,
            Pageable pageable);
    @Query("SELECT v FROM VacationRequest v JOIN FETCH v.employee WHERE v.status <> :status")
    Page<VacationRequest> findByNotThisStatus(RequestStatus status,Pageable pageable);
    @Query("SELECT v FROM VacationRequest v JOIN FETCH v.employee where v.fromDate <= :date AND v.toDate >= :date " +
            "AND v.status = :status order by v.submittedAt desc")
    Page<VacationRequest> findRequestsByDateAndStatus(@Param("date")LocalDate date, @Param("status") RequestStatus status, Pageable pageable);
    @Query("SELECT v FROM VacationRequest v JOIN FETCH v.employee " +
            "WHERE v.status IN ('PENDING', 'APPROVED') AND " +
            "( (v.fromDate <= :oldHolEnd AND v.toDate >= :oldHolStart) OR " +
            "  (v.fromDate <= :newHolEnd AND v.toDate >= :newHolStart) ) order by v.submittedAt desc")
    List<VacationRequest> findAffectedRequests(
            @Param("oldHolStart") LocalDate oldHolStart,
            @Param("oldHolEnd") LocalDate oldHolEnd,
            @Param("newHolStart") LocalDate newHolStart,
            @Param("newHolEnd") LocalDate newHolEnd
    );

    @Query("SELECT v FROM VacationRequest v JOIN FETCH v.employee " +
            "WHERE v.status = :status order by v.submittedAt asc")
    List<VacationRequest> findRequestsByStatus(
            @Param("status") RequestStatus status
    );

    @Query("SELECT COUNT(v) FROM VacationRequest v " +
            "WHERE v.status = :status " +
            "AND v.submittedAt >= :yearStart")
    Long countByStatusAndYear(@Param("status") RequestStatus status,
                              @Param("yearStart") LocalDateTime yearStart);

    @EntityGraph(attributePaths = {"employee", "employee.department"})
    @Query("SELECT v FROM VacationRequest v " +
            "WHERE (:status IS NULL OR v.status = :status) " +
            "AND v.status <> com.TimeAway.demo.enums.RequestStatus.REJECTED " +
            "AND v.employee.department.id = :departmentId " +
            "AND v.fromDate <= :toDate AND v.toDate >= :fromDate order by v.submittedAt desc"
          )
    Page<VacationRequest> findAffectedRequestsByStatusAndDepartment(
            @Param("status") RequestStatus status,
            @Param("departmentId") Long departmentId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

}
