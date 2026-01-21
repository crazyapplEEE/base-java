package org.jeecg.modules.qywx.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data @Accessors(chain = true) public class MsgMiniProgramNoticeDTO {
    /**
     * 小程序appid，必须是与当前小程序应用关联的小程序
     */
    private String appid;
    /**
     * 点击消息卡片后的小程序页面，仅限本小程序内的页面。该字段不填则消息点击后不跳转。
     */
    private String page;
    private String title;
    private String description;
    /**
     * 是否放大第一个content_item
     */
    private Boolean emphasis_first_item;
    /**
     * 消息内容键值对，最多允许10个item
     */
    private List<MsgContentItemDTO> content_item;
}
