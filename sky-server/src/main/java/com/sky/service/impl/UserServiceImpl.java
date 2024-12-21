package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParser;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        //1. 使用HTTPCLIENT工具类发送HTTP请求，获取openId

        //设置请求参数
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", userLoginDTO.getCode());
        paramMap.put("grant_type", "authorization_code");
        String response = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", paramMap);

        //解析Json格式的返回数据
        JSONObject jsonObject = JSONObject.parseObject(response);
        String openid = (String) jsonObject.get("openid");

        //如果登陆失败，抛异常
        if (openid == null)
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);

        //2.查询用户是否已经注册
        User user = userMapper.selectByOpenId(openid);

        //3.如果没注册，那么就将用户数据插入user表中
        if (user == null) {
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        //4.返回user
        return user;
    }
}
