package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class WpsFormatDTO {
    /**
     * 任务ID（必须唯一）
     * 推荐: appId_docId_fileId_业务数据ID
     */
    private String task_id;
    /**
     * 文件名（需要有后缀）
     */
    private String doc_filename;
    /**
     * 需要转换的文档（例如pdf）
     */
    private String doc_url;
    /**
     * 转换后文件格式
     */
    private String target_file_format;
    /**
     * 业务唯一标识，由应用自定义，64个字符以内，仅支持字母、数字、下划线
     */
    private String scene_id;
    /**
     * 文档密码。不为空时，会参与转换PDF的过程加密方式：先进行AES/ECB/PKCS5Padding加密，再进行base64加密。密钥:XDWe0nNGxTg2yD8Gb3uUapkoA8XtKvq3
     * （通过getBytes获取字节数组）。示例：123加密后为q0rxZGJDQCG+Hu3pvHwByw==
     */
    private String doc_password;
    /**
     * 文档编辑密码。不为空时，转换为带有编辑密码的PDF。加密方式：先进行AES/ECB/PKCS5Padding加密，再进行base64加密。密钥:XDWe0nNGxTg2yD8Gb3uUapkoA8XtKvq3
     * （通过getBytes获取字节数组）。示例：123加密后为q0rxZGJDQCG+Hu3pvHwByw==
     */
    private String doc_edit_password;
    /**
     * 表格转换参数
     */
    private EtPageZoom et_page_zoom;
    /**
     * ofd stamp水印参数。仅ofd转pdf有效
     */
    private Ofdseal ofdseal;
    /**
     * pdf 转换参数。仅pdf转docx有效
     */
    private PdfConvertor pdf_convertor;

    /**
     * 模版文件地址
     */
    private String template_url;

    /**
     * 模版文件文件名
     */
    private String template_filename;


    private boolean expand_bookmark;
    /**
     * 样章列表
     */
    private List<Sample> sample_list;
    /**
     * 操作步骤，可以选择operate里面的一种或者多种进行组合操作
     */
    private List<Step> steps;
}
