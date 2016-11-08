package app;

import java.security.*;
import java.util.concurrent.Executor;

import app.utility.Folder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class Application extends AsyncConfigurerSupport{

    public static void main(String[] args) {
        Folder.create("logs");
        Folder.create("statistics");
        Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(Application.class, args);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("PushThread-");
        executor.initialize();
        return executor;
    }



}
