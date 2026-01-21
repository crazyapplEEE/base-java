package org.jeecg.common.system.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data @TableName("sys_loginid_roles") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "sys_loginid_roles对象", description = "loginid对照表") public class SysLoginidRoles
    implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) @ApiModelProperty(value = "id") private Integer id;
    @ApiModelProperty(value = "创建时间") private String createTime;
    @ApiModelProperty(value = "更新时间") private String updateTime;
    @ApiModelProperty(value = "loginid") private String loginid;
    @ApiModelProperty(value = "roles") private String roles;
}
