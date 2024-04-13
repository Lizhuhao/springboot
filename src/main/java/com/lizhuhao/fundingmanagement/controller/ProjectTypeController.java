package com.lizhuhao.fundingmanagement.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;

import com.lizhuhao.fundingmanagement.service.IProjectTypeService;
import com.lizhuhao.fundingmanagement.entity.ProjectType;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-12
 */
@RestController
@RequestMapping("/projectType")
public class ProjectTypeController {

    @Autowired
    private IProjectTypeService projectTypeService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody ProjectType projectType){
        if(projectType.getId() != null){ //修改时填入修改时间
            Date currentTime = new Date();
            projectType.setModifyTime(currentTime);
        }
        return Result.success(projectTypeService.saveOrUpdate(projectType));
    }

    //查询所有未逻辑删除的数据
    @GetMapping
    public Result findAll() {
        return Result.success(projectTypeService.findAll());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(projectTypeService.getById(id));
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String typeName) {
        QueryWrapper<ProjectType> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        if(!typeName.equals("")){
            queryWrapper.like("type_name",typeName);
        }
        queryWrapper.orderByDesc("id");
        return Result.success(projectTypeService.page(new Page<>(pageNum, pageSize),queryWrapper));
    }

    //逻辑删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        if(projectTypeService.delete(id)){
            return Result.success();
        }else{
            return Result.error();
        }
    }

    //批量逻辑删除
    @PostMapping("/del/batch")
    public Result delBatch(@RequestBody List<Integer> ids){
        if(projectTypeService.delBatch(ids)){
            return Result.success();
        }else{
            return Result.error();
        }
    }
}

