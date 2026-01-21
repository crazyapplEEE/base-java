package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class ImageWatermark {
    /**
     * 图片水印时必填
     * 水印图片地址
     */
    private String watermark_url;
    /**
     * 图片水印时必填
     * 图片水印的文件名，必须带后缀
     */
    private String watermark_filename;
    /**
     * 是否取消冲蚀，WPS水印的冲蚀效果参数为：亮度0.85，对比度0.15，默认false
     */
    private Boolean no_washout;
    /**
     * 	是否倾斜45度，默认false
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
     * 水印图片缩放比例，0.1-5，默认：1
     */
    private Double scale;

    /**
     * 透明度
     * 取值范围0-1的小数，0：完全透明，1：不透明
     * 默认值：1
     */
    private Double transparent;
    /**
     * 旋转角度，单位°，默认：-45
     */
    private Integer rotate;
}