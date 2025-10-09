package top.deepdog.yiaicodemaster.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import top.deepdog.yiaicodemaster.model.dto.app.AppQueryRequest;
import top.deepdog.yiaicodemaster.model.entity.App;
import top.deepdog.yiaicodemaster.model.vo.AppVO;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/askyikai">程序员oi</a>
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);
}
