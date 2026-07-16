package com.TimeAway.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class UserRole {

    @EmbeddedId
    private EmplyeeRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("emplyeeId")
    @JoinColumn(name = "emplyee_id", nullable = false)
    @JsonBackReference(value = "emplyee-userrole")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    @JsonBackReference(value = "role-userrole")
    private Role role;
}
