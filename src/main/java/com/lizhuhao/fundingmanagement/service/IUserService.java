package com.lizhuhao.fundingmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.UserDTO;
import com.lizhuhao.fundingmanagement.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    Boolean delete(Integer id);

    List<User> findAll();

    List<User> findPerson();

    User findOneByAccount(String account);

    Page<User> findPage(Integer pageNum, Integer pageSize, String name, String permissions, String startDate, String endDate);

    Boolean changePassword(User user);

    Result imp(MultipartFile file) throws IOException;

    void export(HttpServletResponse response) throws IOException;
}
