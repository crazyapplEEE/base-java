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

@Data @TableName("sys_token_user") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "sys_token_user对象", description = "token与user对照表") public class SysTokenUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) @ApiModelProperty(value = "id") private Integer id;
    @ApiModelProperty(value = "创建时间") private String createTime;
    @ApiModelProperty(value = "token") private String token;
    @ApiModelProperty(value = "loginid") private String loginid;
    @ApiModelProperty(value = "oaId") private Integer oaId;
    @ApiModelProperty(value = "roleCodes") private String roleCodes;
    @ApiModelProperty(value = "mpUserId") private String mpUserId;
    @ApiModelProperty(value = "deptOaId") private Integer deptOaId;
    @ApiModelProperty(value = "dept") private String dept;
    @ApiModelProperty(value = "company") private String company;
    @ApiModelProperty(value = "companyOaId") private Integer companyOaId;
    @ApiModelProperty(value = "realname") private String realname;
}
