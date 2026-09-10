package com.xja.ncut.monitor.web.dto;

import com.xja.ncut.monitor.domain.Device;
import com.xja.ncut.monitor.domain.DeviceMedia;
import java.util.Date;

public record DeviceLatestMediaView(
    Long deviceId,
    String deviceCode,
    String deviceName,
    String deviceType,
    String area,
    String deviceStatus,
    Long mediaId,
    String originalName,
    String mediaType,
    String mediaPath,
    String previewPath,
    Date uploadedTime
) {
    public static DeviceLatestMediaView of(Device device, DeviceMedia media) {
        return new DeviceLatestMediaView(
            device.getId(),
            device.getCode(),
            device.getName(),
            device.getType(),
            device.getArea(),
            device.getStatus(),
            media == null ? null : media.getId(),
            media == null ? null : media.getOriginalName(),
            media == null ? null : media.getMediaType(),
            media == null ? null : media.getMediaPath(),
            media == null ? null : media.getPreviewPath(),
            media == null ? null : media.getUploadedTime()
        );
    }
}
