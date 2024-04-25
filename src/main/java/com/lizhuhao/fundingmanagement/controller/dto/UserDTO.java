package com.lizhuhao.fundingmanagement.controller.dto;

import lombok.Data;

/**
 * 接受前端登录请求的参数
 */
@Data
public class UserDTO {
    private Integer id;
    private String account;
    private String password;
    private String name;
    private String token;
    private String permissions;
}
