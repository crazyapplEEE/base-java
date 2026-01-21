package org.jeecg.modules.qywx.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true) public class MsgTextDTO {
    private String content;
}
