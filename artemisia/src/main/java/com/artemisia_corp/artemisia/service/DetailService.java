package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.datail.*;

import java.util.List;

public interface DetailService {
    List<SimpleDetailDto> getAllSimpleDetails();
    List<SimpleDetailDto> getDetailsByNotaVenta(Long notaVentaId);
    List<DetailResponseDto> listAll();
    void save(DetailRequestDto detail);
    void delete(DetailDeleteDto detail);
    void update(DetailUpdateDto detail);
}
