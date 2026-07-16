package com.TimeAway.demo.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EmplyeeRoleId implements Serializable {

    private Integer emplyeeId;
    private Integer roleId;

    public EmplyeeRoleId() {}

    public EmplyeeRoleId(Integer emplyeeId, Integer roleId) {
        this.emplyeeId = emplyeeId;
        this.roleId = roleId;
    }

    public Integer getUserId() {
        return emplyeeId;
    }

    public void setUserId(Integer userId) {
        this.emplyeeId = userId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmplyeeRoleId that = (EmplyeeRoleId) o;
        return Objects.equals(emplyeeId, that.emplyeeId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emplyeeId, roleId);
    }
}
