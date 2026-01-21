package org.jeecg;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 单体启动类（采用此类启动为单体模式）
 */
@Slf4j @SpringBootApplication @EnableScheduling @EnableAsync public class JeecgSystemApplication
    extends SpringBootServletInitializer {

    public static void main(String[] args) throws UnknownHostException {
        final ConfigurableApplicationContext application = SpringApplication.run(JeecgSystemApplication.class, args);
        final Environment env = application.getEnvironment();
        final String ip = InetAddress.getLocalHost().getHostAddress();
        final String port = env.getProperty("server.port");
        final String path = oConvertUtils.getString(env.getProperty("server.servlet.context-path"));
        final String applicationName = env.getProperty("spring.application.name");
        log.info("\n----------------------------------------------------------\n\t" + "Application " + applicationName
            + " is running! Access URLs:\n\t" + "Local: \t\thttp://localhost:" + port + path + "/\n\t"
            + "External: \thttp://" + ip + ":" + port + path + "/\n\t" + "Swagger文档: \thttp://" + ip + ":" + port + path
            + "/doc.html\n" + "----------------------------------------------------------");
    }

    @Override protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(JeecgSystemApplication.class);
    }

}
