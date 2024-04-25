package com.lizhuhao.fundingmanagement.service;

import com.lizhuhao.fundingmanagement.controller.dto.FundingDTO;
import com.lizhuhao.fundingmanagement.entity.Funding;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-17
 */
public interface IFundingService extends IService<Funding> {

    Long selectCount(Integer projectId);

    List<Funding> selectList(Integer projectId);
}
