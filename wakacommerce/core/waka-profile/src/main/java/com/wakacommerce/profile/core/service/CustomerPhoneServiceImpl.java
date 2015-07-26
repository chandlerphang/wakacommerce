/*
 * #%L
 * BroadleafCommerce Profile
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.wakacommerce.profile.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wakacommerce.common.util.TransactionUtils;
import com.wakacommerce.profile.core.dao.CustomerPhoneDao;
import com.wakacommerce.profile.core.domain.CustomerPhone;

import java.util.List;

import javax.annotation.Resource;

@Service("blCustomerPhoneService")
public class CustomerPhoneServiceImpl implements CustomerPhoneService {

    @Resource(name="blCustomerPhoneDao")
    protected CustomerPhoneDao customerPhoneDao;

    @Override
    @Transactional(TransactionUtils.DEFAULT_TRANSACTION_MANAGER)
    public CustomerPhone saveCustomerPhone(CustomerPhone customerPhone) {
        List<CustomerPhone> activeCustomerPhones = readActiveCustomerPhonesByCustomerId(customerPhone.getCustomer().getId());
        if (activeCustomerPhones != null && activeCustomerPhones.isEmpty()) {
            customerPhone.getPhone().setDefault(true);
        } else {
            // if parameter customerPhone is set as default, unset all other default phones
            if (customerPhone.getPhone().isDefault()) {
                for (CustomerPhone activeCustomerPhone : activeCustomerPhones) {
                    if (!activeCustomerPhone.getId().equals(customerPhone.getId()) && activeCustomerPhone.getPhone().isDefault()) {
                        activeCustomerPhone.getPhone().setDefault(false);
                        customerPhoneDao.save(activeCustomerPhone);
                    }
                }
            }
        }
        return customerPhoneDao.save(customerPhone);
    }

    @Override
    public List<CustomerPhone> readActiveCustomerPhonesByCustomerId(Long customerId) {
        return customerPhoneDao.readActiveCustomerPhonesByCustomerId(customerId);
    }

    @Override
    public CustomerPhone readCustomerPhoneById(Long customerPhoneId) {
        return customerPhoneDao.readCustomerPhoneById(customerPhoneId);
    }

    @Override
    @Transactional(TransactionUtils.DEFAULT_TRANSACTION_MANAGER)
    public void makeCustomerPhoneDefault(Long customerPhoneId, Long customerId) {
        customerPhoneDao.makeCustomerPhoneDefault(customerPhoneId, customerId);
    }

    @Override
    @Transactional(TransactionUtils.DEFAULT_TRANSACTION_MANAGER)
    public void deleteCustomerPhoneById(Long customerPhoneId){
        customerPhoneDao.deleteCustomerPhoneById(customerPhoneId);
    }

    @Override
    public CustomerPhone findDefaultCustomerPhone(Long customerId) {
        return customerPhoneDao.findDefaultCustomerPhone(customerId);
    }

    @Override
    public List<CustomerPhone> readAllCustomerPhonesByCustomerId(Long customerId) {
        return customerPhoneDao.readAllCustomerPhonesByCustomerId(customerId);
    }

    @Override
    public CustomerPhone create() {
        return customerPhoneDao.create();
    }

}