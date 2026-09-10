package com.xja.ncut.monitor.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiDetectionObject(
    @JsonProperty("class") String className,
    Double conf
) {
}
