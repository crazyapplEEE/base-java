package org.jeecg.common.system.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

@Data @Accessors(chain = true) @EqualsAndHashCode(callSuper = false) public class JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Dict(dicCode = "yn") @ApiModelProperty("删除标识") @TableLogic private Integer delFlag;
    @TableField("CREATE_TIME") @Excel(name = "创建日期", width = 15) @ApiModelProperty("创建日期") private String
        createTime;
    @TableField("CREATE_BY") @Excel(name = "创建人", width = 15) @ApiModelProperty("创建人") private String createBy;
    @TableField("CREATOR_ID") @ApiModelProperty("创建人OA ID") private Integer creatorId;
    @TableField("CREATE_MP_USER_ID") @ApiModelProperty("创建人OA ID") private String createMpUserId;
    @TableField("CREATE_DEPT_ID") @ApiModelProperty("创建人部门OA ID") private Integer createDeptId;
    @TableField("CREATE_DEPT") @Excel(name = "创建人部门", width = 15) @ApiModelProperty("创建人部门") private String
        createDept;
    @TableField("CREATE_SUB_COMPANY_ID") @ApiModelProperty("创建人公司OA ID") private Integer createSubCompanyId;
    @TableField("CREATE_SUB_COMPANY") @Excel(name = "创建人公司", width = 15) @ApiModelProperty("创建人公司")
    private String createSubCompany;
    @TableField("UPDATE_TIME") @Excel(name = "更新日期", width = 15) @ApiModelProperty("更新日期") private String
        updateTime;
    @TableField("UPDATE_BY") @Excel(name = "更新人", width = 15) @ApiModelProperty("更新人") private String updateBy;
    @TableField("UPDATE_MP_USER_ID") @ApiModelProperty("创建人OA ID") private String updateMpUserId;
    @TableField("UPDATE_DEPT_ID") @ApiModelProperty("更新人部门ID") private Integer updateDeptId;
    @TableField("UPDATE_DEPT") @Excel(name = "更新人部门", width = 15) @ApiModelProperty("更新人部门") private String
        updateDept;
    @TableField("UPDATER_ID") @ApiModelProperty("更新人ID") private Integer updaterId;
    @TableField("UPDATE_SUB_COMPANY_ID") @ApiModelProperty("更新人公司OA ID") private Integer updateSubCompanyId;
    @TableField("UPDATE_SUB_COMPANY") @Excel(name = "更新人公司", width = 15) @ApiModelProperty("更新人公司")
    private String updateSubCompany;
}
