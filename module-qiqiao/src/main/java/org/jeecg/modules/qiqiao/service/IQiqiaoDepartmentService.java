package org.jeecg.modules.qiqiao.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.common.vo.AdvTreeNode;

import java.util.List;

public interface IQiqiaoDepartmentService {
    JSONObject getByDepartmentId(String departmentId);

    JSONArray getChildren(String departmentId);

    JSONObject getParent(String departmentId);

    JSONArray getRoot();

    JSONArray getByDepartmentIdList(List<String> departmentIdList);

    List<AdvTreeNode> getDeptTreeData(String deptId, boolean refresh);
}
