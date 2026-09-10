package com.xja.ncut.monitor.service;

import com.xja.ncut.monitor.domain.Device;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Lenovo
* @description 针对表【device(安防设备信息)】的数据库操作Service
* @createDate 2026-09-08 09:17:14
*/
@Mapper
public interface DeviceService extends IService<Device> {

}
