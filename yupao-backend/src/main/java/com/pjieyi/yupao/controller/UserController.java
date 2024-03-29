package com.pjieyi.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pjieyi.yupao.annotation.AuthCheck;
import com.pjieyi.yupao.common.BaseResponse;
import com.pjieyi.yupao.common.DeleteRequest;
import com.pjieyi.yupao.common.ErrorCode;
import com.pjieyi.yupao.common.ResultUtils;
import com.pjieyi.yupao.model.dto.response.CaptureResponse;
import com.pjieyi.yupao.model.dto.user.*;
import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.model.vo.UserVO;
import com.pjieyi.yupao.service.UserService;
import com.pjieyi.yupao.exception.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pjieyi.yupao.constant.UserConstant.USER_LOGIN_CAPTCHA;
import static com.pjieyi.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 * @author pjieyi
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    // region 登录相关

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String phone=userRegisterRequest.getPhone();
        String verifyCode = userRegisterRequest.getVerifyCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,phone,verifyCode)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword,phone,verifyCode);
        return ResultUtils.success(result);
    }



    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //if (userLoginRequest.getType().equals("account")) {
        //    String userAccount = userLoginRequest.getUserAccount();
        //    String userPassword = userLoginRequest.getUserPassword();
        //    if (StringUtils.isAnyBlank(userAccount, userPassword)) {
        //        throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //    }
        //    User user = userService.userLogin(userAccount, userPassword, request);
        //    return ResultUtils.success(user);
        //}else{ //手机号登录
        //    String phone = userLoginRequest.getPhone();
        //    String captcha = userLoginRequest.getCaptcha();
        //    if (StringUtils.isAnyBlank(phone,captcha)){
        //        throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //    }
        //    User user=userService.userLogin(request,phone,captcha);
        //    return ResultUtils.success(user);
        //
        //}
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }


    /**
     * 发送验证码
     * @param phone 手机号
     * @return
     */
    @GetMapping("/captcha")
    public BaseResponse getCaptcha(@RequestParam String phone){
        if (StringUtils.isAnyBlank(phone)){
            throw new BusinessException((ErrorCode.PARAMS_ERROR));
        }
        userService.getCaptcha(phone);
        return ResultUtils.success(phone);
    }

    /**
     * 图片二次验证
     * @param getParams 验证参数
     * @return
     */
    @GetMapping("/verifyCapture")
    public BaseResponse verifyCapture(@RequestParam Map<String,String> getParams){
        CaptureResponse captureResponse = userService.identifyCapture(getParams);
        return ResultUtils.success(captureResponse);
    }

    /**
     * 忘记密码
     * @param retrievePasswordRequest
     * @return
     */
    @PostMapping("/retrievePassword")
    public BaseResponse retrievePassword(@RequestBody RetrievePasswordRequest retrievePasswordRequest,HttpServletRequest request){
        if (retrievePasswordRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验参数
        String userPassword = retrievePasswordRequest.getUserPassword();
        String checkPassword = retrievePasswordRequest.getCheckPassword();
        String phone = retrievePasswordRequest.getPhone();
        String verifyCode = retrievePasswordRequest.getVerifyCode();
        if (StringUtils.isAnyBlank(userPassword,checkPassword,phone,verifyCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = userService.retrievePassword(userPassword, checkPassword, phone, verifyCode);
        if (request!=null && request.getSession().getAttribute(USER_LOGIN_STATE) != null) {
            // 如果当前用户已经登录 移除登录态
            request.getSession().removeAttribute(USER_LOGIN_STATE);
        }
        return ResultUtils.success(userId);
    }

    /**
     * 修改密码
     * @param passwordRequest
     * @return 用户id
     */
    @PostMapping("/updatePassword")
    public BaseResponse updatePassword(@RequestBody UserPasswordRequest passwordRequest,HttpServletRequest request){
        if (passwordRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验参数
        Long id = passwordRequest.getId();
        String oldPassword = passwordRequest.getOldPassword();
        String newPassword = passwordRequest.getNewPassword();
        String confirmPassword = passwordRequest.getConfirmPassword();
        if (StringUtils.isAnyBlank(oldPassword,newPassword,confirmPassword) ||id==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.updatePassword(id,oldPassword,newPassword,confirmPassword);
        //清空session
        this.userLogout(request);
        return ResultUtils.success(id);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("用户注销")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断用户是否存在
        if (userService.getById(userUpdateRequest.getId())==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName=userUpdateRequest.getUserName();
        if (StringUtils.isNotEmpty(userName)){
            //检查是否存在该昵称
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUserName,userName);
            long count = userService.count(queryWrapper);
            if (count>0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"昵称已存在");
            }
        }
        String phone = userUpdateRequest.getPhone();
        String code = userUpdateRequest.getCode();
        if (StringUtils.isNotEmpty(phone)){
            if (StringUtils.isBlank(code)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"请发送验证码");
            }
            //检查验证码是否错误
            if (!code.equals(redisTemplate.opsForValue().get(USER_LOGIN_CAPTCHA+phone))){
                throw new BusinessException(ErrorCode.CODE_ERROR,"验证码错误");
            }


            //检查电话号码是否存在
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            long count = userService.count(queryWrapper);
            if (count>0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"手机号已被注册");
            }
        }

        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(user.getId()) && !userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {

        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current=1;
        long size = 10;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrentPage();
            size = userQueryRequest.getPageSize();
        }

        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        String userAccount = userQuery.getUserAccount();
        String userRole = userQuery.getUserRole();
        String userName = userQuery.getUserName();
        String phone = userQuery.getPhone();
        String email = userQuery.getEmail();
        String startTime = userQueryRequest.getStartTime();
        String endTime = userQueryRequest.getEndTime();
        Integer gender = userQuery.getGender();
        //模糊查询
        if (StringUtils.isNotEmpty(userAccount)){
            queryWrapper.like(User::getUserAccount,userAccount);
        }
        if (StringUtils.isNotEmpty(userName)){
            queryWrapper.like(User::getUserName,userName);
        }
        if (StringUtils.isNotEmpty(phone)){
            queryWrapper.like(User::getPhone,phone);
        }
        if (StringUtils.isNotEmpty(email)){
            queryWrapper.like(User::getEmail,email);
        }
        if(gender != null){
            queryWrapper.like(User::getGender,gender);
        }
        if (StringUtils.isNotEmpty(userRole)){
            queryWrapper.like(User::getUserRole,userRole);
        }
        if (StringUtils.isNotEmpty(startTime) && StringUtils.isNoneEmpty(endTime)){
            //大于等于
            queryWrapper.ge(User::getCreateTime,startTime);
            //小于等于
            queryWrapper.le(User::getCreateTime,endTime);
        }
        queryWrapper.orderByDesc(User::getUpdateTime);
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchTags(@RequestParam(required=false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> users = userService.searchUserByTagsMemory(tagNameList);
        return ResultUtils.success(users);
    }

    /**
     * 获取当前用户的标签
     * @param request
     * @return
     */
    @GetMapping("/tags")
    public BaseResponse<List<String>> getUserTags(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User user = userService.getById(loginUser.getId());
        if (user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        Gson gson=new Gson();
        List<String> tagList=gson.fromJson(loginUser.getTags(),new TypeToken<List<String>>(){}.getType());
        if (tagList==null){
            tagList=new ArrayList<>();
        }
        return ResultUtils.success(tagList);
    }


    /**
     * 修改当前用户的标签
     * @param request
     * @return
     */
    @GetMapping("/update/tags")
    public BaseResponse<Boolean> updateUserTags(@RequestParam List<String> tagList,HttpServletRequest request){
        if (CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签为空");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User user = userService.getById(loginUser.getId());
        if (user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        Gson gson=new Gson();
        String userTags = gson.toJson(tagList);
        loginUser.setTags(userTags);
        boolean res = userService.updateById(loginUser);
        if (!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(res);
    }

    /**
     * 主页用户推荐
     * @param currentPage 页码
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(Integer currentPage,HttpServletRequest request){
        if (currentPage==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.recommendUser(currentPage,10,request));
    }

    /**
     * 根据标签匹配最佳用户
     * @param num 推荐人数
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<UserVO>> matchUsers(Integer num,HttpServletRequest request){
        if (num==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //可以防止缓存穿透
        if (num<=0 || num>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<UserVO> userVOList=userService.matchUsers(num,loginUser);
        return ResultUtils.success(userVOList);
    }


    // endregion
}
