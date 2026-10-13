package com.rockhill.reception_hall.payment;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/balance-reminders")
public class BalanceReminderController {

    private final BalanceReminderService reminderService;

    public BalanceReminderController(BalanceReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // active reminders (Pending / Overdue); add ?includeCompleted=true to also list paid bookings
    @GetMapping
    public List<Map<String, Object>> getReminders(@RequestParam(defaultValue = "false") boolean includeCompleted) {
        return reminderService.getReminders(includeCompleted);
    }

}
