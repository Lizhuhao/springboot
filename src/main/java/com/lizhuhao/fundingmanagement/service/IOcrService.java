package com.lizhuhao.fundingmanagement.service;

import org.springframework.web.multipart.MultipartFile;

public interface IOcrService {
    String actionOcr(MultipartFile multipartFile);
}
