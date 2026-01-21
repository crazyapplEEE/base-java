package org.jeecg.modules.regulation.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.regulation.service.IZyOaRequestBjmoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * @author MENG RAN
 * @date 2024-1-11
 */
@Api(tags = "自研-轨道运营OA流程") @RestController @RequestMapping("/oaflow/bjmoa") @Slf4j
public class ZyOaRequestBjmoaController {
    @Autowired private IZyOaRequestBjmoaService zyOaRequestBjmoaService;

    @AutoLog("自研-轨道运营OA流程-制度发文流程审批通过") @ApiOperation("制度发文流程审批通过") @PostMapping("/approve_regulation_plan")
    public Result<Boolean> approveRegulationPlan(@RequestBody String params) {
        log.info("[oaflow/bjmoa/approve_regulation] params: " + params);
        String requestId = null;
        String yfrq = null;
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            if (jsonObject == null) {
                log.warn("EMPTY PARAMS");
                return Result.OK(false);
            }

            requestId = jsonObject.getString("requestId");
            yfrq = jsonObject.getString("yfrq");
            zyOaRequestBjmoaService.approveRegulationPublish(requestId, yfrq);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return Result.OK(false);
        }
        return Result.OK(true);
    }

    @AutoLog("自研-轨道运营OA流程-制度发文流程审批驳回") @ApiOperation("制度发文流程审批驳回") @PostMapping("/reject_regulation_plan")
    public Result<Boolean> rejectRegulationPlan(@RequestBody String params) {
        log.info("[oaflow/bjmoa/reject_regulation] params: " + params);
        String requestId = null;
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            if (jsonObject == null) {
                log.warn("EMPTY PARAMS");
                return Result.OK(false);
            }

            requestId = jsonObject.getString("requestId");
            zyOaRequestBjmoaService.rejectRegulationPublish(requestId);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return Result.OK(false);
        }
        return Result.OK(true);
    }
}
