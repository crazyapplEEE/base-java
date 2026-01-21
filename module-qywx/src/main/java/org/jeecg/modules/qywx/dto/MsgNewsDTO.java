package org.jeecg.modules.qywx.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data @Accessors(chain = true) public class MsgNewsDTO {
    private List<MsgArticleDTO> articles;
}
