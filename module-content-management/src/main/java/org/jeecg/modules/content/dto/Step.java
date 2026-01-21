package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class Step {
    /**
     * 操作类型，可选值：
     * OFFICE_CLEAN：清稿
     * OFFICE_WATERMARK：加水印
     */
    String operate;

    /**
     * 操作参数
     */
    Arg args;
}
