package top.deepdog.yiaicodemaster.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import top.deepdog.yiaicodemaster.annotation.AuthCheck;
import top.deepdog.yiaicodemaster.common.BaseResponse;
import top.deepdog.yiaicodemaster.common.DeleteRequest;
import top.deepdog.yiaicodemaster.common.ResultUtils;
import top.deepdog.yiaicodemaster.constant.UserConstant;
import top.deepdog.yiaicodemaster.exception.ErrorCode;
import top.deepdog.yiaicodemaster.exception.ThrowUtils;
import top.deepdog.yiaicodemaster.model.dto.user.*;
import top.deepdog.yiaicodemaster.model.entity.User;
import top.deepdog.yiaicodemaster.model.enums.UserRoleEnum;
import top.deepdog.yiaicodemaster.model.vo.LoginUserVO;
import top.deepdog.yiaicodemaster.model.vo.UserVO;
import top.deepdog.yiaicodemaster.service.UserService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author <a href="https://github.com/askyikai">程序员oi</a>
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册。
     *
     * @param userRegisterRequest 用户注册请求
     * @return 用户ID
     */
    @PostMapping("register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userRegister(
                userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getCheckPassword()));
    }

    /**
     * 用户登录。
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求
     * @return 脱敏后的用户信息
     */
    @PostMapping("login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword(), request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户。
     *
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    @GetMapping("get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getSafetyUser(user));
    }

    /**
     * 用户注销。
     *
     * @param request 请求
     * @return 是否注销成功
     */
    @GetMapping("logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 添加用户(管理员权限)。
     *
     * @param userAddRequest 用户添加请求
     * @return 用户ID
     */
    @PostMapping("add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        return ResultUtils.success(userService.addUser(userAddRequest));
    }

    /**
     * 获取用户(管理员权限)。
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 获取用户包装类。
     *
     * @param id 用户ID
     * @return 用户包装类
     */
    @GetMapping("get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }


    /**
     * 获取用户包装类列表(管理员权限)。
     *
     * @param userQueryRequest 用户查询请求
     * @return 用户列表
     */
    @PostMapping("list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = userQueryRequest.getPageNum();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(pageNum, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 删除用户(管理员权限)。
     *
     * @param deleteRequest 删除请求
     * @return 是否删除成功
     */
    @PostMapping("delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.removeById(deleteRequest.getId()));
    }

    /**
     * 更新用户(管理员权限)。
     *
     * @param userUpdateRequest 用户更新请求
     * @return 是否更新成功
     */
    @PostMapping("update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        return ResultUtils.success(userService.updateById(user));
    }
}
