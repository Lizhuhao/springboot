package com.lizhuhao.fundingmanagement.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;

import com.lizhuhao.fundingmanagement.service.IFundingTypeService;
import com.lizhuhao.fundingmanagement.entity.FundingType;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-11
 */
@RestController
@RequestMapping("/fundingType")
public class FundingTypeController {

    @Autowired
    private IFundingTypeService fundingTypeService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody FundingType fundingType){
        if(fundingType.getId() != null){ //修改时填入修改时间
            Date currentTime = new Date();
            fundingType.setModifyTime(currentTime);
        }
        return Result.success(fundingTypeService.saveOrUpdate(fundingType));
    }

    //查询所有数据
    @GetMapping
    public Result findAll() {
        return Result.success(fundingTypeService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(fundingTypeService.getById(id));
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String typeName) {
        QueryWrapper<FundingType> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        if(!typeName.equals("")){
            queryWrapper.like("type_name",typeName);
        }
        queryWrapper.orderByDesc("id");
        return Result.success(fundingTypeService.page(new Page<>(pageNum, pageSize),queryWrapper));
    }

    //逻辑删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        if(fundingTypeService.delete(id)){
            return Result.success();
        }else{
            return Result.error();
        }
    }

    //批量逻辑删除
    @PostMapping("/del/batch")
    public Result delBatch(@RequestBody List<Integer> ids){
        if(fundingTypeService.delBatch(ids)){
            return Result.success();
        }else{
            return Result.error();
        }
    }
}

