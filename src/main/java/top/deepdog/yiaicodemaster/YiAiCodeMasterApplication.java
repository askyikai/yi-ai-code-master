package top.deepdog.yiaicodemaster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("top.deepdog.yiaicodemaster.mapper")
public class YiAiCodeMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(YiAiCodeMasterApplication.class, args);
    }

}
