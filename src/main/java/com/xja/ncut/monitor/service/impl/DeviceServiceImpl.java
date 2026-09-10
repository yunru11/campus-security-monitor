package com.xja.ncut.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xja.ncut.monitor.domain.Device;
import com.xja.ncut.monitor.service.DeviceService;
import com.xja.ncut.monitor.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【device(安防设备信息)】的数据库操作Service实现
* @createDate 2026-09-08 09:17:14
*/
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device>
    implements DeviceService{

}




