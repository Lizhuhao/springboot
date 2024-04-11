package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.controller.dto.UserDTO;
import com.lizhuhao.fundingmanagement.entity.User;
import com.lizhuhao.fundingmanagement.exception.ServiceException;
import com.lizhuhao.fundingmanagement.mapper.UserMapper;
import com.lizhuhao.fundingmanagement.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizhuhao.fundingmanagement.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-03-28
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final Log LOG = Log.get();
    @Override
    public UserDTO login(UserDTO userDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", userDTO.getAccount());
        queryWrapper.eq("password", userDTO.getPassword());
        queryWrapper.ne("del_flag",true);
        User user;
        try{
            user = getOne(queryWrapper);   //查询出符合账号密码的用户信息
        }catch(Exception e){
            LOG.error(e);
            throw new ServiceException(Constants.CODE_500, "系统错误");
        }
        if(user != null){
            //使用工具类复制属性
            BeanUtil.copyProperties(user,userDTO,true);
            //设置token
            String token = TokenUtils.genToken(user.getId().toString(), user.getPassword());
            userDTO.setToken(token);
            return userDTO;
        }else{
            throw new ServiceException(Constants.CODE_600, "用户名或密码错误");
        }
    }

    @Override
    public Boolean delete(Integer id) {
        User user = getById(id);
        user.setDelFlag(true);
        boolean b = updateById(user);
        return b;
    }

    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> findAll() {
        List<User> list =  userMapper.findAll();
        return list;
    }
}
