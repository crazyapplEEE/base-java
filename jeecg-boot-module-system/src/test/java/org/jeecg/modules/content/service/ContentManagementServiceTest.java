package org.jeecg.modules.content.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.modules.content.constant.OperateConstant;
import org.jeecg.modules.content.constant.WpsOperateType;
import org.jeecg.modules.content.dto.Arg;
import org.jeecg.modules.content.dto.Step;
import org.jeecg.modules.content.dto.TextWatermark;
import org.jeecg.modules.content.dto.WpsFormatDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContentManagementServiceTest {
    @Autowired @Qualifier("biiContentManagementService") private IContentManagementService contentManagementService;
    @Value("${content-management.biiAppId}") private String biiAppId;

    @Test public void checkNull() {
        Assert.assertNotNull(contentManagementService);
    }

    @Test public void addWatermark() {
        final String fileId = "65541ae17bc2b4a96f399320";
        final String docId = "40f026d8-2e21-47a6-95a2-7474d5d44a63";

        final WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
        final String taskId = biiAppId + "_" + docId + "_" + fileId + "_" + System.currentTimeMillis();
        System.out.println(taskId);
        wpsFormatDTO.setTask_id(taskId);
        wpsFormatDTO.setScene_id(biiAppId);
        wpsFormatDTO.setDoc_url(contentManagementService.getDownloadUrl(fileId));
        wpsFormatDTO.setDoc_filename("测试合并1.pdf");

        final List<Step> steps = new ArrayList<>();
        final Step step = new Step();
        step.setOperate(OperateConstant.OFFICE_WATERMARK);
        final TextWatermark textWatermark = new TextWatermark();
        textWatermark.setContent("北京市轨道交通运营管理有限公司");
        textWatermark.setTilt(true);
        // textWatermark.setTiled(true);

        final Arg args = new Arg();
        args.setText_watermark(textWatermark);
        step.setArgs(args);
        steps.add(step);

        wpsFormatDTO.setSteps(steps);

        System.out.println(wpsFormatDTO);

        contentManagementService.officeOperate(wpsFormatDTO);
    }

    @Test public void convertPdf() {
        final String fileId = "6466f3307de2df2e30f66145";
        final String docId = "da10c3b4-3c11-4b92-8d42-93776d8cc3ec";

        WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
        // 注意：制度编号里面不能有下划线
        wpsFormatDTO.setTask_id(biiAppId + "_" + docId + "_" + fileId + "_businessId");
        wpsFormatDTO.setScene_id(biiAppId);
        wpsFormatDTO.setDoc_url(contentManagementService.getDownloadNewestUrl(docId));
        wpsFormatDTO.setDoc_filename("很重要的制度.docx");
        wpsFormatDTO.setTarget_file_format("pdf");
        contentManagementService.officeConvert(wpsFormatDTO);
    }

    @Test public void queryTask() {
        final String taskId =
            "80000130000_a70b824a-1b1b-4605-859f-d1c3b185be98_646c58d07de2df2e30f67496_7598606936944345091";
        final JSONObject jsonObject = contentManagementService.queryTask(taskId);
        System.out.println(jsonObject);
        Assert.assertNotNull(jsonObject);
    }

    @Test public void downloadConvertedFile() {
        // 将某个已经在内管上的文件转换成PDF，并上传至内管
        final String downloadId = "8c95b002b27240948999d16f06482a3b";
        final String fileName = "test.pdf";
        contentManagementService.downloadConvertedFile(downloadId, fileName, null);
    }

    @Test public void testConsumer() {
        final String fileId = "6466f3307de2df2e30f66145";
        final String docId = "da10c3b4-3c11-4b92-8d42-93776d8cc3ec";
        final String qiqiaoRegulationId = String.valueOf(new Random().nextGaussian());
        final String taskId =
            biiAppId + "@" + WpsOperateType.OFFICE_CONVERT + "@" + docId + "@" + fileId + "@" + qiqiaoRegulationId;
        System.out.println(taskId);

        WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
        // 注意：制度编号里面不能有下划线
        wpsFormatDTO.setTask_id(taskId);
        wpsFormatDTO.setScene_id(biiAppId);
        wpsFormatDTO.setDoc_url(contentManagementService.getDownloadNewestUrl(docId));
        wpsFormatDTO.setDoc_filename("很重要的制度.docx");
        wpsFormatDTO.setTarget_file_format("pdf");
        contentManagementService.officeConvert(wpsFormatDTO);
    }
}
