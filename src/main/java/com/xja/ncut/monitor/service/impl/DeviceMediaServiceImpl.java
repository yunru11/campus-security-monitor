package com.xja.ncut.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xja.ncut.monitor.domain.DeviceMedia;
import com.xja.ncut.monitor.mapper.DeviceMediaMapper;
import com.xja.ncut.monitor.service.DeviceMediaService;
import org.springframework.stereotype.Service;

@Service
public class DeviceMediaServiceImpl extends ServiceImpl<DeviceMediaMapper, DeviceMedia>
    implements DeviceMediaService {
}
