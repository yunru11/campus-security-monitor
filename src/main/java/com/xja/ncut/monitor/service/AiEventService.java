package com.xja.ncut.monitor.service;

import com.xja.ncut.monitor.domain.Alarm;
import com.xja.ncut.monitor.domain.DeviceMedia;
import com.xja.ncut.monitor.web.dto.AiDetectionObject;
import com.xja.ncut.monitor.web.dto.AiEventRequest;
import com.xja.ncut.monitor.web.dto.AiEventResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AiEventService {
    private static final Set<String> VEHICLE_CLASSES = Set.of(
        "car", "truck", "bus", "motorcycle", "bicycle"
    );
    private static final Set<String> ANIMAL_CLASSES = Set.of(
        "dog", "cat", "bird", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe"
    );
    private static final Set<String> ITEM_CLASSES = Set.of(
        "backpack", "suitcase", "handbag"
    );
    private static final Set<String> FIRE_CLASSES = Set.of("fire", "smoke");

    private final AlarmService alarmService;
    private final DeviceMediaService deviceMediaService;
    private final double minConfidence;

    public AiEventService(
        AlarmService alarmService,
        DeviceMediaService deviceMediaService,
        @Value("${ai.event.min-confidence:0.25}") double minConfidence
    ) {
        this.alarmService = alarmService;
        this.deviceMediaService = deviceMediaService;
        this.minConfidence = minConfidence;
    }

    @Transactional
    public AiEventResponse accept(AiEventRequest request) {
        List<Detection> detections = normalizeDetections(request == null ? null : request.objects());
        String image = request == null ? null : request.image();
        String imagePath = request == null ? null : request.imagePath();
        String area = resolveArea(request == null ? null : request.area(), image, imagePath);
        Date eventTime = new Date();
        List<Alarm> alarms = new ArrayList<>();

        recordDeviceMedia(request, area, eventTime);

        List<Detection> persons = select(detections, detection -> detection.className().equals("person"));
        if (persons.size() >= 2) {
            alarms.add(createAlarm("人员聚集", area, "中", persons, image, eventTime));
        } else if (persons.size() == 1) {
            alarms.add(createAlarm("人员闯入", area, "高", persons, image, eventTime));
        }

        appendAlarm(alarms, detections, FIRE_CLASSES, "烟火异常", "高", area, image, eventTime);
        appendAlarm(alarms, detections, VEHICLE_CLASSES, "车辆异常", "中", area, image, eventTime);
        appendAlarm(alarms, detections, ANIMAL_CLASSES, "动物进入", "中", area, image, eventTime);
        appendAlarm(alarms, detections, ITEM_CLASSES, "物品异常", "中", area, image, eventTime);

        if (request != null) {
            String originalPath = trimToNull(request.mediaUrl());
            String previewPath = trimToNull(request.previewUrl());
            alarms.forEach(alarm -> {
                alarm.setOriginalPath(originalPath);
                alarm.setPreviewPath(previewPath);
            });
        }

        if (!alarms.isEmpty()) {
            alarmService.saveBatch(alarms);
        }

        String message = alarms.isEmpty()
            ? "识别结果已接收，未发现需要生成告警的目标"
            : "识别结果已接收并生成告警";
        return new AiEventResponse(true, message, detections.size(), alarms.size(), alarms);
    }

    private void recordDeviceMedia(AiEventRequest request, String area, Date uploadedTime) {
        if (request == null
            || !StringUtils.hasText(request.deviceCode())
            || !StringUtils.hasText(request.mediaUrl())) {
            return;
        }

        DeviceMedia media = new DeviceMedia();
        media.setDeviceCode(request.deviceCode().trim());
        media.setArea(area);
        String originalName = StringUtils.hasText(request.mediaName()) ? request.mediaName() : request.image();
        media.setOriginalName(StringUtils.hasText(originalName) ? originalName.trim() : "未命名媒体");
        media.setMediaType(resolveMediaType(request.mediaType(), request.image()));
        media.setMediaPath(request.mediaUrl().trim());
        media.setPreviewPath(StringUtils.hasText(request.previewUrl()) ? request.previewUrl().trim() : null);
        media.setUploadedTime(uploadedTime);
        deviceMediaService.save(media);
    }

    private String resolveMediaType(String mediaType, String filename) {
        if (StringUtils.hasText(mediaType)) {
            String normalized = mediaType.trim().toLowerCase(Locale.ROOT);
            if (normalized.equals("video")) {
                return "video";
            }
        }
        if (StringUtils.hasText(filename)) {
            String lowerName = filename.toLowerCase(Locale.ROOT);
            if (lowerName.endsWith(".mp4") || lowerName.endsWith(".avi")
                || lowerName.endsWith(".mov") || lowerName.endsWith(".mkv")
                || lowerName.endsWith(".webm")) {
                return "video";
            }
        }
        return "image";
    }

    private List<Detection> normalizeDetections(List<AiDetectionObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return List.of();
        }

        List<Detection> detections = new ArrayList<>();
        for (AiDetectionObject object : objects) {
            if (object == null || !StringUtils.hasText(object.className()) || object.conf() == null) {
                continue;
            }
            double confidence = object.conf();
            if (!Double.isFinite(confidence) || confidence < minConfidence || confidence > 1) {
                continue;
            }
            detections.add(new Detection(object.className().trim().toLowerCase(Locale.ROOT), confidence));
        }
        return detections;
    }

    private void appendAlarm(
        List<Alarm> alarms,
        List<Detection> detections,
        Set<String> acceptedClasses,
        String type,
        String level,
        String area,
        String image,
        Date eventTime
    ) {
        List<Detection> matches = select(detections, detection -> acceptedClasses.contains(detection.className()));
        if (!matches.isEmpty()) {
            alarms.add(createAlarm(type, area, level, matches, image, eventTime));
        }
    }

    private List<Detection> select(List<Detection> detections, Predicate<Detection> predicate) {
        return detections.stream().filter(predicate).toList();
    }

    private Alarm createAlarm(
        String type,
        String area,
        String level,
        List<Detection> detections,
        String image,
        Date eventTime
    ) {
        Alarm alarm = new Alarm();
        alarm.setType(type);
        alarm.setArea(area);
        alarm.setLevel(level);
        alarm.setStatus("待处置");
        alarm.setSource("AI视觉分析");
        alarm.setDetail(buildDetail(detections, image));
        alarm.setEventTime(eventTime);
        return alarm;
    }

    private String buildDetail(List<Detection> detections, String image) {
        Map<String, Double> highestConfidence = new LinkedHashMap<>();
        for (Detection detection : detections) {
            highestConfidence.merge(detection.className(), detection.confidence(), Math::max);
        }

        StringBuilder detail = new StringBuilder("双模型识别：");
        boolean first = true;
        for (Map.Entry<String, Double> entry : highestConfidence.entrySet()) {
            if (!first) {
                detail.append("、");
            }
            detail.append(entry.getKey())
                .append('(')
                .append(String.format(Locale.ROOT, "%.1f%%", entry.getValue() * 100))
                .append(')');
            first = false;
        }
        if (StringUtils.hasText(image)) {
            detail.append("；图片：").append(image.trim());
        }
        return detail.toString();
    }

    private String resolveArea(String requestArea, String image, String imagePath) {
        if (StringUtils.hasText(requestArea)) {
            return requestArea.trim();
        }

        String filename = StringUtils.hasText(image) ? image.trim() : extractFilename(imagePath);
        if (!StringUtils.hasText(filename)) {
            return "未知区域";
        }

        int dotIndex = filename.lastIndexOf('.');
        String stem = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
        String[] parts = stem.split("_");
        if (parts.length >= 2 && StringUtils.hasText(parts[1])) {
            return parts[1].trim();
        }
        return "未知区域";
    }

    private String extractFilename(String imagePath) {
        if (!StringUtils.hasText(imagePath)) {
            return null;
        }
        try {
            return Path.of(imagePath.trim()).getFileName().toString();
        } catch (RuntimeException error) {
            return imagePath.trim();
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record Detection(String className, double confidence) {
    }
}
