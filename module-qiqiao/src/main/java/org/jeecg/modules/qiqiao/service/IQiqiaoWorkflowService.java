package org.jeecg.modules.qiqiao.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.qiqiao.constants.RecordVO;

/**
 * 流程OpenAPI
 * https://qiqiao.do1.com.cn/help/develop_manual/%E9%9B%86%E6%88%90%E4%B8%AD%E5%BF%83/%E6%B5%81%E7%A8%8BOpenAPI.html
 */
public interface IQiqiaoWorkflowService {
    /**
     * 获取流程绑定表单
     *
     * @param applicationId 应用id
     * @param processId     流程模型id
     * @return null if failed
     */
    JSONObject getWorkflowForm(String applicationId, String processId);

    /**
     * 启动流程实例
     *
     * @param recordVO
     * @param findNextApprovers 是否需要寻找下个节点审批人（如直接归档则不需要）
     * @return null if failed
     */
    JSONObject startWorkflow(RecordVO recordVO, boolean findNextApprovers);

    /**
     * 删除流程实例
     *
     * @param recordVO
     * @return null if failed
     */
    JSONObject deleteWorkflow(RecordVO recordVO);

    /**
     * 获取下一节点参与人和指定节点
     *
     * @param recordVO
     * @return null if failed
     */
    JSONObject getNextApprovers(RecordVO recordVO);
}
