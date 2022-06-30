package com.anjia.unidbgserver.service;


import com.anjia.unidbgserver.config.UnidbgProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 单元测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class iBoxServiceTest {
    @Autowired
    UnidbgProperties properties;

    /**
     * 业务逻辑
     */
    iBoxService iBoxService;

    /**
     * 多线程
     */
    @Autowired
    iBoxServiceWorker iBoxServiceWorker;

    @SneakyThrows @Test
    void testiBoxService() {
        // 调用测试方法
        iBoxService = new iBoxService(properties);
        log.info("{}测试,结果:{}","iBox",iBoxService.doWork(null));
    }

    @SneakyThrows @Test
    void testiBoxServiceWorker() {
        // 调用测试方法
        log.info("{}测试,结果:{}","iBox",iBoxServiceWorker.doWork(null).get());
    }
}
