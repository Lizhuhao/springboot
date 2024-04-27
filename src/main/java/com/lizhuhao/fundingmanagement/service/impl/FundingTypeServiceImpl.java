package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizhuhao.fundingmanagement.entity.FundingType;
import com.lizhuhao.fundingmanagement.mapper.FundingTypeMapper;
import com.lizhuhao.fundingmanagement.service.IFundingTypeService;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
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

    @Override
    public Page<FundingType> findPage(Integer pageNum, Integer pageSize, String typeName, String startDate, String endDate) {
        QueryWrapper<FundingType> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        if(StrUtil.isNotBlank(typeName)){
            queryWrapper.like("type_name",typeName);
        }
        if(StrUtil.isNotBlank(startDate) && StrUtil.isNotBlank(endDate)){
            queryWrapper.between("create_time", TimeUtils.timeProcess(startDate),TimeUtils.timeProcess(endDate));
        }
        queryWrapper.orderByDesc("id");
        return page(new Page<>(pageNum, pageSize),queryWrapper);
    }
}
