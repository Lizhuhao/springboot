package com.lizhuhao.fundingmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.EvidenceDTO;
import com.lizhuhao.fundingmanagement.entity.BudgetChange;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-16
 */
public interface IBudgetChangeService extends IService<BudgetChange> {

    Result addAndUpdate(BudgetChange budgetChange);

    Page<EvidenceDTO> findPage(Integer pageNum, Integer pageSize, Integer projectId);

    EvidenceDTO upload(MultipartFile file) throws IOException;

    void download(String fileUUID, HttpServletResponse response) throws IOException;

    void deleteEvidences(String fileUUID);

    List<EvidenceDTO> findDetail(Integer projectId);

    void exportDetail(List<EvidenceDTO> list, HttpServletResponse response) throws IOException;
}
