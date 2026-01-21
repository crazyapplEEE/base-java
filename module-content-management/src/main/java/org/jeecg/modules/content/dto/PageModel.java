package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor public class PageModel<T> implements Serializable {
    private List<T> records;
    private long total;
    private long pageSize;
    private long pageNo;
}
