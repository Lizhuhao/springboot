package com.lizhuhao.fundingmanagement.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.UserDTO;
import com.lizhuhao.fundingmanagement.entity.User;
import com.lizhuhao.fundingmanagement.service.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
        if(dto != null){
            return Result.success(dto);
        }else{
            return Result.error(Constants.CODE_400,"用户或密码错误");
        }
    }

    //新增或更新
    @PostMapping
    public Result save(@RequestBody User user){
        if(user.getId() != null){
            Date currentTime = new Date();
            user.setModifyTime(currentTime);
        }
        if(userService.saveOrUpdate(user)){
            return Result.success();
        }else{
            return Result.error(Constants.CODE_400,"保存失败");
        }
    }
    //查询所有未逻辑删除的数据
    @GetMapping
    public Result findAll() {
        List<User> userList = userService.findAll();
        if(userList.size() != 0){
            return Result.success();
        }else{
            return Result.error(Constants.CODE_400,"查询失败");
        }
    }

    //查询所有科研负责人
    @GetMapping("/findPerson")
    public Result findPerson() {
        List<User> userList = userService.findPerson();
        if(userList.size() != 0){
            return Result.success();
        }else{
            return Result.error(Constants.CODE_400,"查询失败");
        }
    }

    //重置账号
    @PostMapping("/reset")
    public Result resetAccount(@RequestParam Integer id){
        User user = userService.getById(id);
        user.setPassword("123");
        if(userService.saveOrUpdate(user)){
            return Result.success();
        }else{
            return Result.error(Constants.CODE_400,"重置失败");
        }
    }
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    //根据账号查询所有信息
    @GetMapping("/account/{account}")
    public Result findOneByAccount(@PathVariable String account) {
        User oneByAccount = userService.findOneByAccount(account);
        if(oneByAccount != null){
            return Result.success();
        }else{
            return Result.error(Constants.CODE_400,"查询失败");
        }
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam(defaultValue = "") String name,
                               @RequestParam(defaultValue = "") String permissions,
                               @RequestParam(defaultValue = "") String startDate,
                               @RequestParam(defaultValue = "") String endDate) {
        Page<User> page = userService.findPage(pageNum, pageSize, name, permissions, startDate, endDate);
        return Result.success(page);
    }

    //逻辑删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        if(userService.delete(id)){
            return Result.success();
        }else{
            return Result.error();
        }
    }

    //修改密码
    @PutMapping("/changePassword")
    public Result changePassword(@RequestBody User user) {
        if(userService.changePassword(user)){
            return Result.success();
        }else{
            return Result.error();
        }
    }

    /**
     * 导出用户数据
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        userService.export(response);
    }

    /**
     * 导入
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws IOException {
        return userService.imp(file);
    }
}

