package com.TimeAway.demo.restController;

import com.TimeAway.demo.dto.HolidayDTO;
import com.TimeAway.demo.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/holiday")
public class HolidayController {

    private final HolidayService holidayService;

    @Autowired
    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

        @GetMapping("all")
    public ResponseEntity<List<HolidayDTO>> getAllHolidays() {
        return new ResponseEntity<>(holidayService.findAllHolidays(), HttpStatus.OK);
    }

    @GetMapping("/find-between-dates")
    public ResponseEntity<List<HolidayDTO>> getHolidaysBetweenDates(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return new ResponseEntity<>(holidayService.findAllHolidaysByDateRange(startDate, endDate), HttpStatus.OK);
    }
    @GetMapping("/holiday-by-year")
    public ResponseEntity<List<HolidayDTO>> getHolidaysByYear(@RequestParam int year) {
        return new ResponseEntity<>(holidayService.FindHolidaysByYear(year), HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<HolidayDTO> createHoliday(@RequestBody HolidayDTO holidayDTO) {
        return new ResponseEntity<>(holidayService.addHoliday(holidayDTO), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<HolidayDTO> updateHoliday(@RequestBody HolidayDTO holidayDTO) {
        return new ResponseEntity<>(holidayService.updateHoliday(holidayDTO), HttpStatus.OK);
    }
}
