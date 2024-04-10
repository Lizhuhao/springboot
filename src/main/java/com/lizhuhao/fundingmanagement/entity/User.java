package com.lizhuhao.fundingmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author lizhuhao
 * @since 2024-03-28
 */
@Getter
@Setter
@TableName("tbl_user")
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * ID
     */
      @TableId(type= IdType.AUTO)
        private Integer id;

      /**
     * 姓名
     */
      private String name;

      /**
     * 账号（手机号）
     */
      private String account;

      /**
     * 密码
     */
      private String password;

      /**
     * 权限
     */
      private String permissions;

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
