package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.HolidayDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VacationRequestServiceImplTest {

    private final VacationRequestServiceImpl service = new VacationRequestServiceImpl(null, null, null, null, null);

    @Test
    void halfDaySingleDayReturnsHalfDay() {
        BigDecimal result = service.calculateRequestedDays(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                List.of(),
                "AM"
        );

        assertEquals(new BigDecimal("0.5"), result);
    }

    @Test
    void fullDayRangeReturnsNumberOfWorkingDays() {
        BigDecimal result = service.calculateRequestedDays(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                List.of(),
                "FULL_DAY"
        );

        assertEquals(new BigDecimal("3"), result);
    }

    @Test
    void halfDayAcrossMultipleDaysThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.calculateRequestedDays(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 2),
                List.of(),
                "AM"
        ));
    }
}
