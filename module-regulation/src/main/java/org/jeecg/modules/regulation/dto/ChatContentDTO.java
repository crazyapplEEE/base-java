package org.jeecg.modules.regulation.dto;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data public class ChatContentDTO {
    private String prompt;
    private JSONObject options;
    private int memory;
    private int top_p;
    private int max_length;
    private Double temperature;
    private String model;
    @JSONField(name = "is_knowledge") private boolean is_knowledge;
}
