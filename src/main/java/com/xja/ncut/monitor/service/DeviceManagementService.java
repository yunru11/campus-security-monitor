package com.xja.ncut.monitor.service;

import com.xja.ncut.monitor.domain.Device;
import com.xja.ncut.monitor.domain.DeviceMedia;
import com.xja.ncut.monitor.web.dto.DeviceRequest;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeviceManagementService {
    private static final Set<String> VALID_STATUSES = Set.of("在线", "离线");

    private final DeviceService deviceService;
    private final DeviceMediaService deviceMediaService;

    public DeviceManagementService(DeviceService deviceService, DeviceMediaService deviceMediaService) {
        this.deviceService = deviceService;
        this.deviceMediaService = deviceMediaService;
    }

    @Transactional
    public Device create(DeviceRequest request) {
        Device device = normalizedDevice(request);
        ensureCodeAvailable(device.getCode(), null);
        if (!deviceService.save(device)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "设备新增失败，请刷新后重试");
        }
        return device;
    }

    @Transactional
    public Device update(Long id, DeviceRequest request) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "设备编号无效");
        }
        Device existing = deviceService.getById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "设备不存在");
        }

        Device device = normalizedDevice(request);
        ensureCodeAvailable(device.getCode(), id);
        device.setId(id);
        if (!deviceService.updateById(device)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "设备修改失败，请刷新后重试");
        }

        if (!existing.getCode().equals(device.getCode())) {
            deviceMediaService.lambdaUpdate()
                .eq(DeviceMedia::getDeviceCode, existing.getCode())
                .set(DeviceMedia::getDeviceCode, device.getCode())
                .update();
        }
        return device;
    }

    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "设备编号无效");
        }
        Device existing = deviceService.getById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "设备不存在");
        }
        deviceMediaService.lambdaUpdate()
            .eq(DeviceMedia::getDeviceCode, existing.getCode())
            .remove();
        if (!deviceService.removeById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "设备删除失败，请刷新后重试");
        }
    }

    private Device normalizedDevice(DeviceRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "设备信息不能为空");
        }
        String code = required(request.code(), "设备编码").toUpperCase();
        String name = required(request.name(), "设备名称");
        String type = required(request.type(), "设备类型");
        String area = required(request.area(), "所属区域");
        String status = required(request.status(), "在线状态");
        if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "在线状态只能是在线或离线");
        }

        Device device = new Device();
        device.setCode(code);
        device.setName(name);
        device.setType(type);
        device.setArea(area);
        device.setStatus(status);
        return device;
    }

    private String required(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "不能超过255个字符");
        }
        return trimmed;
    }

    private void ensureCodeAvailable(String code, Long excludedId) {
        long count = deviceService.lambdaQuery()
            .eq(Device::getCode, code)
            .ne(excludedId != null, Device::getId, excludedId)
            .count();
        if (count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "设备编码已存在");
        }
    }
}
