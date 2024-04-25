package com.lizhuhao.fundingmanagement.config;

import com.lizhuhao.fundingmanagement.config.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public JwtInterceptor jwtInterceptor(){
        return new JwtInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor()) // 拦截请求，通过判断token是否合法决定是否需要登录
                .addPathPatterns("/**")             // 拦截所有请求
                .excludePathPatterns("/user/login","/**/export","/**/import","/file/**","/budgetChange/**");   //放行请求
    }
}
