package com.lizhuhao.fundingmanagement.service;

import com.lizhuhao.fundingmanagement.controller.dto.UserDTO;
import com.lizhuhao.fundingmanagement.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-03-28
 */
public interface IUserService extends IService<User> {

    UserDTO login(UserDTO userDTO);
}
