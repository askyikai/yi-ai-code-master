package top.deepdog.yiaicodemaster.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import top.deepdog.yiaicodemaster.model.dto.user.UserAddRequest;
import top.deepdog.yiaicodemaster.model.dto.user.UserQueryRequest;
import top.deepdog.yiaicodemaster.model.entity.User;
import top.deepdog.yiaicodemaster.model.vo.LoginUserVO;
import top.deepdog.yiaicodemaster.model.vo.UserVO;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/askyikai">程序员oi</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      请求
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param User 原始用户信息
     * @return 脱敏后的用户信息
     */
    LoginUserVO getSafetyUser(User User);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 单个用户信息脱敏
     *
     * @param user 原始用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 批量用户信息脱敏
     * @param userList 原始用户信息列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     * @param userQueryRequest 查询条件
     * @return 查询条件
     */
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    Long addUser(UserAddRequest userAddRequest);
}
