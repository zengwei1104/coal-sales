package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.CoalProductDTO;
import com.nczy.coalsales.entity.CoalProduct;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.CoalProductService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products")
public class CoalProductController {
    private final CoalProductService coalProductService;

    public CoalProductController(CoalProductService coalProductService) {
        this.coalProductService = coalProductService;
    }

    @GetMapping
    public Result<PageResult<CoalProduct>> page(@RequestParam(defaultValue = "1") Long current,
                                                @RequestParam(defaultValue = "10") Long size,
                                                @AuthenticationPrincipal LoginUser loginUser){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN,RoleConstant.STOCK);
        return Result.success(coalProductService.pageCoalProduct(current,size));
    }

    @PostMapping
    public Result<Void> addCoalProduct(@RequestBody @Valid CoalProductDTO dto,
                                       @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        coalProductService.addCoalProduct(dto);
        return Result.success("添加成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCoalProduct(@PathVariable("id") long id,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        coalProductService.deleteCoalProduct(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> updateCoalProduct(@PathVariable("id") long id,
                                          @AuthenticationPrincipal LoginUser loginUser,
                                          @RequestBody @Valid CoalProductDTO dto) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        coalProductService.updateCoalProduct(id,dto);
        return Result.success("修改成功", null);
    }

    @GetMapping("/{id}")
    public Result<CoalProduct> getCoalProduct(@PathVariable("id") long id,
                                              @AuthenticationPrincipal LoginUser loginUser){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        CoalProduct product = coalProductService.getById(id);
        if (product == null) {
            return Result.error("商品不存在或已被删除");
        }
        return Result.success(coalProductService.getCoalProduct(id));
    }
}