package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 编码校验规则
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Data @TableName("sys_check_rule") @EqualsAndHashCode(callSuper = false) @Accessors(chain = true)
@ApiModel(value = "sys_check_rule对象", description = "编码校验规则") public class SysCheckRule {
    @TableId(type = IdType.ASSIGN_ID) @ApiModelProperty(value = "主键id") private String id;
    @Excel(name = "规则名称", width = 15) @ApiModelProperty(value = "规则名称") private String ruleName;
    @Excel(name = "规则Code", width = 15) @ApiModelProperty(value = "规则Code") private String ruleCode;
    @Excel(name = "规则JSON", width = 15) @ApiModelProperty(value = "规则JSON") private String ruleJson;
    @Excel(name = "规则描述", width = 15) @ApiModelProperty(value = "规则描述") private String ruleDescription;
    @Excel(name = "更新人", width = 15) @ApiModelProperty(value = "更新人") private String updateBy;
    @Excel(name = "更新时间", width = 20) @ApiModelProperty(value = "更新时间") private String updateTime;
    @Excel(name = "创建人", width = 15) @ApiModelProperty(value = "创建人") private String createBy;
    @Excel(name = "创建时间", width = 20) @ApiModelProperty(value = "创建时间") private String createTime;
}
