package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.CustomerAddress;
import com.example.permission.mapper.CustomerAddressMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.permission.entity.table.CustomerAddressTableDef.CUSTOMER_ADDRESS;

@Service
public class CustomerAddressService {

    @Autowired
    private CustomerAddressMapper customerAddressMapper;

    public List<CustomerAddress> listByCustomerId(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerAddress.class)
                .where(CUSTOMER_ADDRESS.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_ADDRESS.DELETED.eq(0))
                .orderBy(CUSTOMER_ADDRESS.IS_DEFAULT.desc());
        return customerAddressMapper.selectListByQuery(query);
    }

    @Transactional
    public CustomerAddress add(CustomerAddress address) {
        long count = countByCustomerId(address.getCustomerId());
        if (count >= 5) {
            throw new BusinessException("常用地址最多添加5个");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(address.getCustomerId());
        }
        address.setDeleted(0);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        customerAddressMapper.insert(address);
        return address;
    }

    @Transactional
    public CustomerAddress update(CustomerAddress address) {
        CustomerAddress existing = customerAddressMapper.selectOneById(address.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("地址不存在");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(existing.getCustomerId());
        }
        customerAddressMapper.update(address);
        return address;
    }

    @Transactional
    public void delete(Long id) {
        CustomerAddress existing = customerAddressMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("地址不存在");
        }
        existing.setDeleted(1);
        customerAddressMapper.update(existing);
    }

    @Transactional
    public void setDefault(Long id) {
        CustomerAddress address = customerAddressMapper.selectOneById(id);
        if (address == null || address.getDeleted() == 1) {
            throw new BusinessException("地址不存在");
        }
        clearDefault(address.getCustomerId());
        address.setIsDefault(1);
        customerAddressMapper.update(address);
    }

    private long countByCustomerId(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerAddress.class)
                .where(CUSTOMER_ADDRESS.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_ADDRESS.DELETED.eq(0));
        return customerAddressMapper.selectCountByQuery(query);
    }

    private void clearDefault(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerAddress.class)
                .where(CUSTOMER_ADDRESS.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_ADDRESS.DELETED.eq(0))
                .and(CUSTOMER_ADDRESS.IS_DEFAULT.eq(1));
        List<CustomerAddress> defaults = customerAddressMapper.selectListByQuery(query);
        for (CustomerAddress addr : defaults) {
            addr.setIsDefault(0);
            customerAddressMapper.update(addr);
        }
    }
}
