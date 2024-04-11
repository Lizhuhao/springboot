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
 * @since 2024-04-11
 */
@Getter
@Setter
  @TableName("tbl_funding_type")
public class FundingType implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * id
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      /**
     * 经费类型名
     */
      private String typeName;

      /**
     * 删除标志
     */
      private Boolean delFlag;

      /**
     * 创建时间
     */
      private Date createTime;

      /**
     * 修改时间
     */
      private Date modifyTime;


}
