package com.lizhuhao.fundingmanagement.mapper;

import com.lizhuhao.fundingmanagement.entity.FundingType;
import com.lizhuhao.fundingmanagement.entity.ProjectType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-12
 */
public interface ProjectTypeMapper extends BaseMapper<ProjectType> {

    @Select("select * from tbl_project_type where del_flag != true")
    List<FundingType> findAll();
}
