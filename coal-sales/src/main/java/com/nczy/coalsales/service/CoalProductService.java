package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.CoalProductDTO;
import com.nczy.coalsales.entity.CoalProduct;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface CoalProductService extends IService<CoalProduct> {
    void addCoalProduct(@Valid CoalProductDTO dto);

    void deleteCoalProduct(long id);

    void updateCoalProduct(long id,@Valid CoalProductDTO dto);

    PageResult<CoalProduct> pageCoalProduct(Long current, Long size);

    CoalProduct getCoalProduct(long id);
}
