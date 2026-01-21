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
public class Font {
    /**
     * 水印字体名。默认：Times New Roman
     */
    private String font_name;
    /**
     * 	水印字号。默认：19
     */
    private String font_size;
    /**
     * 水印字体加粗，true或者false。默认：false
     */
    private Boolean bold;
    /**
     * 水印字体倾斜，true或者false。默认：false
     */
    private Boolean italic;
    /**
     * 水印字体颜色，默认：#0000ff
     */
    private String color;
}
