package com.xja.ncut.monitor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 安防告警记录
 * @TableName alarm
 */
@TableName(value ="alarm")
@Data
public class Alarm {
    /**
     * 告警主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 告警类型
     */
    private String type;

    /**
     * 发生区域
     */
    private String area;

    /**
     * 告警等级：高、中、低
     */
    private String level;

    /**
     * 处置状态：待处置、处置中、已关闭
     */
    private String status;

    /**
     * 告警来源
     */
    private String source;

    /**
     * 识别结果或告警详情
     */
    private String detail;

    /**
     * 本次告警对应的原始上传媒体地址
     */
    private String originalPath;

    /**
     * AI 标注图片或视频预警帧地址
     */
    private String previewPath;

    /**
     * 事件发生时间
     */
    private Date eventTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Alarm other = (Alarm) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getArea() == null ? other.getArea() == null : this.getArea().equals(other.getArea()))
            && (this.getLevel() == null ? other.getLevel() == null : this.getLevel().equals(other.getLevel()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getSource() == null ? other.getSource() == null : this.getSource().equals(other.getSource()))
            && (this.getDetail() == null ? other.getDetail() == null : this.getDetail().equals(other.getDetail()))
            && (this.getOriginalPath() == null ? other.getOriginalPath() == null : this.getOriginalPath().equals(other.getOriginalPath()))
            && (this.getPreviewPath() == null ? other.getPreviewPath() == null : this.getPreviewPath().equals(other.getPreviewPath()))
            && (this.getEventTime() == null ? other.getEventTime() == null : this.getEventTime().equals(other.getEventTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getArea() == null) ? 0 : getArea().hashCode());
        result = prime * result + ((getLevel() == null) ? 0 : getLevel().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
        result = prime * result + ((getDetail() == null) ? 0 : getDetail().hashCode());
        result = prime * result + ((getOriginalPath() == null) ? 0 : getOriginalPath().hashCode());
        result = prime * result + ((getPreviewPath() == null) ? 0 : getPreviewPath().hashCode());
        result = prime * result + ((getEventTime() == null) ? 0 : getEventTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", type=").append(type);
        sb.append(", area=").append(area);
        sb.append(", level=").append(level);
        sb.append(", status=").append(status);
        sb.append(", source=").append(source);
        sb.append(", detail=").append(detail);
        sb.append(", originalPath=").append(originalPath);
        sb.append(", previewPath=").append(previewPath);
        sb.append(", eventTime=").append(eventTime);
        sb.append("]");
        return sb.toString();
    }
}
