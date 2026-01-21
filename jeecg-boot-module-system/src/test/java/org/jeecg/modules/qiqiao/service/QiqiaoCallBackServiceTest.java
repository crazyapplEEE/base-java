package org.jeecg.modules.qiqiao.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.JeecgSystemApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouwei
 * @date 2024/9/26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QiqiaoCallBackServiceTest {

    @Autowired
    private IQiqiaoCallBackService qiqiaoCallBackService;
    @Value("${biisaas.bjmoaRegulationInfo.applicationId}")
    private String bjmoaRegulationInfoApplicationId;

    //  http://dy.bii.com.cn:18081/plus/cgi-bin/open/callback?corpId=wwae49a5f164f53f25&secret=04865c9ce85c48719eb85b7973cd06ff&account=zhouwei2&applicationId=f3feabed8e0a41eebeebbf37e78c6362&taskId=key_1724824391203_150270

    @Test
    public void callBack() {
        String taskId = "key_1727251805668_122838";
        String qiqiaoRegulationId = "8925213998429986816";
        String yfrq = "2024-09-26";
        Map<String, String> data = new HashMap<>(2);
        data.put("qiqiaoRegulationId", qiqiaoRegulationId);
        data.put("publishTime", yfrq);
        JSONObject jsonObject = qiqiaoCallBackService.callBack(bjmoaRegulationInfoApplicationId, taskId, data);
        System.out.println(jsonObject);
    }
}
