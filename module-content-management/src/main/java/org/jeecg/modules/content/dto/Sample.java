package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 水印字体。默认字体属性：字体：Times New Roman，字号：19，颜色：#0000ff
 */
@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class Sample {
    /**
     * 	模板文件中的书签名称，会将样章放到指定的书签位置
     */
    private String bookmark;
    /**
     * 样章类型，可选值：
     * DOCUMENT：文档
     * IMAGE: 图片
     * TEXT:文本
     */
    private String type;
    /**
     * 样章文件，当type为DOCUMENT、IMAGE时，必填
     */
    private String sample_url;
    /**
     * 文件名，当type为DOCUMENT、IMAGE时，必填，必须带后缀
     */
    private String sample_filename;
    /**
     * 样章文本，当type是TEXT时，允许为空
     */
    private String text;
}
