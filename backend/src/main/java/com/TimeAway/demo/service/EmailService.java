package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.VacationRequestDto;

import java.util.List;

public interface EmailService {
    void sendOrderNotification(VacationRequestDto request, List<String> receiverEmails);
    void sendNotificationWithMessage(VacationRequestDto request, List<String> receiverEmails
            , String message, String statusUpdate);
}
