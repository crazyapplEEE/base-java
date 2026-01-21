package org.jeecg.modules.publicManagement.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "统一待办消息DTO", description = "统一待办消息DTO") public class QywxUnifiedAgendaMessageDTO implements Serializable {
    @ApiModelProperty("OA账号ID") Integer account;
    @ApiModelProperty("标题") String title;
    @ApiModelProperty("内容") String content;
    @ApiModelProperty("跳转链接") String jumpUrl;
}
