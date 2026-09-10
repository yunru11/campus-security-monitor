package com.xja.ncut.monitor.web;

import com.xja.ncut.monitor.domain.DeviceMedia;
import com.xja.ncut.monitor.service.DeviceMediaService;
import com.xja.ncut.monitor.service.DeviceManagementService;
import com.xja.ncut.monitor.service.DeviceService;
import com.xja.ncut.monitor.web.dto.DeviceLatestMediaView;
import com.xja.ncut.monitor.web.dto.DeviceRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device")
public class DeviceController {
    private final DeviceService deviceService;
    private final DeviceMediaService deviceMediaService;
    private final DeviceManagementService deviceManagementService;

    public DeviceController(
        DeviceService deviceService,
        DeviceMediaService deviceMediaService,
        DeviceManagementService deviceManagementService
    ) {
        this.deviceService = deviceService;
        this.deviceMediaService = deviceMediaService;
        this.deviceManagementService = deviceManagementService;
    }

    @GetMapping("/list")
    public Object list() {
        return deviceService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public com.xja.ncut.monitor.domain.Device create(@RequestBody DeviceRequest request) {
        return deviceManagementService.create(request);
    }

    @PutMapping("/{id}")
    public com.xja.ncut.monitor.domain.Device update(
        @PathVariable Long id,
        @RequestBody DeviceRequest request
    ) {
        return deviceManagementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deviceManagementService.delete(id);
    }

    @GetMapping("/media/latest")
    public List<DeviceLatestMediaView> latestMedia() {
        Map<String, DeviceMedia> latestByCode = new LinkedHashMap<>();
        deviceMediaService.lambdaQuery()
            .orderByDesc(DeviceMedia::getUploadedTime)
            .orderByDesc(DeviceMedia::getId)
            .list()
            .forEach(media -> latestByCode.putIfAbsent(media.getDeviceCode(), media));

        return deviceService.lambdaQuery()
            .orderByAsc(com.xja.ncut.monitor.domain.Device::getId)
            .list()
            .stream()
            .map(device -> DeviceLatestMediaView.of(device, latestByCode.get(device.getCode())))
            .toList();
    }
}
