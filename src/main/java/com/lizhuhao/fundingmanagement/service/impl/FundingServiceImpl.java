package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    public boolean distributeFunding(List<FundingDTO> list, Integer projectId) {
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
                totalBudget = totalBudget.add(one.getAmount());
                fundings.add(one);
            }
        }else{
            for (FundingDTO fundingDTO : list) {
                Funding funding = new Funding();
                funding.setFundingTypeId(fundingDTO.getKey());
                if(StrUtil.isNotBlank(fundingDTO.getValue())){
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
        return (projectR && fundingR);
    }

    @Override
    public List<FundingDTO> findFunding(Integer projectId) {
        List<FundingDTO> fundingDTOList = new ArrayList<>();
        List<Funding> list = selectList(projectId);
        for (Funding funding : list) {
            FundingDTO fundingDTO = new FundingDTO();
            fundingDTO.setId(funding.getId());
            fundingDTO.setKey(funding.getFundingTypeId());
            fundingDTO.setValue(funding.getAmount().toString());
            FundingType fundingType = fundingTypeService.getById(funding.getFundingTypeId());
            fundingDTO.setLabel(fundingType.getTypeName());
            fundingDTOList.add(fundingDTO);
        }
        return fundingDTOList;
    }
}
