package com.xja.ncut.monitor.websocket;

@FunctionalInterface
public interface AlarmTypePublisher {
    void publish(String type);
}
