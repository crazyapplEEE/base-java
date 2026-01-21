package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ofd stamp水印参数。仅ofd转pdf有效
 */
@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class Ofdseal {
    /**
     * 水印类型。默认：Text
     */
    private String type;
    /**
     * 水印内容
     */
    private String content;
    /**
     * 水印字体。默认字体属性：字体：Times New Roman，字号：19，颜色：#0000ff
     */
    private Font font;
    /**
     * 水印横坐标偏移像素。整数数值格式。默认：0
     */
    private Integer delta_x;
    /**
     * 水印纵坐标偏移像素。整数数值格式。默认：0
     */
    private Integer delta_y;
}
