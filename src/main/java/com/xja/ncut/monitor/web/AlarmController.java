package com.xja.ncut.monitor.web;

import com.xja.ncut.monitor.service.AlarmService;
import com.xja.ncut.monitor.web.dto.AlarmStatusRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alarm")
public class AlarmController {
    @Autowired
    private AlarmService alarmService;

    @GetMapping("/list")
    public Object list() {
        return alarmService.list();
    }

    @PatchMapping("/{id}/status")
    public Object changeStatus(@PathVariable Long id, @RequestBody AlarmStatusRequest request) {
        return alarmService.changeStatus(id, request == null ? null : request.status());
    }
}
