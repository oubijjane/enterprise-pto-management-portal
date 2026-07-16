package com.TimeAway.demo.service;

public interface AccrualService {
    void runMonthlyAccrual();
    void processMidYearRolloverSafe();
    void resetLastYearPTOS();
}
