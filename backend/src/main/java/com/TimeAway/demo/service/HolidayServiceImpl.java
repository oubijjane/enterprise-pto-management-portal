package com.TimeAway.demo.service;

import com.TimeAway.demo.dao.HolidayRepository;
import com.TimeAway.demo.dto.HolidayDTO;
import com.TimeAway.demo.dto.HolidayUpdatedEvent;
import com.TimeAway.demo.entity.Holiday;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class HolidayServiceImpl implements HolidayService {
    private final HolidayRepository holidayRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public HolidayServiceImpl(HolidayRepository holidayRepository, ApplicationEventPublisher eventPublisher) {
        this.holidayRepository = holidayRepository;
        this.eventPublisher = eventPublisher;
    }
    @Override
    public List<HolidayDTO> findAllHolidays() {
        return holidayRepository.findAll().stream().map(
                holiday ->
                        new HolidayDTO(holiday.getId(), holiday.getName(),
                                holiday.getDate(),  holiday.getType(), holiday.getStatus(), holiday.getNumberOfDays())
        ).toList();
    }

    @Override
    public List<HolidayDTO> FindHolidaysByYear(int year) {

       return holidayRepository.findAllDatesByYear(year).stream().map(
                holiday ->
                        new HolidayDTO(holiday.getId(), holiday.getName(),
                                holiday.getDate(),  holiday.getType(), holiday.getStatus(), holiday.getNumberOfDays())
        ).toList();
    }

    @Override
    public HolidayDTO addHoliday(HolidayDTO holidayDTO) {
        Holiday holiday = new Holiday();
        holiday.setName(holidayDTO.name());
        holiday.setDate(holidayDTO.date());
        holiday.setType(holidayDTO.type());
        holiday.setStatus(holidayDTO.status());
        holiday.setNumberOfDays(holidayDTO.numberOfDays());
        Holiday savedHoliday = holidayRepository.save(holiday);
        return new HolidayDTO(
                savedHoliday.getId(), // The crucial missing piece!
                savedHoliday.getName(),
                savedHoliday.getDate(),
                savedHoliday.getType(),
                savedHoliday.getStatus(),
                savedHoliday.getNumberOfDays()
        );
    }

    @Override
    @Transactional
    public HolidayDTO updateHoliday(HolidayDTO holidayDTO) {
        Holiday holiday = holidayRepository.findById(holidayDTO.id()).orElse(null);
        if (holiday == null) {
            throw new RuntimeException("Holiday with id " + holidayDTO.id() + " not found");
        }
        LocalDate oldDate = holiday.getDate();
        BigDecimal oldNumberOfDays = holiday.getNumberOfDays();
        holiday.setName(holidayDTO.name());
        holiday.setDate(holidayDTO.date());
        holiday.setType(holidayDTO.type());
        holiday.setStatus(holidayDTO.status());
        holiday.setNumberOfDays(holidayDTO.numberOfDays());
        Holiday savedHoliday = holidayRepository.save(holiday);
        HolidayDTO updatedHolidayDTO = new HolidayDTO(savedHoliday.getId(), savedHoliday.getName(),
                savedHoliday.getDate(),savedHoliday.getType(), savedHoliday.getStatus(), savedHoliday.getNumberOfDays());

        publishEventIfChanged(savedHoliday, oldDate, oldNumberOfDays);

        return updatedHolidayDTO;
    }

    @Override
    public void deleteHoliday(HolidayDTO holidayDTO) {

    }

    @Override
    public List<HolidayDTO> findAllHolidaysByDateRange(LocalDate startDate, LocalDate endDate) {
        return  holidayRepository.findHolidaysBetweenDates(startDate, endDate).stream().map(
                holiday -> new HolidayDTO(holiday.getId(), holiday.getName(), holiday.getDate(), holiday.getType(), holiday.getStatus(), holiday.getNumberOfDays())
        ).toList();
    }
    private void publishEventIfChanged(Holiday savedHoliday, LocalDate oldDate, BigDecimal oldNumberOfDays) {
        boolean dateChanged = !oldDate.equals(savedHoliday.getDate());
        boolean durationChanged = oldNumberOfDays.compareTo(savedHoliday.getNumberOfDays()) != 0;

        if (dateChanged || durationChanged) {
            eventPublisher.publishEvent(new HolidayUpdatedEvent(
                    savedHoliday.getId(),
                    oldDate,
                    oldNumberOfDays,
                    savedHoliday.getDate(),
                    savedHoliday.getNumberOfDays()
            ));
        }
    }
}
