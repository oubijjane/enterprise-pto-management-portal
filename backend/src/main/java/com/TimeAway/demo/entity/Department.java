package com.TimeAway.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "responsible_id", referencedColumnName = "id")
    @JsonManagedReference(value = "department-employee")
    private Employee responsible;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "department-employee")
    private List<Employee> employees = new ArrayList<>();

}
