package org.jeecg.modules.qywx.dto;

import lombok.Data;
import org.jeecg.modules.qywx.enums.MsgType;

@Data public class WxMsgMiniProgramNoticeDTO extends WxMsgDTO {
    private String msgtype = MsgType.MINI_PROGRAM_NOTICE;
    private MsgMiniProgramNoticeDTO miniprogram_notice;
    /**
     * 表示是否开启id转译，0表示否，1表示是，默认0。仅第三方应用需要用到，企业自建应用可以忽略。
     */
    private Integer enable_id_trans = 0;

    /**
     * 表示是否开启重复消息检查，0表示否，1表示是，默认0
     */
    private Integer enable_duplicate_check = 0;
}
