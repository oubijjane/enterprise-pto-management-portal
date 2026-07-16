package com.TimeAway.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class Employee implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true, unique = false)
    private String firstName;

    @Column(nullable = true, unique = false)
    private String lastName;

    @Column(unique = true, nullable = true)
    private String loginName;

    @Column(unique = true, nullable = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(unique = true, nullable = true)
    private String phone;

    @Column
    private BigDecimal thisYearVacationDays = BigDecimal.ZERO;

    @Column
    private BigDecimal lastYearVacationDays = BigDecimal.ZERO;

    @Column
    private BigDecimal usedVacationDays  = BigDecimal.ZERO;

    @Column
    private BigDecimal lastYearUsedVacationDays  = BigDecimal.ZERO;

    @Column(nullable = true)
    private BigDecimal accrualRatePerMonth = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private LocalDate lastAccrualDate = LocalDate.now();

    @Column
    private BigDecimal nextYearVacationDays = BigDecimal.ZERO;

    @Column(name = "last_rollover_year")
    private Integer lastRolloverYear;

    @Column(nullable = false, unique = false)
    private LocalDate hiringDate;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at", columnDefinition = "DATETIME(0)")
    private LocalDate createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME(0)")
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonBackReference(value = "department-employee")
    private Department department;

    @OneToOne(mappedBy = "responsible", fetch = FetchType.LAZY)
    @JsonBackReference(value = "department-employee")
    private Department managedDepartment;

    @Column
    private Boolean isActive = true;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "vacationRequest-employee")
    private List<VacationRequest> vacationRequests;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "emplyee-userrole")
    @Nullable
    private Set<UserRole> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "employee_default_backups",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "backup_employee_id")
    )
    @JsonIgnoreProperties({"defaultBackups", "coveringFor", "vacationRequests"})
    private Set<Employee> defaultBackups = new HashSet<>();

    @ManyToMany(mappedBy = "defaultBackups")
    private Set<Employee> coveringFor = new HashSet<>();

    @Transient
    public BigDecimal getRemainingDays() {
        return lastYearVacationDays.add(thisYearVacationDays).subtract(usedVacationDays).subtract(lastYearUsedVacationDays);
    }

    @Transient
    public BigDecimal getRollOverDays() {
        return lastYearVacationDays.subtract(lastYearUsedVacationDays);
    }

    @Transient
    public BigDecimal getThisYearDays() {
        return thisYearVacationDays.subtract(usedVacationDays);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole().getName())) // Assuming UserRole has getRoleName()
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.loginName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.isActive);
    }
}
