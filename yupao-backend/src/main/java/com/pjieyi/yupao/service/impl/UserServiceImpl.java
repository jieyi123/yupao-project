package com.pjieyi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pjieyi.yupao.common.ErrorCode;
import com.pjieyi.yupao.mapper.UserMapper;
import com.pjieyi.yupao.model.dto.response.CaptureResponse;
import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.service.UserService;
import com.pjieyi.yupao.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.pjieyi.yupao.constant.UserConstant.ADMIN_ROLE;
import static com.pjieyi.yupao.constant.UserConstant.USER_LOGIN_STATE;
import static com.pjieyi.yupao.utils.AliyunIdentifyCode.getParams;
import static com.pjieyi.yupao.utils.SMSUtils.sendMessage;
import static com.pjieyi.yupao.utils.ValidateCodeUtils.generateValidateCode;


/**
 * 用户服务实现类
 *
 * @author pjieyi
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    private  static  String code="1231";

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "pjieyi";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String phone,String verifyCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //校验验证码
        if (!verifyCode.equals(code)){
            throw new BusinessException(ErrorCode.CODE_ERROR,"验证码错误");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
            }
            //手机号不能重复
            QueryWrapper<User> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.eq("phone",phone);
            Long count1 = userMapper.selectCount(queryWrapper);
            if (count1>0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前手机号已被注册");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setPhone(phone);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 图片二次验证
     * @param getParams 验证参数
     * @return 图形验证响应参数
     */
    @Override
    public CaptureResponse identifyCapture(Map<String, String> getParams) {
        JSONObject jsonObject = getParams(getParams);
        CaptureResponse captureResponse=new CaptureResponse();
        try {
            captureResponse.setResult(jsonObject.getString("result"));
            captureResponse.setStatus(jsonObject.getString("status"));
            captureResponse.setReason(jsonObject.getString("reason"));
            JSONObject captchaArgs = jsonObject.getJSONObject("captcha_args");
            String usedType = captchaArgs.getString("used_type");
            String userIp = captchaArgs.getString("user_ip");
            String scene = captchaArgs.getString("scene");
            String referer = captchaArgs.getString("referer");
            Map<String, String> captchaArgList = new HashMap<>();
            captchaArgList.put("usedType", usedType);
            captchaArgList.put("userIp", userIp);
            captchaArgList.put("scene", scene);
            captchaArgList.put("referer", referer);
            captureResponse.setCaptchaArgs(captchaArgList);
        }catch (RuntimeException e){
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }
        return captureResponse;
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不合法");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不合法");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Long count = userMapper.selectCount(queryWrapper);
        // 用户不存在
        if (count == 0) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在，请注册后登录");
        }
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return getSafetyUser(user);
    }

    /**
     * 手机登录
     * @param request
     * @param phone 手机号
     * @param captcha 验证码
     * @return
     */
    @Override
    public User userLogin(HttpServletRequest request, String phone, String captcha) {
        // 1. 校验
        if (StringUtils.isAnyBlank(phone, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (phone.length() < 11 || phone.length() > 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不合法");
        }
        //验证码校验
        if (!captcha.equals(code)){
            throw new BusinessException(ErrorCode.CODE_ERROR);
        }

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在，请注册后登录");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return getSafetyUser(user);
    }


    /**
     * 获取验证码
     * @param phone 手机号
     * @return 验证码
     */
    public String getCaptcha(String phone){
        code=generateValidateCode(6).toString();
        sendMessage("originai","SMS_464995252",phone,code);
        log.info("验证码："+code);
        return code;
    }

    /**
     * 找回密码
     * @param userPassword 新密码
     * @param checkPassword 确认密码
     * @param phone 电话
     * @param verifyCode 验证码
     * @return 用户id
     */
    @Override
    public long retrievePassword(String userPassword, String checkPassword, String phone, String verifyCode){
        //校验参数
        if (StringUtils.isAnyBlank(userPassword,checkPassword,phone,verifyCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不合法");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不合法");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        if (phone.length() < 11 || phone.length() > 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不合法");
        }
        //校验验证码
        if(!verifyCode.equals(code)){
            throw new BusinessException(ErrorCode.CODE_ERROR);
        }
        //数据库验证
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("phone",phone);
        User user = userMapper.selectOne(queryWrapper);
        if (user ==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"当前手机号尚未注册");
        }
        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        user.setUserPassword(encryptPassword);
        userMapper.updateById(user);
        return user.getId();
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return getSafetyUser(currentUser);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 修改密码
     * @param id 用户id
     * @param oldPass 旧密码
     * @param newPass 新密码
     * @param confirmPass 确认密码
     * @return
     */
    @Override
    public boolean updatePassword(Long id,String oldPass,String newPass,String confirmPass){
        //校验参数
        if (StringUtils.isAnyBlank(oldPass,newPass,confirmPass) ||id==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (newPass.length() < 8 || confirmPass.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!newPass.equals(confirmPass)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //加密
        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPass).getBytes());
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPass).getBytes());
        //校验原密码
        User userById = this.getById(id);
        if (!userById.getUserPassword().equals(encryptOldPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"原密码不正确");
        }
        //更新密码
        userById.setUserPassword(encryptNewPassword);
        return  updateById(userById);
    }

    /**
     * 信息脱敏
     * @param user 原始信息
     * @return 脱敏后的用户信息
     */
    @Override
    public   User getSafetyUser(User user) {
        if (user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        User safeUser=new User();
        safeUser.setId(user.getId());
        safeUser.setUserName(user.getUserName());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setUserAvatar(user.getUserAvatar());
        safeUser.setGender(user.getGender());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setTags(user.getTags());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setCreateTime(user.getCreateTime());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setProfile(user.getProfile());
        return safeUser;
    }

    /**
     * 根据标签搜索用户数据库查询
     * @param tagNameList 标签列表
     * @return 全部用户
     */
    @Override
    public List<User> searchUserByTagsQuery(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        //拼接tag
        // like '%Java%' and like '%Python%'
        tagNameList.forEach(tag->queryWrapper.like("tags",tag));
        return this.list(queryWrapper).stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户 内存查询
     * @param tagNameList 标签列表
     * @return 全部用户
     */
    @Override
    public List<User> searchUserByTagsMemory(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.先查询所有用户
        List<User> allUser = this.list();
        //2.根据用户在判断内存中是否含有要求的标签
        List<User> userList = allUser.stream().filter(user -> {
            String userTags = user.getTags();
            //用户没有标签
            if (StringUtils.isEmpty(userTags)) {
                return false;
            }
            userTags=userTags.toLowerCase();
            for (String tag : tagNameList) {
                if (!userTags.contains(tag.toLowerCase())) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 推荐用户
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return
     */
    @Override
    public Page<User> recommendUser(int pageNum, int pageSize, HttpServletRequest request){
        if (pageNum<0 || pageSize<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //redis查询
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
        String redisKey=String.format("yupao:user:recommend:%s",getLoginUser(request).getId());
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        //有缓存直接拿缓存数据
        if (userPage!=null){
            return userPage;
        }
        //没有缓存 增加缓存数据
        userPage = this.page(new Page<>(pageNum, pageSize));
        List<User> safeUser = userPage.getRecords().stream().map(this::getSafetyUser).collect(Collectors.toList());
        userPage.setRecords(safeUser);
        try {
            valueOperations.set(redisKey,userPage,300, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error",e);
        }
        return userPage;
    }

}




