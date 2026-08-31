package com.TimeAway.demo.dao;

import com.TimeAway.demo.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Set;


public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    @Query("SELECT DISTINCT h FROM Holiday h WHERE YEAR(h.date) = :year ORDER BY h.date ASC")
    Set<Holiday> findAllDatesByYear(@Param("year") int year);

    @Query("SELECT h FROM Holiday h WHERE h.date BETWEEN :startDate AND :endDate")
    Set<Holiday> findHolidaysBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


}
