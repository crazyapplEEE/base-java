package org.jeecg.modules.content.config;

import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.content.service.impl.ContentManagementServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration @Component public class ContentManagementConfig {
    @Value("${content-management.biiAppId}") private String biiAppId;
    @Value("${content-management.biiToken}") private String biiToken;
    @Value("${content-management.bjmoaAppId}") private String bjmoaAppId;
    @Value("${content-management.bjmoaToken}") private String bjmoaToken;

    @Bean public IContentManagementService biiContentManagementService() {
        return new ContentManagementServiceImpl(biiAppId, biiToken);
    }

    @Bean public IContentManagementService bjmoaContentManagementService() {
        return new ContentManagementServiceImpl(bjmoaAppId, bjmoaToken);
    }

}
