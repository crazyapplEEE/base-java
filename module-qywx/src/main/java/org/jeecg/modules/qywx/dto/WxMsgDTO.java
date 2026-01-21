package org.jeecg.modules.qywx.dto;

import lombok.Data;

@Data public class WxMsgDTO {
    /**
     * 	成员ID列表（消息接收者，多个接收者用‘|’分隔，最多支持1000个）
     */
    private String touser;
    /**
     * 部门ID列表，多个接收者用‘|’分隔，最多支持100个。
     */
    private String toparty;
    /**
     * 标签ID列表，多个接收者用‘|’分隔，最多支持100个。
     */
    private String totag;
}
