package top.deepdog.yiaicodemaster.core;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import top.deepdog.yiaicodemaster.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("做一个简单的打卡网站，总代码不超过50行",
                CodeGenTypeEnum.MULTI_FILE, 1L);
        assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("做一个简单的任务记录网站",
                CodeGenTypeEnum.MULTI_FILE, 1L);
        List<String> result = codeStream.collectList().block();
        assertNotNull(result);
        String completeContent = String.join("", result);
        assertNotNull(completeContent);
    }
}