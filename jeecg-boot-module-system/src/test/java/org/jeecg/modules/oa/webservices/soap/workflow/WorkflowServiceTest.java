package org.jeecg.modules.oa.webservices.soap.workflow;

import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.service.IContentManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.jeecg.modules.oa.webservices.soap.workflow.WorkflowUtils.toTableFieldArray;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WorkflowServiceTest {
    @Autowired private WorkflowServiceImpl workflowService;
    @Autowired @Qualifier("bjmoaContentManagementService") private IContentManagementService contentManagementService;
    @Value("${oa-workflow.bjmoa-publish}") private String bjmoaPublishWorkflowId;

    @Test public void checkNull() {
        Assert.assertNotNull(workflowService);
    }

    @Test public void checkNullPortType() {
        try {
            final WorkflowServicePortType workflowServiceHttpPort = workflowService.getWorkflowServiceHttpPort();
            final int creatorOaId = 7244;
            WorkflowRequestInfo workflowRequestInfo = createWorkflowRequestInfo();
            workflowRequestInfo.setIsnextflow("0");
            final String requestId = workflowServiceHttpPort.doCreateWorkflowRequest(workflowRequestInfo, creatorOaId);
            System.out.println("requestId: " + requestId);
            Assert.assertNotNull(requestId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WorkflowRequestInfo createWorkflowRequestInfo() {
        final String creatorId = "7244";
        if (creatorId == null) {
            return null;
        }

        final String createDeptId = "870";

        final String requestName = "轨道运营-制度发布";
        final WorkflowRequestInfo result = new WorkflowRequestInfo();
        result.setRequestLevel("0");
        result.setRequestName(requestName);
        result.setCreatorId(creatorId);

        // 流程基本信息
        final WorkflowBaseInfo wbi = new WorkflowBaseInfo();
        wbi.setWorkflowId(bjmoaPublishWorkflowId);
        result.setWorkflowBaseInfo(wbi);

        final List<WorkflowRequestTableField> tableFieldList =
            convert2WorkflowRequestTableFieldList(creatorId, createDeptId);
        final WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];
        wrtri[0] = new WorkflowRequestTableRecord();
        wrtri[0].setWorkflowRequestTableFields(toTableFieldArray(tableFieldList));
        final WorkflowMainTableInfo wmi = new WorkflowMainTableInfo();
        wmi.setRequestRecords(wrtri);
        result.setWorkflowMainTableInfo(wmi);

        return result;
    }

    private List<WorkflowRequestTableField> convert2WorkflowRequestTableFieldList(final String creatorId,
        final String createDeptId) {
        if (StringUtils.isEmpty(creatorId) || StringUtils.isEmpty(createDeptId)) {
            return null;
        }

        final List<WorkflowRequestTableField> result = new ArrayList<>();
        final String curDate = DateUtils.formatDate(new Date());
        final String year = curDate.substring(0, 4);

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqbm");
            tableField.setFieldValue(createDeptId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("nf");
            tableField.setFieldValue(year);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqsj");
            tableField.setFieldValue(curDate);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("yfrq");
            tableField.setFieldValue(curDate);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xgbmld");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("ejbmld");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zbfz");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zbzz");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("gszgld");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zxswy");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xlkz");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("thrq");
            tableField.setFieldValue(curDate);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xgbmfzrhq");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("txwjglbmjbr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fwbmjbr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xgfgldhq");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        final String fileId1 = "65362f115f577c619fe952a4";
        final String fileId2 = "6524a96c5f577c619fe8dd75";
        {
            // 附件
            final StringBuilder fieldTypeSb = new StringBuilder();
            final StringBuilder fieldValueSb = new StringBuilder();

            fieldTypeSb.append("http:").append("测试制度套打.pdf").append("|");
            fieldValueSb.append(contentManagementService.getDownloadUrl(fileId1)).append("|");
            fieldTypeSb.append("http:").append("外部文件测试.pdf").append("|");
            fieldValueSb.append(contentManagementService.getDownloadUrl(fileId2)).append("|");

            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fj");
            tableField.setFieldType(fieldTypeSb.substring(0, fieldTypeSb.length() - 1));
            tableField.setFieldValue(fieldValueSb.substring(0, fieldValueSb.length() - 1));
            result.add(tableField);
        }

        for (int i = 0; i < result.size(); ++i) {
            result.get(i).setEdit(true);
            result.get(i).setView(true);
        }

        return result;
    }

}
