package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) public class SysDataLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID) private String id; //id'
    private String createBy; //创建人登录名称
    private String createTime; //创建日期
    private String updateBy; //更新人登录名称
    private String updateTime; //更新日期
    private String dataTable; //表名
    private String dataId; //数据ID
    private String dataContent; //数据内容
    private String dataVersion; //版本号
}
