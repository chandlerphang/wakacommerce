
package com.wakacommerce.openadmin.server.security.service.navigation;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.wakacommerce.openadmin.server.dao.DynamicEntityDao;
import com.wakacommerce.openadmin.server.security.domain.AdminSection;
import com.wakacommerce.openadmin.server.security.domain.AdminUser;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *Jeff Fischer
 */
@Component("blPolymorphicEntityCheckSectionAuthorization")
public class PolymorphicEntitySectionAuthorizationImpl implements SectionAuthorization {

    @Resource(name="blDynamicEntityDao")
    protected DynamicEntityDao dynamicEntityDao;

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @PostConstruct
    public void init() {
        dynamicEntityDao.setStandardEntityManager(em);
    }

    @Override
    public boolean isUserAuthorizedToViewSection(AdminUser adminUser, AdminSection section) {
        if (StringUtils.isBlank(section.getCeilingEntity())) {
            return true;
        }

        try {
            //Only display this section if there are 1 or more entities relative to the ceiling 
            //for this section that are qualified to be created by the admin
            return dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(
                    Class.forName(section.getCeilingEntity()), false).length > 0;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}