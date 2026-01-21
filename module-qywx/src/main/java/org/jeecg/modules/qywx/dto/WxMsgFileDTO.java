package org.jeecg.modules.qywx.dto;

import lombok.Data;
import org.jeecg.modules.qywx.enums.MsgType;

@Data public class WxMsgFileDTO extends WxMsgDTO {
    private String msgtype = MsgType.FILE;
    private String agentid;
    private MsgFileDTO file;
}
