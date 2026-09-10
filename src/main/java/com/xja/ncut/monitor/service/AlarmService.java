package com.xja.ncut.monitor.service;

import com.xja.ncut.monitor.domain.Alarm;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Lenovo
* @description 针对表【alarm(安防告警记录)】的数据库操作Service
* @createDate 2026-09-08 09:17:14
*/
@Mapper
public interface AlarmService extends IService<Alarm> {
    Alarm changeStatus(Long id, String nextStatus);
}
