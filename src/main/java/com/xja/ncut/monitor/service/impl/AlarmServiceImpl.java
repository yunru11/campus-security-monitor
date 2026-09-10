package com.xja.ncut.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xja.ncut.monitor.domain.Alarm;
import com.xja.ncut.monitor.service.AlarmService;
import com.xja.ncut.monitor.mapper.AlarmMapper;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
* @author Lenovo
* @description 针对表【alarm(安防告警记录)】的数据库操作Service实现
* @createDate 2026-09-08 09:17:14
*/
@Service
public class AlarmServiceImpl extends ServiceImpl<AlarmMapper, Alarm>
    implements AlarmService {

    private static final Set<String> VALID_STATUSES = Set.of("待处置", "处置中", "已关闭");
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        "待处置", Set.of("处置中", "已关闭"),
        "处置中", Set.of("已关闭"),
        "已关闭", Set.of()
    );

    @Override
    public Alarm changeStatus(Long id, String nextStatus) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "告警编号无效");
        }
        if (!StringUtils.hasText(nextStatus) || !VALID_STATUSES.contains(nextStatus.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "告警状态无效");
        }

        Alarm alarm = getById(id);
        if (alarm == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "告警不存在");
        }

        String normalizedStatus = nextStatus.trim();
        String currentStatus = alarm.getStatus();
        if (normalizedStatus.equals(currentStatus)) {
            return alarm;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(normalizedStatus)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "不允许从“" + currentStatus + "”变更为“" + normalizedStatus + "”"
            );
        }

        alarm.setStatus(normalizedStatus);
        if (!updateById(alarm)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "告警状态更新失败，请刷新后重试");
        }
        return alarm;
    }
}




