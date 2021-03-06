package com.wakacommerce.admin.server.service.persistence.module.provider.extension;

import org.springframework.stereotype.Service;

import com.wakacommerce.common.extension.ExtensionManager;


@Service("blCategoryParentCategoryFieldPersistenceProviderExtensionManager")
public class CategoryParentCategoryFieldPersistenceProviderExtensionManager extends ExtensionManager<CategoryParentCategoryFieldPersistenceProviderExtensionHandler> {

    public CategoryParentCategoryFieldPersistenceProviderExtensionManager() {
        super(CategoryParentCategoryFieldPersistenceProviderExtensionHandler.class);
    }

}
