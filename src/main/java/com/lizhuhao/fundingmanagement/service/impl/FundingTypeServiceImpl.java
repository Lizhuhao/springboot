package com.lizhuhao.fundingmanagement.service.impl;

import com.lizhuhao.fundingmanagement.entity.FundingType;
import com.lizhuhao.fundingmanagement.mapper.FundingTypeMapper;
import com.lizhuhao.fundingmanagement.service.IFundingTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-11
 */
@Service
public class FundingTypeServiceImpl extends ServiceImpl<FundingTypeMapper, FundingType> implements IFundingTypeService {

    @Override
    public boolean delete(Integer id) {
        FundingType fundingType = getById(id);
        fundingType.setDelFlag(true);
        return updateById(fundingType);
    }

    @Override
    public boolean delBatch(List<Integer> ids) {
        List<FundingType> list = new ArrayList<>();
        for (Integer id : ids) {
            FundingType fundingType = getById(id);
            fundingType.setDelFlag(true);
            list.add(fundingType);
        }
        return updateBatchById(list);
    }

    @Resource
    private FundingTypeMapper fundingTypeMapper;

    @Override
    public List<FundingType> findAll() {
        return fundingTypeMapper.findAll();
    }
}
