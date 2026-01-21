package org.jeecg.modules.system.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Tong Ling
 * @date 2021/9/29
 */
@Data @NoArgsConstructor @AllArgsConstructor @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "单点登录对象", description = "单点登录对象") public class SsoLoginModel implements Serializable {
    private String loginid;
    private String stamp;
    private String token;
    @ApiModelProperty("七巧用户ID") private String qiqiaoUserId;

    private Integer oaId;
    private String roleCodes;
    private String realname;
    private String mpUserId;
    private Integer deptOaId;
    private String dept;
    private Integer companyOaId;
    private String company;
}
