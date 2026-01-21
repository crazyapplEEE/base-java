package org.jeecg.modules.qywx.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true) public class MsgVideoDTO {
    private String media_id;
    private String title;
    private String description;
}
