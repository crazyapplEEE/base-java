package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 表格转换参数
 */
@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class EtPageZoom {
    /**
     * 表示是否保持当前客户端的缩放比，true表示保持当前缩放比打印，false表示以100%的缩放比打印， 当fit_pagetall或fit_pagewide中有一个为1，或都为1时，该参数不生效
     */
    private Boolean keep_pagezoom;
    /**
     * 表示是否适配所有行，0表示正常分页打印，1表示不分页，所有行在一页上
     */
    private Integer fit_pagewide;
    /**
     * 表示是否适配所有列，0表示正常分页打印，1表示不分页，所有列在一页上； 当fit_pagetall与fit_pagewide都为1时，表示将所有内容打印到一页上
     */
    private Integer fit_pagetall;
}
