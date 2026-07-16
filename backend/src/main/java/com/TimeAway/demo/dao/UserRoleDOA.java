package com.TimeAway.demo.dao;

import com.TimeAway.demo.entity.Employee;
import com.TimeAway.demo.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface UserRoleDOA extends JpaRepository<UserRole, Long> {
    @Query("""
        select ur.employee
        from UserRole ur
        where ur.role.name = :roleName
    """)
    List<Employee> findUsersByEmployeeName(@Param("roleName") String roleName);
    List<UserRole> findByEmployeeId(int userId);
    void deleteByEmployeeId(int id);
}
