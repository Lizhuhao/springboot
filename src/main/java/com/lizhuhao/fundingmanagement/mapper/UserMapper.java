package com.lizhuhao.fundingmanagement.mapper;

import com.lizhuhao.fundingmanagement.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lizhuhao
 * @since 2024-03-28
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from tbl_user where del_flag != true")
    List<User> findAll();
}
