package org.jeecg.modules.qywx.dto;

import lombok.Data;
import org.jeecg.modules.qywx.enums.MsgType;

@Data public class WxMsgVoiceDTO extends WxMsgDTO {
    private String msgtype = MsgType.VOICE;
    private String agentid;
    private MsgVoiceDTO voice;
}
