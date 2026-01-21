package org.jeecg.modules.qiqiao.constants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) public class FieldFilter {
    private String fieldName;
    //    大于	gt	不可用于多选类型组件(人员、部门多选，多项选择组件等)
    //    大于且等于	ge	不可用于多选类型组件(人员、部门多选，多项选择组件等)
    //    小于	lt	不可用于多选类型组件(人员、部门多选，多项选择组件等)
    //    小于并且等于	le	不可用于多选类型组件(人员、部门多选，多项选择组件等)
    //    等于	eq	不可用于多选类型组件(人员、部门多选，多项选择组件等)
    //    不等于	ne	不可用于多选类型组件(人员、部门多选，多项选择组件等)
    //    在...之间	between	不可用于多选类型组件(人员、部门多选，多项选择组件等)
    //    模糊匹配	like	不可用于多选类型组件(人员、部门多选，多项选择组件等)
    //    为空	isNull
    //    非空	isNotNull
    //    包含	in
    //    不包含	notIn
    private String logic;
    private String value;
}
