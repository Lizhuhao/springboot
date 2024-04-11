package com.lizhuhao.fundingmanagement.service;

import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.entity.FundingType;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
