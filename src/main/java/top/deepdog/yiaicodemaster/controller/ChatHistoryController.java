package top.deepdog.yiaicodemaster.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import top.deepdog.yiaicodemaster.annotation.AuthCheck;
import top.deepdog.yiaicodemaster.common.BaseResponse;
import top.deepdog.yiaicodemaster.common.ResultUtils;
import top.deepdog.yiaicodemaster.constant.UserConstant;
import top.deepdog.yiaicodemaster.exception.ErrorCode;
import top.deepdog.yiaicodemaster.exception.ThrowUtils;
import top.deepdog.yiaicodemaster.model.dto.chathistory.ChatHistoryQueryRequest;
import top.deepdog.yiaicodemaster.model.entity.ChatHistory;
import top.deepdog.yiaicodemaster.model.entity.User;
import top.deepdog.yiaicodemaster.service.ChatHistoryService;
import top.deepdog.yiaicodemaster.service.UserService;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/askyikai">程序员oi</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private UserService userService;

    /**
     * 分页获取指定应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       每页大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        HttpServletRequest
     * @return 对话历史分页
     */
    @GetMapping("app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistoryBy(@PathVariable Long appId,
                                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                                @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 管理员分页查询对话历史
     *
     * @param chatHistoryQueryRequest 查询参数
     * @return 对话历史分页
     */
    @PostMapping("admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = chatHistoryQueryRequest.getPageNum();
        int pageSize = chatHistoryQueryRequest.getPageSize();
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(
                new Page<>(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(chatHistoryPage);
    }

}
