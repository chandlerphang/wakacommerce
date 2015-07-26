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

import com.wakacommerce.profile.core.dao.RoleDao;
import com.wakacommerce.profile.core.domain.CustomerRole;

import java.util.List;

import javax.annotation.Resource;

@Service("blRoleService")
public class RoleServiceImpl implements RoleService {

    @Resource(name="blRoleDao")
    protected RoleDao roleDao;

    @Override
    @Transactional("blTransactionManager")
    public List<CustomerRole> findCustomerRolesByCustomerId(Long customerId) {
        return roleDao.readCustomerRolesByCustomerId(customerId);
    }
}