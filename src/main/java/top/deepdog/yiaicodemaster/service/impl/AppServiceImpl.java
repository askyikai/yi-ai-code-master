package top.deepdog.yiaicodemaster.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import top.deepdog.yiaicodemaster.constant.AppConstant;
import top.deepdog.yiaicodemaster.core.AiCodeGeneratorFacade;
import top.deepdog.yiaicodemaster.exception.BusinessException;
import top.deepdog.yiaicodemaster.exception.ErrorCode;
import top.deepdog.yiaicodemaster.exception.ThrowUtils;
import top.deepdog.yiaicodemaster.model.dto.app.AppQueryRequest;
import top.deepdog.yiaicodemaster.model.entity.App;
import top.deepdog.yiaicodemaster.mapper.AppMapper;
import top.deepdog.yiaicodemaster.model.entity.User;
import top.deepdog.yiaicodemaster.model.enums.ChatHistoryMessageTypeEnum;
import top.deepdog.yiaicodemaster.model.enums.CodeGenTypeEnum;
import top.deepdog.yiaicodemaster.model.vo.AppVO;
import top.deepdog.yiaicodemaster.model.vo.UserVO;
import top.deepdog.yiaicodemaster.service.AppService;
import org.springframework.stereotype.Service;
import top.deepdog.yiaicodemaster.service.ChatHistoryService;
import top.deepdog.yiaicodemaster.service.UserService;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/askyikai">程序员oi</a>
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(message == null || message.isEmpty(), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 验证用户权限，仅本人可以生成代码
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        // 获取生成应用的代码类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "代码生成类型错误");
        // 通过校验后，将用户消息添加到对话历史
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 调用 AI 生成代码
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 收集AI响应内容并在完成后添加到对话历史
        StringBuilder aiResponseBuilder = new StringBuilder();
        return contentFlux
                .map(chunk -> {
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(()-> {
                    String aiResponse = aiResponseBuilder.toString();
                    if (!aiResponse.isEmpty()) {
                        chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    }
                })
                .doOnError(throwable -> {
                    String errorMessage = "AI回复失败：" + throwable.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户权限，仅本人可以部署应用
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权限部署应用");
        // 4. 取出应用信息中的deployKey
        String deployKey = app.getDeployKey();
        // 5. 如果没有部署，则生成deployKey（6位大小写字母和数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 6. 获取生成的代码类型并构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 7. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成应用");
        // 8. 复制源目录到目标目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码部署失败" + e.getMessage());
        }
        // 9. 更新应用的deployKey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean update = this.updateById(updateApp);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 返回可访问的URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

    @Override
    public boolean removeById(Serializable id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "应用ID无效");
        Long appId = Long.valueOf(id.toString());
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用时删除对话历史失败：{}", e.getMessage());
        }
        return super.removeById(id);
    }
}
