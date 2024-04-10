package com.lizhuhao.fundingmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-09
 */
@Getter
@Setter
  @TableName("tbl_file")
public class UploadFile implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * ID
     */
      @TableId(type= IdType.AUTO)
        private Integer id;

      /**
     * 文件名称
     */
      private String fileName;

      /**
     * 文件类型
     */
      private String fileType;

      /**
     * 文件大小
     */
      private Long fileSize;

      /**
     * 下载链接
     */
      private String fileUrl;

      /**
     * 删除标志
     */
      private Boolean delFlag;

      /**
     * 创建时间
     */
      private Date createTime;
    /**
     * 文件md5
     */
      private String md5;
}
