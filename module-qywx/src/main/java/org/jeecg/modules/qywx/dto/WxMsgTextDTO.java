package org.jeecg.modules.qywx.dto;

import lombok.Data;
import org.jeecg.modules.qywx.enums.MsgType;

@Data public class WxMsgTextDTO extends WxMsgDTO {
    private String msgtype = MsgType.TEXT;
    private String agentid;
    private MsgTextDTO text;
}
