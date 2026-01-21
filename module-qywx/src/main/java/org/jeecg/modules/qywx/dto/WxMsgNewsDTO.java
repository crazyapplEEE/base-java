package org.jeecg.modules.qywx.dto;

import lombok.Data;
import org.jeecg.modules.qywx.enums.MsgType;

@Data public class WxMsgNewsDTO extends WxMsgDTO {
    private String msgtype = MsgType.NEWS;
    private String agentid;
    private MsgNewsDTO news;
}
