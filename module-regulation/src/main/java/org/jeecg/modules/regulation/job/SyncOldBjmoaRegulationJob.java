package org.jeecg.modules.regulation.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.common.constant.ApplicationProfile;
import org.jeecg.modules.qiqiao.service.IQiqiaoDepartmentService;
import org.jeecg.modules.regulation.service.IZyRegulationBiiService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j @Component public class SyncOldBjmoaRegulationJob {
    @Autowired private IZyRegulationBjmoaService zyRegulationBjmoaService;
    @Value("${spring.profiles.active}") private String profile;

    /*@Scheduled(cron = "0 0 1 * * ?") public void syncOldBjmoaRegulations() {
        if (ApplicationProfile.PROD.equals(profile)) {
            log.info("Started to synchronize old bjmoa regulations");
            zyRegulationBjmoaService.pullRegulationFromQiqiqao();
            log.info("Finished synchronizing old bjmoa regulations");
        }
    }*/
}
