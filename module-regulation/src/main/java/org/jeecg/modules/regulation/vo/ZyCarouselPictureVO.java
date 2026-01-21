package org.jeecg.modules.regulation.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "轮播图VO", description = "轮播图VO")
public class ZyCarouselPictureVO {
    @ApiModelProperty("图片")
    private String picture;
    @ApiModelProperty("跳转链接")
    private String pictureUrl;
    @ApiModelProperty("图片名称")
    private String title;
    @ApiModelProperty("图片顺序")
    private Integer order;
}
