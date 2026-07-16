package com.TimeAway.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HolidayUpdatedEvent(Long holidayId,
                                  LocalDate oldDate,
                                  BigDecimal oldNumberOfDays,
                                  LocalDate newDate,
                                  BigDecimal newNumberOfDays) {
}
