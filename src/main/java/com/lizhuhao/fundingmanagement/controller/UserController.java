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
import com.lizhuhao.fundingmanagement.entity.User;
import com.lizhuhao.fundingmanagement.service.IUserService;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
import com.lizhuhao.fundingmanagement.utils.TokenUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
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
        return Result.success(dto);
    }

    //新增或更新
    @PostMapping
    public Result save(@RequestBody User user){
        if(user.getId() != null){
            Date currentTime = new Date();
            user.setModifyTime(currentTime);
        }
        return Result.success(userService.saveOrUpdate(user));
    }

    //查询所有未逻辑删除的数据
    @GetMapping
    public Result findAll() {
        return Result.success(userService.findAll());
    }

    //查询所有未逻辑删除的数据
    @GetMapping("/findPerson")
    public Result findPerson() {
        return Result.success(userService.findPerson());
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
                               @RequestParam(defaultValue = "") String name,
                               @RequestParam(defaultValue = "") String permissions,
                               @RequestParam(defaultValue = "") String startDate,
                               @RequestParam(defaultValue = "") String endDate) {
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

        return Result.success(userService.page(new Page<>(pageNum, pageSize),queryWrapper));
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
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account",user.getAccount());
        User one = userService.getOne(queryWrapper);
        one.setPassword(user.getPassword());
        if(userService.updateById(one)){
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
        //查询所有数据
        List<User> list = userService.findAll();
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
            User one = userService.getOne(queryWrapper);
            if(one != null){
                return Result.error("600","数据库中已存在导入账号");
            }
            users.add(user);
        }
        boolean isSave = userService.saveBatch(users);
        return Result.success(isSave);
    }
}

