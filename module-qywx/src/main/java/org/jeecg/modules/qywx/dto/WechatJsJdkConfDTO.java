package org.jeecg.modules.qywx.dto;

import lombok.Data;

@Data public class WechatJsJdkConfDTO {
    private String appId;
    private String timestamp;
    private String nonceStr;
    private String signature;
}
