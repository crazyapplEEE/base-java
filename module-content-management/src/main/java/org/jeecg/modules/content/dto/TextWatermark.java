package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class TextWatermark {
    /**
     * 文字水印时必填
     * 文字水印内容
     */
    private String content;
    /**
     * 文字水印字体大小，可取值5到500，默认：25
     */
    private Integer size;
    /**
     * 文字水印字体颜色
     * 十六进制颜色值，例如：#CC00FF
     */
    private String color;
    /**
     * 文字水印透明度
     * 取值范围0-1的小数，0：完全透明，1：不透明
     * 默认值：0.5
     */
    private Double transparent;
    /**
     * 是否倾斜45度，默认false
     */
    private Boolean tilt;
    /**
     * 水印位置，可选值：
     * TOP_LEFT：顶部靠左
     * TOP_CENTER： 顶部中间
     * TOP_RIGHT：顶部靠右
     * CENTER_LEFT：中间靠左
     * CENTER：正中
     * CENTER_RIGHT：中间靠右
     * BOTTOM_LEFT：底部靠左
     * BOTTOM_CENTER：底部中间
     * BOTTOM_RIGHT：底部靠右
     * 默认值：CENTER
     */
    private String position;
    /**
     * 水印是否平铺，默认false
     */
    private Boolean tiled;
    /**
     * 字体名称
     */
    private String font_name;
    /**
     * 是否加粗
     */
    private Boolean bold;
    /**
     * 是否斜体
     */
    private Boolean italic;
    /**
     * 旋转角度，单位°，默认：-45
     */
    private Integer rotate;
}
