package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.CoalProductDTO;
import com.nczy.coalsales.entity.CoalProduct;
import com.nczy.coalsales.mapper.CoalProductMapper;
import com.nczy.coalsales.service.CoalProductService;
import org.springframework.stereotype.Service;

@Service
public class CoalProductServiceImpl extends ServiceImpl<CoalProductMapper, CoalProduct> implements CoalProductService {
    private  final CoalProductMapper coalProductMapper;
    public  CoalProductServiceImpl(CoalProductMapper coalProductMapper){
        this.coalProductMapper=coalProductMapper;
    }

    @Override
    public PageResult<CoalProduct> pageCoalProduct(Long current, Long size) {
        Page<CoalProduct> page =page(
                //调的是父类 ServiceImpl 提供的 page(分页对象, 查询条件) 方法
                new Page<>(current,size),
                new LambdaQueryWrapper<CoalProduct>().orderByDesc(CoalProduct::getCreateTime) //mybatisplus提供的查询条件
        );

        return PageResult.of(page);
    }

    @Override
    public void addCoalProduct(CoalProductDTO dto) {
        CoalProduct coalProduct =new CoalProduct();
        coalProduct.setAsh(dto.getAsh());
        coalProduct.setBasePrice(dto.getBasePrice());
        coalProduct.setOrigin(dto.getOrigin());
        coalProduct.setQnetAr(dto.getQnetAr());
        coalProduct.setRemark(dto.getRemark());
        coalProduct.setProductName(dto.getProductName());
        coalProduct.setSafeStock(dto.getSafeStock());
        coalProduct.setSpec(dto.getSpec());
        coalProduct.setStatus(dto.getStatus());
        coalProduct.setSulfur(dto.getSulfur());

        save(coalProduct);
    }

    @Override
    public void deleteCoalProduct(long id) {
           removeById(id);
    }

    @Override
    public void updateCoalProduct(long id, CoalProductDTO dto) {
        CoalProduct update = new CoalProduct();
        update.setId(id);
        update.setProductName(dto.getProductName());
        update.setSpec(dto.getSpec());
        update.setQnetAr(dto.getQnetAr());
        update.setSulfur(dto.getSulfur());
        update.setAsh(dto.getAsh());
        update.setOrigin(dto.getOrigin());
        update.setBasePrice(dto.getBasePrice());
        update.setSafeStock(dto.getSafeStock());
        update.setStatus(dto.getStatus());
        update.setRemark(dto.getRemark());
        updateById(update);
    }

    @Override
    public CoalProduct getCoalProduct(long id) {
        return coalProductMapper.selectById(id);
    }


}
