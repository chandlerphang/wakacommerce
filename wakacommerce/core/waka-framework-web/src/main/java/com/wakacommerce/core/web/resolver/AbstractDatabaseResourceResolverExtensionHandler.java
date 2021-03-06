
package com.wakacommerce.core.web.resolver;

import org.thymeleaf.TemplateProcessingParameters;

import com.wakacommerce.common.extension.AbstractExtensionHandler;
import com.wakacommerce.common.extension.ExtensionResultHolder;
import com.wakacommerce.common.extension.ExtensionResultStatusType;


/**
 * 
 */
public abstract class AbstractDatabaseResourceResolverExtensionHandler extends AbstractExtensionHandler 
        implements DatabaseResourceResolverExtensionHandler {
    
    public ExtensionResultStatusType resolveResource(ExtensionResultHolder erh, 
            TemplateProcessingParameters params, String resourceName) {
        return ExtensionResultStatusType.NOT_HANDLED;
    }

}
