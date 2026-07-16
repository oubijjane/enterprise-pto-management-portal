package com.TimeAway.demo.dto;

import com.TimeAway.demo.enums.HolidayStatus;
import com.TimeAway.demo.enums.HolidayType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HolidayDTO(Long id, String name, LocalDate date, HolidayType type, HolidayStatus status, BigDecimal numberOfDays) {

}
