package com.lizhuhao.fundingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lizhuhao.fundingmanagement.controller.dto.FundingDTO;
import com.lizhuhao.fundingmanagement.entity.Funding;
import com.lizhuhao.fundingmanagement.mapper.FundingMapper;
import com.lizhuhao.fundingmanagement.mapper.FundingTypeMapper;
import com.lizhuhao.fundingmanagement.service.IFundingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
}
