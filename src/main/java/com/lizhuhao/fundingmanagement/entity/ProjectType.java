package com.lizhuhao.fundingmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-12
 */
@Getter
@Setter
  @TableName("tbl_project_type")
public class ProjectType implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * ID
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      /**
     * 项目类型名称
     */
      private String typeName;

      /**
     * 项目类型号
     */
      private Integer typeNumber;

      /**
     * 删除标志
     */
      private Boolean delFlag;

      /**
     * 创建日期
     */
      private Date createTime;

      /**
     * 修改日期
     */
      private Date modifyTime;


}
