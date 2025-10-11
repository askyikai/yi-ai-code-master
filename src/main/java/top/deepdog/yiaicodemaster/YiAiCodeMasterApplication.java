package top.deepdog.yiaicodemaster;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("top.deepdog.yiaicodemaster.mapper")
public class YiAiCodeMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(YiAiCodeMasterApplication.class, args);
    }

}
