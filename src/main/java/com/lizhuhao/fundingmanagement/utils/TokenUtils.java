package com.lizhuhao.fundingmanagement.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.lizhuhao.fundingmanagement.entity.User;
import com.lizhuhao.fundingmanagement.service.IUserService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class TokenUtils {

    private static IUserService staticUserService;

    @Autowired
    private IUserService userService;

    @PostConstruct
    public void setUserService(){
        staticUserService = userService;
    }
    /**
     * 生成token
     * @param userId
     * @param sign
     * @return
     */
    public static String genToken(String userId,String sign) {
        return JWT.create().withAudience(userId) // 将 userId 保存到 token 里面,作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) //两小时后token过期
//                .withExpiresAt(DateUtil.offsetSecond(new Date(),5))//5s后过期
                .sign(Algorithm.HMAC256(sign)); // 以 password 作为 token 的密钥
    }

    /**
     * 获取当前用户信息
     */
    public static User getCurrentUser(){
        try{
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if(StrUtil.isNotBlank(token)){
                String userId = JWT.decode(token).getAudience().get(0);
                return staticUserService.getById(userId);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
