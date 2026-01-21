package org.jeecg.modules.qiqiao.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.common.vo.AdvTreeNode;
import org.jeecg.modules.qiqiao.service.IQiqiaoDepartmentService;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j @Service public class QiqiaoDepartmentServiceImpl implements IQiqiaoDepartmentService {
    @Autowired private RedisUtil redisUtil;
    @Autowired private IQiqiaoService qiqiaoService;
    @Value("${biisaas.baseUrl}") private String baseUrl;
    @Value("${biisaas.department.getById}") private String getByIdUrl;
    @Value("${biisaas.department.getChildren}") private String getChildrenUrl;
    @Value("${biisaas.department.getParent}") private String getParentUrl;
    @Value("${biisaas.department.getRoot}") private String getRootUrl;
    @Value("${biisaas.department.getByIdList}") private String getByIdListUrl;
    @Value("${biisaas.department.deptTreeSelectRedisKey}") private String deptTreeSelectRedisKey;

    @Override public JSONObject getByDepartmentId(final String departmentId) {
        if (StringUtils.isEmpty(departmentId)) {
            return null;
        }
        final String requestUrl = baseUrl + getByIdUrl.replace("{departmentId}", departmentId);
        return qiqiaoService.simpleGetJsonObject(requestUrl);
    }

    @Override public JSONArray getChildren(String departmentId) {
        if (StringUtils.isEmpty(departmentId)) {
            return null;
        }
        final String requestUrl = baseUrl + getChildrenUrl.replace("{departmentId}", departmentId);
        return qiqiaoService.simpleGetJsonArray(requestUrl);
    }

    @Override public JSONObject getParent(String departmentId) {
        if (StringUtils.isEmpty(departmentId)) {
            return null;
        }
        final String requestUrl = baseUrl + getParentUrl.replace("{departmentId}", departmentId);
        return qiqiaoService.simpleGetJsonObject(requestUrl);
    }

    @Override public JSONArray getRoot() {
        final String requestUrl = baseUrl + getRootUrl;
        return qiqiaoService.simpleGetJsonArray(requestUrl);
    }

    @Override public JSONArray getByDepartmentIdList(List<String> departmentIdList) {
        if (CollectionUtils.isEmpty(departmentIdList)) {
            return null;
        }
        final StringBuilder requestUrl = new StringBuilder(baseUrl + getByIdListUrl + "?");
        for (String departmentId : departmentIdList) {
            requestUrl.append("&departmentIds=").append(departmentId);
        }
        return qiqiaoService.simpleGetJsonArray(requestUrl.toString());
    }

    @Override public List<AdvTreeNode> getDeptTreeData(String deptId, final boolean forceRefresh) {
        final String prefix = "[getDeptTreeData] ";

        boolean useRoot = false;
        if (StringUtils.isEmpty(deptId)) {
            useRoot = true;
            final JSONArray rootList = getRoot();
            if (CollectionUtils.isEmpty(rootList)) {
                log.warn(prefix + "ROOT IS EMPTY");
                return new ArrayList<>();
            }
            final JSONObject root = rootList.getJSONObject(0);
            deptId = root.getString("id");
        }

        if (StringUtils.isEmpty(deptId)) {
            log.warn(prefix + "DEPT ID IS EMPTY");
            return new ArrayList<>();
        }

        // 先在redis里面看看有没有
        final String redisKey = deptTreeSelectRedisKey + "_DEPT_ID_" + deptId;
        if (!forceRefresh) {
            final Object deptTreeSelectObj = redisUtil.get(redisKey);
            if (!ObjectUtils.isEmpty(deptTreeSelectObj)) {
                return (List<AdvTreeNode>)deptTreeSelectObj;
            }
        }

        List<AdvTreeNode> result = new ArrayList<>();
        if (useRoot) {
            final JSONArray children = getChildren(deptId);
            for (int i = 0; i < children.size(); ++i) {
                final JSONObject child = children.getJSONObject(i);
                AdvTreeNode childAdvTreeNode = convertJsonObject2TreeNode(child);
                if (childAdvTreeNode != null) {
                    result.add(childAdvTreeNode);
                }
            }
        } else {
            final JSONObject rootDepartment = getByDepartmentId(deptId);
            AdvTreeNode advTreeNode = convertJsonObject2TreeNode(rootDepartment);
            if (advTreeNode != null) {
                result.add(advTreeNode);
            }
        }

        if (CollectionUtils.isNotEmpty(result)) {
            redisUtil.del(redisKey);
            redisUtil.set(redisKey, result, 60 * 60 * 24); // store for 24 hours
        }

        return result;
    }

    private AdvTreeNode buildTree(final AdvTreeNode advTreeNode) {
        if (advTreeNode == null) {
            return null;
        }

        final String key = advTreeNode.getKey();
        if (advTreeNode.getChildren() == null) {
            final JSONArray children = getChildren(key);
            if (CollectionUtils.isNotEmpty(children)) {
                final List<AdvTreeNode> childrenList = new ArrayList<>(children.size());
                for (int i = 0; i < children.size(); ++i) {
                    final JSONObject child = children.getJSONObject(i);
                    AdvTreeNode childAdvTreeNode = convertJsonObject2TreeNode(child);
                    if (childAdvTreeNode == null) {
                        continue;
                    }
                    childrenList.add(childAdvTreeNode);
                }

                if (CollectionUtils.isNotEmpty(childrenList)) {
                    advTreeNode.setChildren(childrenList);
                }
            }
        }

        return advTreeNode;
    }

    private AdvTreeNode convertJsonObject2TreeNode(final JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.getString("qwId").startsWith("-")) {
            return null;
        }

        final AdvTreeNode advTreeNode = new AdvTreeNode();
        advTreeNode.setKey(jsonObject.getString("id"));
        advTreeNode.setTitle(jsonObject.getString("name"));
        advTreeNode.setValue(jsonObject.getString("id"));
        advTreeNode.setOrgCode(jsonObject.getString("qwId"));
        return buildTree(advTreeNode);
    }
}
