
package com.wakacommerce.openadmin.web.processor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;

import com.wakacommerce.common.web.dialect.AbstractModelVariableModifierProcessor;
import com.wakacommerce.openadmin.server.security.domain.AdminMenu;
import com.wakacommerce.openadmin.server.security.domain.AdminUser;
import com.wakacommerce.openadmin.server.security.service.AdminSecurityService;
import com.wakacommerce.openadmin.server.security.service.navigation.AdminNavigationService;

import javax.annotation.Resource;

/**
 * A Thymeleaf processor that will add the appropriate AdminModules to the model. It does this by
 * iterating through the permissions specified in the SecurityContexts AdminUser object and adding the
 * appropriate section to the model attribute specified by resultVar
 *
 * This is useful in constructing the left navigation menu for the admin console.
 *
 *  
 */
@Component("blAdminModuleProcessor")
public class AdminModuleProcessor extends AbstractModelVariableModifierProcessor {

    private static final String ANONYMOUS_USER_NAME = "anonymousUser";

    @Resource(name = "blAdminNavigationService")
    protected AdminNavigationService adminNavigationService;
    
    @Resource(name = "blAdminSecurityService")
    protected AdminSecurityService securityService;

    /**
     * Sets the name of this processor to be used in Thymeleaf template
     */
    public AdminModuleProcessor() {
        super("admin_module");
    }

    @Override
    public int getPrecedence() {
        return 10001;
    }

    @Override
    protected void modifyModelAttributes(Arguments arguments, Element element) {
        String resultVar = element.getAttributeValue("resultVar");

        AdminUser user = getPersistentAdminUser();
        if (user != null) {
            AdminMenu menu = adminNavigationService.buildMenu(user);
            addToModel(arguments, resultVar, menu);
        }

    }

    protected AdminUser getPersistentAdminUser() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx != null) {
            Authentication auth = ctx.getAuthentication();
            if (auth != null && !auth.getName().equals(ANONYMOUS_USER_NAME)) {
                UserDetails temp = (UserDetails) auth.getPrincipal();

                return securityService.readAdminUserByUserName(temp.getUsername());
            }
        }

        return null;
    }
}
