package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.FundingDTO;
import com.lizhuhao.fundingmanagement.entity.BudgetChange;
import com.lizhuhao.fundingmanagement.entity.Funding;
import com.lizhuhao.fundingmanagement.entity.FundingType;
import com.lizhuhao.fundingmanagement.entity.Project;
import com.lizhuhao.fundingmanagement.mapper.FundingMapper;
import com.lizhuhao.fundingmanagement.service.IBudgetChangeService;
import com.lizhuhao.fundingmanagement.service.IFundingService;
import com.lizhuhao.fundingmanagement.service.IFundingTypeService;
import com.lizhuhao.fundingmanagement.service.IProjectService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-17
 */
@Service
public class FundingServiceImpl extends ServiceImpl<FundingMapper, Funding> implements IFundingService {

    @Resource
    private FundingMapper fundingMapper;
    @Autowired
    private IProjectService projectService;
    @Autowired
    private IFundingTypeService fundingTypeService;
    @Autowired
    private IBudgetChangeService budgetChangeService;

    @Override
    public Long selectCount(Integer projectId) {
        QueryWrapper<Funding> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId);
        queryWrapper.ne("del_flag", true);
        return fundingMapper.selectCount(queryWrapper);
    }

    @Override
    public List<Funding> selectList(Integer projectId) {
        QueryWrapper<Funding> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId);
        queryWrapper.ne("del_flag", true);
        return fundingMapper.selectList(queryWrapper);
    }

    @Override
    public Result distributeFunding(List<FundingDTO> list, Integer projectId) {
        List<Funding> fundings = new ArrayList<>();
        BigDecimal totalBudget = new BigDecimal(0);//总额
        BigDecimal balance = new BigDecimal(0);//余额
        QueryWrapper<Project> queryProject= new QueryWrapper<>();
        queryProject.eq("id", projectId);
        queryProject.ne("del_flag", true);
        Project project = projectService.getOne(queryProject);
        if(selectCount(projectId) > 0){//修改的情况
            for (FundingDTO fundingDTO : list) {
                QueryWrapper<Funding> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("project_id", projectId);
                queryWrapper.eq("funding_type_id", fundingDTO.getKey());
                queryWrapper.ne("del_flag", true);
                Funding one = getOne(queryWrapper);
                one.setAmount(new BigDecimal(fundingDTO.getValue()));
                QueryWrapper<BudgetChange> queryChange = new QueryWrapper<>();
                queryChange.eq("project_id", projectId);
                queryChange.eq("funding_type_id",fundingDTO.getKey());
                queryChange.ne("del_flag", true);
                List<BudgetChange> budgetChangeList = budgetChangeService.list(queryChange);   //查询该预算类型预算变化
                BigDecimal all = new BigDecimal("0.00");
                if(budgetChangeList.size() > 0){
                    for (BudgetChange budgetChange : budgetChangeList) {
                        all = all.add(budgetChange.getCostAmount());
                    }
                }
                if(one.getAmount().compareTo(all) < 0){//新填入预算比已花费预算小
                    return Result.error(Constants.CODE_400,fundingDTO.getLabel() + "已花费预算大于新分配预算");
                }
                Date currentTime = new Date();
                one.setModifyTime(currentTime);
                totalBudget = totalBudget.add(one.getAmount());
                fundings.add(one);
            }
        }else{
            for (FundingDTO fundingDTO : list) {
                Funding funding = new Funding();
                funding.setFundingTypeId(fundingDTO.getKey());
                if(StrUtil.isNotBlank(fundingDTO.getValue())){  //如果前端传来的值是空的，就填入0.00
                    funding.setAmount(new BigDecimal(fundingDTO.getValue()));
                }else{
                    funding.setAmount(new BigDecimal("0.00"));
                }
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
        boolean projectR = projectService.saveOrUpdate(project);//给项目总额、余额
        boolean fundingR = saveOrUpdateBatch(fundings);
        if(projectR && fundingR){
            return Result.success();
        }else{
            return Result.error(Constants.CODE_500,"服务器错误，插入失败");
        }
    }

    @Override
    public List<FundingDTO> findFunding(Integer projectId) {
        List<FundingDTO> fundingDTOList = new ArrayList<>();
        List<Funding> list = selectList(projectId);
        if(list.size() > 0){
            for (Funding funding : list) {
                FundingDTO fundingDTO = new FundingDTO();
                fundingDTO.setId(funding.getId());
                fundingDTO.setKey(funding.getFundingTypeId());
                fundingDTO.setValue(funding.getAmount().toString());
                FundingType fundingType = fundingTypeService.getById(funding.getFundingTypeId());
                fundingDTO.setLabel(fundingType.getTypeName());
                fundingDTOList.add(fundingDTO);
            }
        }
        return fundingDTOList;
    }

    @Override
    public List<FundingDTO> surplusFunding(Integer projectId) {
        List<FundingDTO> fundingDTOList = new ArrayList<>();
        List<Funding> list = selectList(projectId);
        if(list.size() > 0){
            for (Funding funding : list) {
                FundingDTO fundingDTO = new FundingDTO();
                fundingDTO.setId(funding.getId());
                fundingDTO.setKey(funding.getFundingTypeId());

                QueryWrapper<BudgetChange> queryWrapper = new QueryWrapper<>();//计算剩余预算
                queryWrapper.ne("del_flag",true);
                queryWrapper.eq("project_id",projectId);
                queryWrapper.eq("funding_type_id",funding.getFundingTypeId());
                List<BudgetChange> budgetChangeList = budgetChangeService.list(queryWrapper);
                BigDecimal surplus = funding.getAmount();
                if(budgetChangeList.size() > 0){
                    for (BudgetChange budgetChange : budgetChangeList) {
                        surplus = surplus.subtract(budgetChange.getCostAmount());
                    }
                }
                fundingDTO.setValue(surplus.toString());

                FundingType fundingType = fundingTypeService.getById(funding.getFundingTypeId());
                fundingDTO.setLabel(fundingType.getTypeName());
                fundingDTOList.add(fundingDTO);
            }
        }
        return fundingDTOList;
    }
}
