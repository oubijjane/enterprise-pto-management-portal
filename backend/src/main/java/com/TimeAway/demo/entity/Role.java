package com.TimeAway.demo.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "role-userrole")
    private Set<UserRole> users;

    @Column()
    @NotBlank(message = "required")
    private String name;
}
