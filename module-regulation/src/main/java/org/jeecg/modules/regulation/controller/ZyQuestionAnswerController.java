package org.jeecg.modules.regulation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.entity.ZyQuestionAnswer;
import org.jeecg.modules.regulation.service.IZyQuestionAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Api(tags = "自研-制度问答记录") @RestController @RequestMapping("/qa") @Slf4j public class ZyQuestionAnswerController
    extends JeecgController<ZyQuestionAnswer, IZyQuestionAnswerService> {
    @Autowired private IZyQuestionAnswerService zyQuestionAnswerService;

    @AutoLog("自研-制度问答记录-保存") @ApiOperation("制度问答记录-保存") @PostMapping("/save")
    public Result<Boolean> save(@RequestBody ZyQuestionAnswer zyQuestionAnswer) {
        log.info("[qa/save] " + zyQuestionAnswer);
        if (zyQuestionAnswer != null) {
            final String question = zyQuestionAnswer.getQuestion();
            final String answer = zyQuestionAnswer.getAnswer();
            if (StringUtils.isNotEmpty(question) && StringUtils.isNotEmpty(answer)) {
                return Result.OK(zyQuestionAnswerService.save(zyQuestionAnswer));
            }
        }
        return Result.OK(false);
    }
}
