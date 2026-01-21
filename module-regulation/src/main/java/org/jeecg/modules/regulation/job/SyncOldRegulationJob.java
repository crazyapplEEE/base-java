package org.jeecg.modules.regulation.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.common.constant.ApplicationProfile;
import org.jeecg.modules.qiqiao.service.IQiqiaoDepartmentService;
import org.jeecg.modules.regulation.service.IZyRegulationBiiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j @Component public class SyncOldRegulationJob {
    @Autowired private IZyRegulationBiiService zyRegulationBiiService;
    @Autowired private IQiqiaoDepartmentService qiqiaoDepartmentService;
    @Value("${spring.profiles.active}") private String profile;

    @Scheduled(cron = "0 50 23 * * ?") public void syncOldRegulations() {
        if (ApplicationProfile.PROD.equals(profile)) {
            log.info("Started to synchronize old regulations");
            zyRegulationBiiService.syncOldRegulationList();
            log.info("Finished synchronizing old regulations");
        }
    }

    @Scheduled(cron = "0 30 23 * * ?") public void getDeptTreeData() {
        log.info("Started to cache qiqiao department tree data");
        qiqiaoDepartmentService.getDeptTreeData(null, true);
        log.info("Finished caching qiqiao department tree data");
    }
}
