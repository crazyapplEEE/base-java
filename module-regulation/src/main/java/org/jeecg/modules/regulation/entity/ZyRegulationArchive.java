package org.jeecg.modules.regulation.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 制度归档记录表
 */
@Data
@TableName("zy_regulation_archive")
public class ZyRegulationArchive implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 七巧制度ID（唯一） */
    private String qiqiaoRegulationId;

    /** 制度唯一标识 */
    private String identifier;

    /** 制度名称 */
    private String name;

    /** 制度编号 */
    private String code;

    /** 大类ID */
    private String categoryId;

    /** 大类名称 */
    private String categoryName;

    /** 管理类别ID */
    private String managementCategoryId;

    /** 管理类别名称 */
    private String managementCategoryName;

    /** 制度级别ID */
    private String levelId;

    /** 制度级别名称 */
    private String levelName;

    /** 制度正文文件ID */
    private String contentFileId;

    /** 发布日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date publishTime;

    /** 发布年份 */
    private Integer publishYear;

    /** 跟进人ID */
    private String qiqiaoCreatorId;

    /** 跟进人姓名 */
    private String qiqiaoCreatorName;

    /** 归档状态：0-未归档，1-已归档，2-归档失败 */
    private Integer archiveStatus;

    /** 归档成功时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archiveTime;

    /** 归档结果JSON */
    private String archiveResult;

//    /** 创建时间 */
//    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
////    @TableField(fill = FieldFill.INSERT)
//    private Date createTime;
//
//    /** 更新时间 */
//    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
////    @TableField(fill = FieldFill.INSERT_UPDATE)
//    private Date updateTime;
}