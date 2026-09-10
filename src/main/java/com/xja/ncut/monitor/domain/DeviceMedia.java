package com.xja.ncut.monitor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 设备最近上传的图片或视频记录。
 */
@TableName("device_media")
@Data
public class DeviceMedia {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceCode;

    private String area;

    private String originalName;

    /** 图片：image，视频：video。 */
    private String mediaType;

    /** YOLO 服务可访问的原始媒体相对路径。 */
    private String mediaPath;

    /** 标注结果或视频预警帧相对路径。 */
    private String previewPath;

    private Date uploadedTime;
}
