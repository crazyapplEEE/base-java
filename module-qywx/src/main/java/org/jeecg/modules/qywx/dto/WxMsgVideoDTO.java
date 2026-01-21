package org.jeecg.modules.qywx.dto;

import lombok.Data;
import org.jeecg.modules.qywx.enums.MsgType;

@Data public class WxMsgVideoDTO extends WxMsgDTO {
    private String msgtype = MsgType.VIDEO;
    private String agentid;
    private MsgVideoDTO video;
}
