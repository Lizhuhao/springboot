package com.lizhuhao.fundingmanagement.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.UserDTO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import com.lizhuhao.fundingmanagement.service.IUserService;
import com.lizhuhao.fundingmanagement.entity.User;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lizhuhao
 * @since 2024-03-28
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    //登录
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO){
        String account = userDTO.getAccount();
        String password = userDTO.getPassword();
        if(StrUtil.isBlank(account) || StrUtil.isBlank(password)){
            return Result.error(Constants.CODE_400,"参数错误");
        }
        UserDTO dto = userService.login(userDTO);
        return Result.success(dto);
    }

    //新增或更新
    @PostMapping
    public Result save(@RequestBody User user){
        return Result.success(userService.saveOrUpdate(user));
    }

    //查询所有数据
    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    //根据账号查询所有信息
    @GetMapping("/account/{account}")
    public Result findOneByAccount(@PathVariable String account) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account",account);
        return Result.success(userService.getOne(queryWrapper));
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam(defaultValue = "") String name) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(!name.equals("")){
            queryWrapper.like("name",name);
        }
        queryWrapper.orderByDesc("id");
        return Result.success(userService.page(new Page<>(pageNum, pageSize),queryWrapper));
    }

    /**
     * 导出数据
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        //查询所有数据
        List<User> list = userService.list();
        //在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //定义标题别名
        writer.addHeaderAlias("name","姓名");
        writer.addHeaderAlias("account","账号");
        writer.addHeaderAlias("password","密码");
        writer.addHeaderAlias("permissions","权限");
        writer.addHeaderAlias("createTime","创建时间");
        writer.addHeaderAlias("modifyTime","修改时间");

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

    /**
     * 导入
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
//        List<User> list = reader.readAll(User.class);
        //忽略表头中文
        List<List<Object>> list = reader.read(1);
        List<User> users= CollUtil.newArrayList();
        for (List<Object> row : list) {
            User user = new User();
            user.setName(row.get(0).toString());
            user.setAccount(row.get(1).toString());
            user.setPassword(row.get(2).toString());
            user.setPermissions(row.get(3).toString());
            users.add(user);
        }
        System.out.print(users);
        boolean isSave = userService.saveBatch(users);
        return Result.success(isSave);
    }
}

