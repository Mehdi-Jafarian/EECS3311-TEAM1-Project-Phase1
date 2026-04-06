package com.platform.presentation.api;

import com.platform.application.ConsultantService;
import com.platform.domain.Consultant;
import com.platform.domain.TimeSlot;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ConsultantController {

    private final ConsultantService consultantService;

    public ConsultantController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @PostMapping("/consultants")
    public Consultant registerConsultant(@RequestBody Map<String, String> body) {
        return consultantService.registerConsultant(body.get("name"), body.get("email"));
    }

    @GetMapping("/consultants")
    public List<Consultant> getAllConsultants() {
        return consultantService.getAllConsultants();
    }

    @GetMapping("/consultants/{id}")
    public Consultant getConsultant(@PathVariable String id) {
        return consultantService.getConsultant(id);
    }

    @PostMapping("/consultants/{id}/timeslots")
    public TimeSlot addTimeSlot(@PathVariable String id, @RequestBody Map<String, String> body) {
        TimeSlot slot = new TimeSlot(
                UUID.randomUUID().toString(),
                id,
                LocalDate.parse(body.get("date")),
                LocalTime.parse(body.get("startTime")),
                LocalTime.parse(body.get("endTime"))
        );
        return consultantService.addTimeSlot(id, slot);
    }

    @GetMapping("/consultants/{id}/timeslots")
    public List<TimeSlot> getTimeSlots(@PathVariable String id) {
        return consultantService.getTimeSlots(id);
    }

    @PutMapping("/timeslots/{id}")
    public void updateTimeSlot(@PathVariable String id, @RequestBody Map<String, String> body) {
        String consultantId = body.get("consultantId");
        TimeSlot slot = new TimeSlot(
                id,
                consultantId,
                LocalDate.parse(body.get("date")),
                LocalTime.parse(body.get("startTime")),
                LocalTime.parse(body.get("endTime"))
        );
        consultantService.updateTimeSlot(slot);
    }
}
