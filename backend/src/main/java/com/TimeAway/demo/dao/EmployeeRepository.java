package com.TimeAway.demo.dao;

import com.TimeAway.demo.dto.EmployeeDTO;
import com.TimeAway.demo.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<Employee, Integer> {
    Page<Employee> findAll(Pageable pageable);

    @Override
    List<Employee> findAll();


    @Query("SELECT e FROM Employee e " +
            "JOIN FETCH e.roles ur " +
            "JOIN FETCH ur.role " +
            "WHERE e.loginName = :loginName")
    Employee findLoginName(String loginName);

    @EntityGraph(attributePaths = {"employee", "employee.department"})
    @Query("SELECT e FROM Employee e " +
            "JOIN FETCH e.department d " +
            "WHERE d.id = :id")
    Page<Employee> findEmployeeByDepartmentId(@Param("id") Long id ,Pageable pageable);



    @Query("SELECT e FROM Employee e " +
            "LEFT JOIN FETCH e.department d " +
            "WHERE e.id = :id")
    Optional<Employee> findEmployeeById(Integer id);

    @Query("SELECT e FROM Employee e WHERE e.department.id = " +
            "(SELECT sub.department.id FROM Employee sub WHERE sub.loginName = :loginName)")
    List<Employee> findColleaguesByLoginName(@Param("loginName") String loginName);

    @Query("SELECT e FROM Employee e WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchEmployees(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE Employee e " +
            "SET e.lastYearVacationDays = 0, e.lastYearUsedVacationDays = 0 " +
            "WHERE e.lastYearVacationDays > 0 OR e.lastYearUsedVacationDays > 0")
    int bulkResetLastYearPTOS();

}
