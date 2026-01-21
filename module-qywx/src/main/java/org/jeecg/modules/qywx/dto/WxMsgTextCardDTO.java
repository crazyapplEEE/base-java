package org.jeecg.modules.qywx.dto;

import lombok.Data;
import org.jeecg.modules.qywx.enums.MsgType;

@Data public class WxMsgTextCardDTO extends WxMsgDTO {
    private String msgtype = MsgType.TEXT_CARD;
    private String agentid;
    private MsgTextCardDTO textcard;
}
