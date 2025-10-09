package top.deepdog.yiaicodemaster.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.deepdog.yiaicodemaster.ai.model.HtmlCodeResult;
import top.deepdog.yiaicodemaster.ai.model.MultiFileCodeResult;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult htmlCode = aiCodeGeneratorService.generateHtmlCode("做个程序员艺凯的留言板，代码不超过50行");
        Assertions.assertNotNull(htmlCode);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("请生成一个HTML代码，内容是：<div>Hello World</div>");
        Assertions.assertNotNull(multiFileCode);
    }
}