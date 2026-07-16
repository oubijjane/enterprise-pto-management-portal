package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.HolidayDTO;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface HolidayService {
    List<HolidayDTO> findAllHolidays();
    List<HolidayDTO> FindHolidaysByYear(int year);
    HolidayDTO addHoliday(HolidayDTO holidayDTO);
    HolidayDTO updateHoliday(HolidayDTO holidayDTO);
    void deleteHoliday(HolidayDTO holidayDTO);
    List<HolidayDTO> findAllHolidaysByDateRange(LocalDate startDate, LocalDate endDate);
}
