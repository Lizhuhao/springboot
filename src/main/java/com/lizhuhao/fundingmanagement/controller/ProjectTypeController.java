package com.lizhuhao.fundingmanagement.controller;

import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.entity.ProjectType;
import com.lizhuhao.fundingmanagement.service.IProjectTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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
        if(projectTypeService.saveOrUpdate(projectType)){
            return Result.success();
        }else{
            return Result.error(Constants.CODE_400,"保存失败");
        }
    }

    //查询所有未逻辑删除的数据
    @GetMapping
    public Result findAll() {
        List<ProjectType> list = projectTypeService.findAll();
        if(list.size() != 0){
            return Result.success(list);
        }else{
            return Result.error(Constants.CODE_400,"查询失败");
        }
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(projectTypeService.getById(id));
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String typeName,
                           @RequestParam(defaultValue = "") String startDate,
                           @RequestParam(defaultValue = "") String endDate) {
        return Result.success(projectTypeService.findPage(pageNum,pageSize,typeName,startDate,endDate));
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

