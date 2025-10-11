package top.deepdog.yiaicodemaster.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.deepdog.yiaicodemaster.service.ChatHistoryService;

import java.time.Duration;

@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30L))
            .expireAfterAccess(Duration.ofMinutes(10L))
            .removalListener((key, value, cause) ->
                    log.info("AI服务实例缓存被移除，appId: {}, cause: {}", key, cause))
            .build();

    /**
     * 获取一个AI服务实例(带缓存)
     *
     * @param appId 应用ID
     * @return AI服务实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建一个AI服务实例
     *
     * @param appId 应用ID
     * @return AI服务实例
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("创建AI服务实例，appId: {}", appId);
        // 根据appId构建独立的会话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库加载历史会话到会话记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices
                .builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(1L);
    }
}
