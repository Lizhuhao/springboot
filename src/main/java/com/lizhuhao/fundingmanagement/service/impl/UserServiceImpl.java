package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.UserDTO;
import com.lizhuhao.fundingmanagement.entity.User;
import com.lizhuhao.fundingmanagement.exception.ServiceException;
import com.lizhuhao.fundingmanagement.mapper.UserMapper;
import com.lizhuhao.fundingmanagement.service.IUserService;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
import com.lizhuhao.fundingmanagement.utils.TokenUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
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
        return userMapper.findAll();
    }

    @Override
    public List<User> findPerson() {
        return userMapper.findPerson();
    }

    @Override
    public User findOneByAccount(String account) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account",account);
        queryWrapper.ne("del_flag",true);
        return getOne(queryWrapper);
    }

    @Override
    public Page<User> findPage(Integer pageNum, Integer pageSize, String name, String permissions, String startDate, String endDate) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        if(!name.equals("")){
            queryWrapper.like("name",name);
        }
        if(!permissions.equals("")){
            queryWrapper.like("permissions",permissions);
        }
        if(StrUtil.isNotBlank(startDate) && StrUtil.isNotBlank(endDate)){
            queryWrapper.between("create_time", TimeUtils.timeProcess(startDate),TimeUtils.timeProcess(endDate));
        }
        queryWrapper.orderByDesc("id"); //根据id排序

        //获取当前用户信息
//        User currentUser = TokenUtils.getCurrentUser();
//        System.out.println("******************"+currentUser.getName());
        return page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    public Boolean changePassword(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account",user.getAccount());
        queryWrapper.ne("del_flag",true);
        User one = getOne(queryWrapper);
        one.setPassword(user.getPassword());
        return updateById(one);
    }

    @Override
    public void export(HttpServletResponse response) throws IOException {
        //查询所有数据
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        List<User> list = list(queryWrapper);
        for (User user : list) {
            if(user.getPermissions().equals("0")){
                user.setPermissions("管理员");
            }else if(user.getPermissions().equals("1")){
                user.setPermissions("团队负责人");
            }
        }
        //在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //定义标题别名
        writer.addHeaderAlias("id","ID");
        writer.addHeaderAlias("name","姓名");
        writer.addHeaderAlias("account","账号");
        writer.addHeaderAlias("password","密码");
        writer.addHeaderAlias("permissions","权限");
        writer.addHeaderAlias("createTime","创建时间");
        writer.addHeaderAlias("modifyTime","修改时间");

        //只输出设置了Alias(别名的字段)
        writer.setOnlyAlias(true);

        //写入excel，默认样式，强制输出标题
        writer.write(list,true);

        //设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out,true);
        out.close();
        writer.close();
    }

    @Override
    public Result imp(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
//        List<User> list = reader.readAll(User.class);
        //忽略表头中文
        List<List<Object>> list = reader.read(1);
        List<User> users= CollUtil.newArrayList();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if((i != j) && (list.get(i).get(1).toString().equals(list.get(j).get(1).toString()))){//导入文件存在重复账号
                    return Result.error("600","导入文件存在重复账号");
                }
            }
            User user = new User();
            user.setName(list.get(i).get(0).toString());
            user.setAccount(list.get(i).get(1).toString());
            user.setPermissions(list.get(i).get(2).toString());
            user.setPassword("123");    //默认密码
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("account",user.getAccount());
            User one = getOne(queryWrapper);
            if(one != null){
                return Result.error("600","数据库中已存在导入账号");
            }
            users.add(user);
        }
        boolean isSave = saveBatch(users);
        if(isSave){
            return Result.success();
        }else {
            return Result.error();
        }
    }
}
