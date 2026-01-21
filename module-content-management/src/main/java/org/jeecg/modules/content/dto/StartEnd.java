package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 转换页数范围
 */
@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class StartEnd {
    /**
     * 起始页码，start与end必须同时存在或同时不存在，从1开始
     */
    private Integer start;
    /**
     * 结束页码，start与end必须同时存在或同时不存在，不为空时必须end>start
     */
    private Integer end;
}
