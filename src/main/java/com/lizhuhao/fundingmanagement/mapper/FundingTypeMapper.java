package com.lizhuhao.fundingmanagement.mapper;

import com.lizhuhao.fundingmanagement.entity.FundingType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-11
 */
public interface FundingTypeMapper extends BaseMapper<FundingType> {
    @Select("select * from tbl_funding_type where del_flag != true")
    List<FundingType> findAll();
}
