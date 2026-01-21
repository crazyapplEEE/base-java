package org.jeecg.modules.regulation.dto;

import lombok.Data;

/**
 * @author zhouwei
 * @date 2024/2/23
 */
@Data
public class RegulationFileMessageDTO {
    private String regulationName; // 制度名称
    private String fileType; // 文档类型 1:制度正文 2:5级表单
    private String code; // 文件ID
    private long fileSize; // 文件大小
    private String fileName; // 文件名称
    private String codecData; // 编码数据
    private String formatInfo; // 格式信息
}
