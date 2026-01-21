package org.jeecg.modules.qywx.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true) public class MsgContentItemDTO {
    /**
     * 长度10个汉字以内
     */
    private String key;
    /**
     * 长度30个汉字以内
     */
    private String value;
}
