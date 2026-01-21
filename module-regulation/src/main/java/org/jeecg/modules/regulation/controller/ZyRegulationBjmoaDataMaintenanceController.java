package org.jeecg.modules.regulation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaSyncDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouwei
 * @date 2024/12/25
 */
@Api(tags = "自研-轨道运营制度")
@RestController
@RequestMapping("/data_maintenance")
@Slf4j
public class ZyRegulationBjmoaDataMaintenanceController {

    @Autowired
    private IZyRegulationBjmoaSyncDataService iZyRegulationBjmoaSyncDataService;

    @AutoLog("自研-轨道运营制度-同步应急预案")
    @ApiOperation("同步应急预案")
    @GetMapping("/syncEmergencyRegulationFromQiqiaoToDatabase")
    public Result<?> syncEmergencyRegulationFromQiqiaoToDatabase() {
        log.info("[sync emergency regulation from qiqiao to database] start");
        iZyRegulationBjmoaSyncDataService.syncEmergencyRegulationFromQiqiaoToDatabase();
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-应急预案发布入库")
    @ApiOperation("应急预案发布入库")
    @GetMapping("/syncPublishedEmergencyRegulation")
    public Result<?> syncPublishedEmergencyRegulation(@RequestParam String qiqiaoRegulationId) {
        log.info("[sync published emergency regulation] start");
        iZyRegulationBjmoaSyncDataService.syncPublishedEmergencyRegulation(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-经营层制度修改入库")
    @ApiOperation("经营层制度修改入库")
    @GetMapping("/syncModifiedRegulation")
    public Result<?> syncModifiedRegulation(@RequestParam String qiqiaoRegulationId) {
        log.info("[sync modified regulation] start");
        iZyRegulationBjmoaSyncDataService.syncModifiedRegulation(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-经营层制度修订入库")
    @ApiOperation("经营层制度修订入库")
    @GetMapping("/syncPublishedRegulation")
    public Result<?> syncPublishedRegulation(@RequestParam String qiqiaoRegulationId) {
        log.info("[sync published regulation] start");
        iZyRegulationBjmoaSyncDataService.syncPublishedRegulation(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-经营层制度数据库同步")
    @ApiOperation("经营层制度数据库同步")
    @GetMapping("/syncRegulationToDatabase")
    public Result<?> syncRegulationToDatabase(@RequestParam String qiqiaoRegulationId) {
        log.info("[sync regulation to database] start");
        iZyRegulationBjmoaSyncDataService.syncRegulationToDatabase(qiqiaoRegulationId);
        return Result.OK();
    }

}
