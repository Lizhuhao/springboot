package com.lizhuhao.fundingmanagement.controller;

import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.entity.Project;
import com.lizhuhao.fundingmanagement.service.IProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-13
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Project project){
        if(projectService.addAndUpdate(project)){
            return Result.success();
        }else {
            return Result.error(Constants.CODE_400,"保存失败");
        }
    }

    //查询所有未逻辑删除的数据
    @GetMapping
    public Result findAll() {
        return Result.success(projectService.findAll());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(projectService.getById(id));
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "'0'") Integer userId,
                           @RequestParam(defaultValue = "0") Integer pId,
                           @RequestParam(defaultValue = "") String projectName,
                           @RequestParam(defaultValue = "") String responsiblePerson,
                           @RequestParam(defaultValue = "") String startDate,
                           @RequestParam(defaultValue = "") String endDate,
                           @RequestParam(defaultValue = "") String startTime,
                           @RequestParam(defaultValue = "") String endTime) {
        return Result.success(projectService.findPage(pageNum,pageSize,pId,projectName,
                responsiblePerson, startDate,endDate,startTime,endTime,userId));
    }

    //逻辑删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        if(projectService.delete(id)){
            return Result.success();
        }else{
            return Result.error();
        }
    }

    //批量逻辑删除
    @PostMapping("/del/batch")
    public Result delBatch(@RequestBody List<Integer> ids){
        if(projectService.delBatch(ids)){
            return Result.success();
        }else{
            return Result.error();
        }
    }
}

