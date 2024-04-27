package com.lizhuhao.fundingmanagement.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.FundingDTO;
import com.lizhuhao.fundingmanagement.entity.BudgetChange;
import com.lizhuhao.fundingmanagement.entity.Funding;
import com.lizhuhao.fundingmanagement.entity.FundingType;
import com.lizhuhao.fundingmanagement.entity.Project;
import com.lizhuhao.fundingmanagement.service.IBudgetChangeService;
import com.lizhuhao.fundingmanagement.service.IFundingService;
import com.lizhuhao.fundingmanagement.service.IFundingTypeService;
import com.lizhuhao.fundingmanagement.service.IProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Autowired
    private IFundingTypeService fundingTypeService;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private IBudgetChangeService budgetChangeService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Funding funding){
        return Result.success(fundingService.saveOrUpdate(funding));
    }

    //查询所有数据
    @GetMapping
    public Result findAll() {
        return Result.success(fundingService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(fundingService.getById(id));
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {
        QueryWrapper<Funding> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        return Result.success(fundingService.page(new Page<>(pageNum, pageSize),queryWrapper));
    }

    //保存项目的分配金额信息,且给项目总额、余额
    @PostMapping("/distribute")
    public Result distributeFunding(@RequestBody List<FundingDTO> list,@RequestParam Integer projectId){
        List<Funding> fundings = new ArrayList<>();
        BigDecimal totalBudget = new BigDecimal(0);//总额
        BigDecimal balance = new BigDecimal(0);//余额
        QueryWrapper<Project> queryProject= new QueryWrapper<>();
        queryProject.eq("id", projectId);
        queryProject.ne("del_flag", true);
        Project project = projectService.getOne(queryProject);
        if(fundingService.selectCount(projectId) > 0){//修改的情况
            for (FundingDTO fundingDTO : list) {
                QueryWrapper<Funding> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("project_id", projectId);
                queryWrapper.eq("funding_type_id", fundingDTO.getKey());
                queryWrapper.ne("del_flag", true);
                Funding one = fundingService.getOne(queryWrapper);
                one.setAmount(new BigDecimal(fundingDTO.getValue()));
                totalBudget = totalBudget.add(one.getAmount());
                fundings.add(one);
            }
        }else{
            for (FundingDTO fundingDTO : list) {
                Funding funding = new Funding();
                funding.setFundingTypeId(fundingDTO.getKey());
                funding.setAmount(new BigDecimal(fundingDTO.getValue()));
                funding.setProjectId(projectId);
                totalBudget = totalBudget.add(funding.getAmount());
                fundings.add(funding);
            }
        }
        project.setTotalBudget(totalBudget);
        QueryWrapper<BudgetChange> queryBudgetChange = new QueryWrapper<>();
        queryBudgetChange.eq("project_id",projectId);
        queryBudgetChange.ne("del_flag", true);
        balance = balance.add(totalBudget);
        List<BudgetChange> budgetChangeList = budgetChangeService.list(queryBudgetChange);
        for (BudgetChange budgetChange : budgetChangeList) {
            balance = balance.subtract(budgetChange.getCostAmount());
        }
        project.setBalance(balance);
        projectService.saveOrUpdate(project);//给项目总额、余额
        if(fundingService.saveOrUpdateBatch(fundings)){
            return Result.success();
        }else{
            return Result.error();
        }
    }
    //查询项目的分配经费
    @GetMapping("/findFunding")
    public Result distributeFunding(@RequestParam Integer projectId){
        List<FundingDTO> fundingDTOList = new ArrayList<>();
        List<Funding> list = fundingService.selectList(projectId);
        for (Funding funding : list) {
            FundingDTO fundingDTO = new FundingDTO();
            fundingDTO.setId(funding.getId());
            fundingDTO.setKey(funding.getFundingTypeId());
            fundingDTO.setValue(funding.getAmount().toString());
            FundingType fundingType = fundingTypeService.getById(funding.getFundingTypeId());
            fundingDTO.setLabel(fundingType.getTypeName());
            fundingDTOList.add(fundingDTO);
        }
        return Result.success(fundingDTOList);
    }
}

