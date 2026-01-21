package org.jeecg.modules.regulation.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.regulation.service.IZyOaRequestBiiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * @author Tong Ling
 * @date 2023-12-08
 */
@Api(tags = "自研-京投本部OA流程") @RestController @RequestMapping("/oaflow/bii") @Slf4j
public class ZyOaRequestBiiController {
    @Autowired private IZyOaRequestBiiService zyOaRequestBiiService;

    @AutoLog("自研-京投本部OA流程-发起制度计划流程") @ApiOperation("发起制度计划流程")
    @PostMapping("/create_regulation_plan")
    public Result<String> createRegulationPlan(@RequestParam String qiqiaoUserId,
        @RequestParam String qiqiaoRegulationPlanId) {
        log.info(
            "[oaflow/bii/create_regulation_plan] qiqiaoUserId: " + qiqiaoUserId + ", qiqiaoRegulationPlanId: " + qiqiaoRegulationPlanId);
        return Result.OK(zyOaRequestBiiService.createRegulationPlan(qiqiaoUserId, qiqiaoRegulationPlanId));
    }

    @AutoLog("自研-京投本部OA流程-制度计划流程审批通过") @ApiOperation("制度计划流程审批通过")
    @PostMapping("/approve_regulation_plan") public Result<Boolean> approveRegulationPlan(@RequestBody String params) {
        log.info("[oaflow/bii/approve_regulation_plan] params: " + params);
        String requestId = null;
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            if (jsonObject == null) {
                log.warn("EMPTY PARAMS");
                return Result.OK(false);
            }

            requestId = jsonObject.getString("requestId");
            zyOaRequestBiiService.approveRegulationPlan(requestId);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return Result.OK(false);
        }
        return Result.OK(true);
    }

    @AutoLog("自研-京投本部OA流程-制度计划流程审批驳回") @ApiOperation("制度计划流程审批驳回")
    @PostMapping("/reject_regulation_plan") public Result<Boolean> rejectRegulationPlan(@RequestBody String params) {
        log.info("[oaflow/bii/reject_regulation_plan] params: " + params);
        String requestId = null;
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            if (jsonObject == null) {
                log.warn("EMPTY PARAMS");
                return Result.OK(false);
            }

            requestId = jsonObject.getString("requestId");
            zyOaRequestBiiService.rejectRegulationPlan(requestId);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return Result.OK(false);
        }
        return Result.OK(true);
    }

    @AutoLog("自研-京投本部OA流程-发起本部专项审核") @ApiOperation("发起本部专项审核")
    @PostMapping("/create_special_audit") public Result<String> createSpecialAudit(@RequestParam String qiqiaoUserId,
        @RequestParam String qiqiaoRegulationInfoId) {
        log.info(
            "[oaflow/bii/create_special_audit] qiqiaoUserId: " + qiqiaoUserId + ", qiqiaoRegulationInfoId: " + qiqiaoRegulationInfoId);
        return Result.OK(zyOaRequestBiiService.createSpecialAudit(qiqiaoUserId, qiqiaoRegulationInfoId));
    }

    @AutoLog("自研-京投本部OA流程-本部专项审核审批通过") @ApiOperation("本部专项审核审批通过")
    @PostMapping("/approve_special_audit") public Result<Boolean> approveSpecialAudit(@RequestBody String params) {
        log.info("[oaflow/bii/approve_special_audit] params: " + params);
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            if (jsonObject == null) {
                log.warn("EMPTY PARAMS");
                return Result.OK(false);
            }

            final String requestId = jsonObject.getString("requestId");
            final String creatorId = jsonObject.getString("creatorId");
            final String oaDocIds = jsonObject.getString("wshzdgj");
            zyOaRequestBiiService.approveSpecialAudit(requestId, creatorId, oaDocIds);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return Result.OK(false);
        }
        return Result.OK(true);
    }

    @AutoLog("自研-京投本部OA流程-本部专项审核审批驳回") @ApiOperation("本部专项审核审批驳回")
    @PostMapping("/reject_special_audit") public Result<Boolean> rejectSpecialAudit(@RequestBody String params) {
        log.info("[oaflow/bii/reject_special_audit] params: " + params);
        String requestId = null;
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            if (jsonObject == null) {
                log.warn("EMPTY PARAMS");
                return Result.OK(false);
            }

            requestId = jsonObject.getString("requestId");
            zyOaRequestBiiService.rejectSpecialAudit(requestId);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return Result.OK(false);
        }
        return Result.OK(true);
    }

    @AutoLog("自研-京投事业部OA流程-发起事业部专项审核") @ApiOperation("发起事业部专项审核")
    @PostMapping("/create_shiyebu_special_audit")
    public Result<String> createShiyebuSpecialAudit(@RequestParam String qiqiaoUserId,
        @RequestParam String qiqiaoRegulationInfoId) {
        log.info(
            "[oaflow/bii/create_shiyebu_special_audit] qiqiaoUserId: " + qiqiaoUserId + ", qiqiaoRegulationInfoId: " + qiqiaoRegulationInfoId);
        return Result.OK(zyOaRequestBiiService.createShiyebuSpecialAudit(qiqiaoUserId, qiqiaoRegulationInfoId));
    }

    @AutoLog("自研-京投事业部OA流程-事业部专项审核审批通过") @ApiOperation("事业部专项审核审批通过")
    @PostMapping("/approve_shiyebu_special_audit")
    public Result<Boolean> approveShiyebuSpecialAudit(@RequestBody String params) {
        log.info("[oaflow/bii/approve_shiyebu_special_audit] params: " + params);
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            if (jsonObject == null) {
                log.warn("EMPTY PARAMS");
                return Result.OK(false);
            }

            final String requestId = jsonObject.getString("requestId");
            zyOaRequestBiiService.approveShiyebuSpecialAudit(requestId);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return Result.OK(false);
        }
        return Result.OK(true);
    }

    @AutoLog("自研-京投事业部OA流程-事业部专项审核审批驳回") @ApiOperation("事业部专项审核审批驳回")
    @PostMapping("/reject_shiyebu_special_audit")
    public Result<Boolean> rejectShiyebuSpecialAudit(@RequestBody String params) {
        log.info("[oaflow/bii/reject_shiyebu_special_audit] params: " + params);
        String requestId = null;
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            if (jsonObject == null) {
                log.warn("EMPTY PARAMS");
                return Result.OK(false);
            }

            requestId = jsonObject.getString("requestId");
            zyOaRequestBiiService.rejectShiyebuSpecialAudit(requestId);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return Result.OK(false);
        }
        return Result.OK(true);
    }
}
