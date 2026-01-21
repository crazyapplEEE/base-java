package org.jeecg.modules.qywx.dto;

import lombok.Data;
import org.jeecg.modules.qywx.enums.MsgType;

@Data public class WxMsgImageDTO extends WxMsgDTO {
    private String msgtype = MsgType.IMAGE;
    private String agentid;
    private MsgImageDTO image;
}
