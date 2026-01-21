package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class Arg {
    /**
     * 清稿类型
     * accept_all_revisions：指定接受所有修订；
     * delete_all_comments：删除所有批注；
     * delete_all_ink：删除所有墨迹；
     * 默认所有参数都存在枚举:accept_all_revisions,delete_all_comments,delete_all_ink
     */
    private List<String> clean_options;

    /**
     * 文字水印，操作类型为OFFICE_WATERMARK时，才有该字段
     */
    private TextWatermark text_watermark;

    /**
     * 图片水印，操作类型为OFFICE_WATERMARK时，才有该字段
     */
    private ImageWatermark image_watermark;
}
