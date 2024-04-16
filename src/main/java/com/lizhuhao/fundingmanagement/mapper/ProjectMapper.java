package com.lizhuhao.fundingmanagement.mapper;

import com.lizhuhao.fundingmanagement.entity.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-13
 */
public interface ProjectMapper extends BaseMapper<Project> {

    @Select("select * from tbl_project where del_flag != true")
    List<Project> findAll();
}
