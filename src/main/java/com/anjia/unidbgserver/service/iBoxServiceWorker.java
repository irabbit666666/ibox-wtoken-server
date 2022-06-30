package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.github.unidbg.worker.Worker;
import com.github.unidbg.worker.WorkerPool;
import com.github.unidbg.worker.WorkerPoolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("iBoxServiceWorker")
public class iBoxServiceWorker implements Worker {

    private UnidbgProperties unidbgProperties;
    private WorkerPool pool;
    private iBoxService iBoxService;


    public iBoxServiceWorker() {

    }

    @Autowired
    public iBoxServiceWorker(UnidbgProperties unidbgProperties,
                           @Value("${spring.task.execution.pool.core-size:4}") int poolSize) {
        this.unidbgProperties = unidbgProperties;
        this.iBoxService = new iBoxService(unidbgProperties);
        pool = WorkerPoolFactory.create(() ->
                        new iBoxServiceWorker(unidbgProperties.isDynarmic(), unidbgProperties.isVerbose()),
                Math.max(poolSize, 4));
        log.info("线程池为:{}", Math.max(poolSize, 4));
    }

    public iBoxServiceWorker(boolean dynarmic, boolean verbose) {
        this.unidbgProperties = new UnidbgProperties();
        unidbgProperties.setDynarmic(dynarmic);
        unidbgProperties.setVerbose(verbose);
        log.info("线程启动成功,当前版本1.1.8  友情提示: 为了算法能活久一点,请不要二次转卖,否则极容易被破解后泛滥,谢谢合作 vx:irabbit666");
        this.iBoxService = new iBoxService(unidbgProperties);
    }

    @Async
    public CompletableFuture<Object> doWork(Object param) {
        iBoxServiceWorker worker;
        Object data;
        if (this.unidbgProperties.isAsync()) {
            while (true) {
                if ((worker = pool.borrow(2, TimeUnit.SECONDS)) == null) {
                    continue;
                }
                data = worker.exec(param);
                pool.release(worker);
                break;
            }
        } else {
            synchronized (this) {
                data = this.exec(param);
            }
        }
        return CompletableFuture.completedFuture(data);
    }

    @Override
    public void close() throws IOException {
        iBoxService.destroy();
        log.debug("Destroy: {}", iBoxService);
    }

    private Object exec(Object param) {
        return iBoxService.doWork(param);
    }

}
