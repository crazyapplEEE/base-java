package org.jeecg.modules.oa.webservices.soap.workflow;

import java.util.List;

/**
 * @author jeecg
 */
public class WorkflowUtils {
    public static WorkflowRequestTableField[] toTableFieldArray(final List<WorkflowRequestTableField> list) {
        if (list == null) {
            return null;
        }
        return list.toArray(new WorkflowRequestTableField[0]);
    }

    public static WorkflowRequestTableRecord[] toTableRecordArray(final List<WorkflowRequestTableRecord> list) {
        if (list == null) {
            return null;
        }
        return list.toArray(new WorkflowRequestTableRecord[list.size()]);
    }

    public static WorkflowDetailTableInfo[] toDetailTableInfoArray(final List<WorkflowDetailTableInfo> list) {
        if (list == null) {
            return null;
        }
        return list.toArray(new WorkflowDetailTableInfo[list.size()]);
    }

    public static boolean isRequestIdValid(final String requestIdStr) {
        Integer requestId = null;
        try {
            requestId = Integer.valueOf(requestIdStr);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return isRequestIdValid(requestId);
    }

    public static boolean isRequestIdValid(final Integer requestId) {
        if (requestId == null) {
            return false;
        }
        return requestId >= 0;
    }

    public static String getRequestErrorMsg(final String requestIdStr) {
        Integer requestId = null;
        try {
            requestId = Integer.valueOf(requestIdStr);
        } catch (Exception e) {
            e.printStackTrace();
            return "requestId必须为整数！";
        }

        return getRequestErrorMsg(requestId);
    }

    public static String getRequestErrorMsg(final Integer requestId) {
        if (requestId == null) {
            return "流程请求ID为null";
        }

        if (requestId >= 0) {
            return "流程" + requestId + "无错误";
        }

        switch (requestId) {
            case -1: {
                return "创建流程失败";
            }
            case -2: {
                return "用户没有流程创建权限";
            }
            case -3: {
                return "创建流程基本信息失败";
            }
            case -4: {
                return "保存表单主表信息失败";
            }
            case -5: {
                return "更新紧急程度失败";
            }
            case -6: {
                return "流程操作者失败";
            }
            case -7: {
                return "流转至下一节点失败";
            }
            case -8: {
                return "节点附加操作失败";
            }
            default:
                break;
        }

        return "未知错误";
    }
}
