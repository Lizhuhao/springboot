package com.lizhuhao.fundingmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lizhuhao.fundingmanagement.entity.FundingType;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-11
 */
public interface IFundingTypeService extends IService<FundingType> {

    boolean delete(Integer id);

    boolean delBatch(List<Integer> ids);

    List<FundingType> findAll();

    Page<FundingType> findPage(Integer pageNum, Integer pageSize, String typeName, String startDate, String endDate);
}
