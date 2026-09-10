package com.xja.ncut.monitor.web.dto;

public record DeviceRequest(
    String code,
    String name,
    String type,
    String area,
    String status
) {
}
