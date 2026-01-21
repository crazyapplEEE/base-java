package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 职务表
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
@Data @TableName("sys_position") @EqualsAndHashCode(callSuper = false) @Accessors(chain = true)
@ApiModel(value = "sys_position对象", description = "职务表") public class SysPosition {
    @TableId(type = IdType.ASSIGN_ID) @ApiModelProperty(value = "id") private String id;
    @Excel(name = "职务编码", width = 15) @ApiModelProperty(value = "职务编码") private String code;
    @Excel(name = "职务名称", width = 15) @ApiModelProperty(value = "职务名称") private String name;
    @Excel(name = "职级", width = 15, dicCode = "position_rank") @ApiModelProperty(value = "职级")
    @Dict(dicCode = "position_rank") private String postRank;
    @Excel(name = "公司id", width = 15) @ApiModelProperty(value = "公司id") private String companyId;
    @ApiModelProperty(value = "创建人") private String createBy;
    @ApiModelProperty(value = "创建时间") private String createTime;
    @ApiModelProperty(value = "修改人") private String updateBy;
    @ApiModelProperty(value = "修改时间") private String updateTime;
    @Excel(name = "组织机构编码", width = 15) @ApiModelProperty(value = "组织机构编码") private String sysOrgCode;
}
