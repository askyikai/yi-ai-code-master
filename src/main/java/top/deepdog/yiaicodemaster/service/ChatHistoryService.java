package top.deepdog.yiaicodemaster.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import top.deepdog.yiaicodemaster.model.dto.chathistory.ChatHistoryQueryRequest;
import top.deepdog.yiaicodemaster.model.entity.ChatHistory;
import top.deepdog.yiaicodemaster.model.entity.User;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/askyikai">程序员oi</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页获取指定应用的对话历史（游标查询）
     *
     * @param appId 应用ID
     * @param pageSize 每页大小
     * @param lastCreateTime 最后创建时间
     * @param loginUser 登录用户
     * @return 对话历史分页
     */
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    /**
     * 添加对话消息
     *
     * @param appId 应用ID
     * @param message 消息内容
     * @param messageType 消息类型
     * @param userId 用户ID
     * @return 是否添加成功
     */
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用ID删除对话消息
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    public boolean deleteByAppId(Long appId);

    /**
     * 加载指定应用的对话历史到内存中
     *
     * @param appId 应用ID
     * @param chatMemory 聊天内存
     * @param maxCount 最大数量
     * @return 加载的条数
     */
    int loadChatHistoryToMemory(long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
