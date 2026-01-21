package org.jeecg.modules.quartz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 定时任务在线管理
 * @Author: jeecg-boot
 * @Date: 2019-01-02
 * @Version: V1.0
 */
@Data @TableName("sys_quartz_job") public class QuartzJob implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID) private String id;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 删除状态
     */
    private Integer delFlag;
    /**
     * 修改人
     */
    private String updateBy;
    /**
     * 修改时间
     */
    private String updateTime;
    /**
     * 任务类名
     */
    @Excel(name = "任务类名", width = 40) private String jobClassName;
    /**
     * cron表达式
     */
    @Excel(name = "cron表达式", width = 30) private String cronExpression;
    /**
     * 参数
     */
    @Excel(name = "参数", width = 15) private String parameter;
    /**
     * 描述
     */
    @Excel(name = "描述", width = 40) private String description;
    /**
     * 状态 0正常 -1停止
     */
    @Excel(name = "状态", width = 15, dicCode = "quartz_status") @Dict(dicCode = "quartz_status") private Integer status;

}
