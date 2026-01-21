package org.jeecg.modules.qiqiao.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.JeecgSystemApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QiqiaoDepartmentServiceTest {
    @Autowired private IQiqiaoDepartmentService qiqiaoDepartmentService;

    @Test public void checkNull() {
        Assert.assertNotNull(qiqiaoDepartmentService);
    }

    @Test public void getByDepartmentId() {
        // 人力资源部
        String departmentId = "ddd5727fb883427fbc2d827a2ae34818";
        JSONObject record = qiqiaoDepartmentService.getByDepartmentId(departmentId);
        Assert.assertNotNull(record);
        System.out.println(record);

        // 信息数据管理部
        departmentId = "7c1c05ac71614da0a66decf0add9cac1";
        record = qiqiaoDepartmentService.getByDepartmentId(departmentId);
        Assert.assertNotNull(record);
        System.out.println(record);
    }

    @Test public void getChildren() {
        String departmentId = "edc3eb37-30a1-4bfc-9975-49a85d9b9418";
        JSONArray recordList = qiqiaoDepartmentService.getChildren(departmentId);
        Assert.assertNotNull(recordList);
        System.out.println(recordList);
    }

    @Test public void getParent() {
        String departmentId = "edc3eb37-30a1-4bfc-9975-49a85d9b9418";
        JSONObject record = qiqiaoDepartmentService.getParent(departmentId);
        Assert.assertNull(record);
        System.out.println(record);

        departmentId = "7c1c05ac71614da0a66decf0add9cac1";
        record = qiqiaoDepartmentService.getParent(departmentId);
        Assert.assertNotNull(record);
        System.out.println(record);
    }

    @Test public void getRoot() {
        JSONArray recordList = qiqiaoDepartmentService.getRoot();
        Assert.assertNotNull(recordList);
        System.out.println(recordList);

        // [{"parentName":"","qwId":"","name":"京投智能门户","fullName":"京投智能门户","id":"edc3eb37-30a1-4bfc-9975-49a85d9b9418","parentId":"0"}]
    }

    @Test public void getByDepartmentIdList() {
        List<String> departmentIdList = new ArrayList<>(2);
        departmentIdList.add("ddd5727fb883427fbc2d827a2ae34818");
        departmentIdList.add("7c1c05ac71614da0a66decf0add9cac1");
        JSONArray recordList = qiqiaoDepartmentService.getByDepartmentIdList(departmentIdList);
        Assert.assertNotNull(recordList);
        System.out.println(recordList);
    }
}
