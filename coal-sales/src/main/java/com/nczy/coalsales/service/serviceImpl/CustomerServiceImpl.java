package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.CustomerDTO;
import com.nczy.coalsales.entity.Customer;
import com.nczy.coalsales.mapper.CustomerMapper;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {
    private final CustomerMapper customerMapper;
    public CustomerServiceImpl(CustomerMapper customerMapper){
        this.customerMapper =customerMapper;
    }
    @Override
    public void addCustomer(CustomerDTO dto) {

        Customer customer=new Customer();
        customer.setAddress(dto.getAddress());
        customer.setContactName(dto.getContactName());
        customer.setCustomerName(dto.getCustomerName());
        customer.setRegion(dto.getRegion());
        customer.setRemark(dto.getRemark());
        customer.setPhone(dto.getPhone());
        customer.setInvoiceInfo(dto.getInvoiceInfo());
        customer.setCreditLimit(dto.getCreditLimit());

        save(customer);

    }

    @Override
    public PageResult<Customer> pageCustomer(Long current, Long size) {
            Page<Customer> page =page(
                                    //调的是父类 ServiceImpl 提供的 page(分页对象, 查询条件) 方法
                                    new Page<>(current,size),
                                    new LambdaQueryWrapper<Customer>().orderByDesc(Customer::getCreateTime) //mybatisplus提供的查询条件
            );

            return PageResult.of(page);
    }

    @Override
    public void deleteCustomer(long id) {
        removeById(id);
    }

    @Override
    public Customer getCustomer(long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public void updateCustomer(long id, CustomerDTO dto, LoginUser loginUser) {
        Customer customer =getById(id);
        if(customer==null){
            throw new BusinessException("客户不存在或已被删除");
        }


        // ===== 归属校验：销售员只能改自己创建的客户 =====
        // 管理员(1)、销售经理(2) 可以改任意客户
        // 销售员(3) 只能改自己 createBy 的客户
        boolean isAdminOrManager = RoleConstant.ADMIN.equals(loginUser.getRoleId())
                || RoleConstant.MANAGER.equals(loginUser.getRoleId());

        if (!isAdminOrManager) {
            // 非管理员/经理 → 校验归属
            if (customer.getCreateBy() == null
                    || !customer.getCreateBy().equals(loginUser.getUserId())) {
                throw new BusinessException("无权修改该客户：这不是您负责的客户");
            }
        }

        Customer update=new Customer();
        update.setId(id);
        update.setCreditLimit(dto.getCreditLimit());
        update.setInvoiceInfo(dto.getInvoiceInfo());
        update.setPhone(dto.getPhone());
        update.setRemark(dto.getRemark());
        update.setRegion(dto.getRegion());
        update.setCustomerName(dto.getCustomerName());
        update.setContactName(dto.getContactName());

        updateById(update);
    }
}
