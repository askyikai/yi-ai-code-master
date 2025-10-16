package top.deepdog.yiaicodemaster.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;
import top.deepdog.yiaicodemaster.model.dto.app.AppQueryRequest;
import top.deepdog.yiaicodemaster.model.entity.App;
import top.deepdog.yiaicodemaster.model.entity.User;
import top.deepdog.yiaicodemaster.model.vo.AppVO;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/askyikai">程序员oi</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用视图对象
     *
     * @param app 应用
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取查询包装类
     *
     * @param appQueryRequest 查询参数
     * @return 查询包装类
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);

    /**
     * 异步生成应用截图
     *
     * @param appId 应用ID
     * @param appUrl 应用URL
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);
}
