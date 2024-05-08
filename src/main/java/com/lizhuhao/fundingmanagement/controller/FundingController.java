package com.lizhuhao.fundingmanagement.controller;

import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.FundingDTO;
import com.lizhuhao.fundingmanagement.service.IFundingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-17
 */
@RestController
@RequestMapping("/funding")
public class FundingController {

    @Autowired
    private IFundingService fundingService;


    //查询所有数据
    @GetMapping
    public Result findAll() {
        return Result.success(fundingService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(fundingService.getById(id));
    }


    //保存项目的分配金额信息,且给项目总额、余额
    @PostMapping("/distribute")
    public Result distributeFunding(@RequestBody List<FundingDTO> list,@RequestParam Integer projectId){
        if(fundingService.distributeFunding(list,projectId)){
            return Result.success();
        }else{
            return Result.error();
        }
    }

    //查询项目的分配经费
    @GetMapping("/findFunding")
    public Result findFunding(@RequestParam Integer projectId){
        return Result.success(fundingService.findFunding(projectId));
    }

    //查询项目的分配经费
    @GetMapping("/surplusFunding")
    public Result surplusFunding(@RequestParam Integer projectId){
        return Result.success(fundingService.surplusFunding(projectId));
    }
}

