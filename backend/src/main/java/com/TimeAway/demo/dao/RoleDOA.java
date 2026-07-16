package com.TimeAway.demo.dao;

import com.TimeAway.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDOA extends JpaRepository<Role, Integer> {
    Role findRoleById(Integer id);
}
