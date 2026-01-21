package org.jeecg.modules.regulation.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.regulation.service.IZyRegulationArchiveBjmoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhouwei
 * @date 2024/2/27
 */
@Api(tags = "自研-制度归档") @RestController @RequestMapping("/ams/bjmoa") @Slf4j
public class ZyArchiveBjmoaController {

    @Autowired private IZyRegulationArchiveBjmoaService zyRegulationArchiveBjmoaService;

    /**
     *
     * @param jsonObject
     * {
     *      "reqid": "xxxxxx",          受理请求的唯一标识
     *      "appid": "bzsystem",        档案系统分配的规章制度系统标识
     *      "libcode": "M2",            档案分类代码
     *      "unitcode": "A040",         全宗标识
     *      "retcode": "xxxxxx",        归档结果代码，200表示归档成功
     *      "retdesc": "xxxxxx",        归档结果描述
     *      "detectionResult“: "xxxxxx" 四性检测未通过内容，值可能为{}
     * }
     * @return
     */
    @AutoLog("自研-制度归档-档案系统回调接口") @ApiOperation("档案系统回调接口") @PostMapping("/get_notice")
    public JSONObject addFileBack(@RequestBody JSONObject jsonObject) {
        log.info("addFileCallback: " + jsonObject.toString());

        // 向规章制度系统七巧端推送制度归档结果
        zyRegulationArchiveBjmoaService.pushArchiveResultToQiqiao(jsonObject);

        JSONObject result = new JSONObject();
        result.put("code", "0");
        result.put("desc", "接收成功");
        return result;
    }


    @AutoLog("自研-轨道运营制度归档-同步已发布制度")
    @ApiOperation("从七巧同步已发布的制度到本地数据库")
    @PostMapping("/sync")
    public Result<JSONObject> syncPublishedRegulations() {

        log.info("[syncPublishedRegulations]");
        JSONObject result = zyRegulationArchiveBjmoaService.syncPublishedRegulations();

        return Result.OK(result);
    }

    @AutoLog("自研-轨道运营制度归档-按年份批量归档")
    @ApiOperation("按年份批量归档推送")
    @PostMapping("/batch")
    public Result<JSONObject> batchArchiveByYear(@RequestParam Integer publishYear) {
        log.info("[batchArchiveByYear] publishYear: {}", publishYear);
        JSONObject result = zyRegulationArchiveBjmoaService.batchArchiveByYear(publishYear);
        return Result.OK(result);
    }


}
