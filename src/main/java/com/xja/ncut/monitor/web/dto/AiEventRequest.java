package com.xja.ncut.monitor.web.dto;

import java.util.List;

public record AiEventRequest(
    String image,
    String imagePath,
    String area,
    String deviceCode,
    String mediaType,
    String mediaName,
    String mediaUrl,
    String previewUrl,
    List<AiDetectionObject> objects
) {
    public AiEventRequest(
        String image,
        String imagePath,
        String area,
        List<AiDetectionObject> objects
    ) {
        this(image, imagePath, area, null, null, null, null, null, objects);
    }
}
