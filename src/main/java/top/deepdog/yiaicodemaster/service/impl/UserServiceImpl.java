package top.deepdog.yiaicodemaster.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import top.deepdog.yiaicodemaster.exception.ErrorCode;
import top.deepdog.yiaicodemaster.exception.ThrowUtils;
import top.deepdog.yiaicodemaster.model.dto.user.UserAddRequest;
import top.deepdog.yiaicodemaster.model.dto.user.UserQueryRequest;
import top.deepdog.yiaicodemaster.model.entity.User;
import top.deepdog.yiaicodemaster.mapper.UserMapper;
import top.deepdog.yiaicodemaster.model.enums.UserRoleEnum;
import top.deepdog.yiaicodemaster.model.vo.LoginUserVO;
import top.deepdog.yiaicodemaster.model.vo.UserVO;
import top.deepdog.yiaicodemaster.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static top.deepdog.yiaicodemaster.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/askyikai">程序员oi</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${custom.key}")
    private String userAuthKey;

    @Value("${custom.adminKey}")
    private String userAuthAdminKey;

    @Value("${custom.teaUser}")
    private String teaUser;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userAuth) {
        // 1. 校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword, userAuth), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        ThrowUtils.throwIf(!userAuth.equals(userAuthKey) && !userAuth.equals(userAuthAdminKey), ErrorCode.PARAMS_ERROR, "授权码错误，请联系管理员获取");
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount);
        ThrowUtils.throwIf(this.mapper.selectCountByQuery(queryWrapper) > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        if (userAuth.equals(userAuthAdminKey)) {
            user.setUserRole(UserRoleEnum.ADMIN.getValue());
        } else {
            user.setUserRole(UserRoleEnum.USER.getValue());
        }
        user.setUserAvatar(teaUser);
        ThrowUtils.throwIf(!this.save(user), ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        // 3. 返回用户
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值
        final String salt = "master";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询用户
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getUserPassword, encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        // 4. 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5. 返回脱敏后的用户信息
        return getSafetyUser(user);
    }

    @Override
    public LoginUserVO getSafetyUser(User User) {
        if (User == null) {
            return null;
        }
        LoginUserVO safetyUser = new LoginUserVO();
        BeanUtil.copyProperties(User, safetyUser);
        return safetyUser;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User currentUser = (User)(request.getSession().getAttribute(USER_LOGIN_STATE));
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        currentUser = this.getById(currentUser.getId());
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            return true;
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).toList();
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        return QueryWrapper.create()
                .eq(User::getId, userQueryRequest.getId())
                .like(User::getUserAccount, userQueryRequest.getUserAccount())
                .like(User::getUserName, userQueryRequest.getUserName())
                .like(User::getUserProfile, userQueryRequest.getUserProfile())
                .eq(User::getUserRole, userQueryRequest.getUserRole())
                .orderBy(userQueryRequest.getSortField(), "ascend".equals(userQueryRequest.getSortOrder()));
    }

    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(getEncryptPassword(DEFAULT_PASSWORD));
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }
}
