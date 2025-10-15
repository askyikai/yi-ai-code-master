package top.deepdog.yiaicodemaster.core;

import cn.hutool.json.JSONUtil;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import top.deepdog.yiaicodemaster.ai.AiCodeGeneratorService;
import top.deepdog.yiaicodemaster.ai.AiCodeGeneratorServiceFactory;
import top.deepdog.yiaicodemaster.ai.model.HtmlCodeResult;
import top.deepdog.yiaicodemaster.ai.model.MultiFileCodeResult;
import top.deepdog.yiaicodemaster.ai.model.message.AiResponseMessage;
import top.deepdog.yiaicodemaster.ai.model.message.ToolExecutedMessage;
import top.deepdog.yiaicodemaster.ai.model.message.ToolRequestMessage;
import top.deepdog.yiaicodemaster.core.parser.CodeParserExecutor;
import top.deepdog.yiaicodemaster.core.saver.CodeFileSaverExecutor;
import top.deepdog.yiaicodemaster.exception.BusinessException;
import top.deepdog.yiaicodemaster.exception.ErrorCode;
import top.deepdog.yiaicodemaster.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 统一入口：根据类型生成并保存代码(流式)
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(result, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, codeGenTypeEnum, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 处理代码流
     *
     * @param codeStream      代码流
     * @param codeGenTypeEnum 生成类型
     * @return 处理后的代码流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    try {
                        String completeCode = codeBuilder.toString();
                        Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                        File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenTypeEnum, appId);
                        log.info("代码保存成功，保存目录：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("代码保存失败：{}", e.getMessage());
                    }
                });
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用
     *
     * @param tokenStream token流
     * @return Flux<String> 流
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
            }).onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                sink.next(JSONUtil.toJsonStr(toolRequestMessage));
            }).onToolExecuted((ToolExecution toolExecution) -> {
                ToolExecutedMessage toolExecutionMessage = new ToolExecutedMessage(toolExecution);
                sink.next(JSONUtil.toJsonStr(toolExecutionMessage));
            }).onCompleteResponse((ChatResponse chatResponse) -> {
                sink.complete();
            }).onError((Throwable error) -> {
                error.printStackTrace();
                sink.error(error);
            }).start();
        });
    }

}
