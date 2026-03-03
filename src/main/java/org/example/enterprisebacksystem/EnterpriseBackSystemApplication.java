package org.example.enterprisebacksystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement // 开启声明式事务支持，Day2 业务代码必须用到
public class EnterpriseBackSystemApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(EnterpriseBackSystemApplication.class, args);

        // 打印启动成功的漂亮日志，方便直接点击链接
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path", "");

        log.info("\n----------------------------------------------------------\n\t" +
                "Application EnterpriseBackSystem is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "Swagger: \thttp://localhost:" + port + path + "/swagger-ui.html\n" +
                "----------------------------------------------------------");
    }
}
