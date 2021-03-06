package com.wakacommerce.admin.web.controller.entity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.wakacommerce.common.presentation.client.SupportedFieldType;
import com.wakacommerce.common.util.BLCSystemProperty;
import com.wakacommerce.core.catalog.domain.Category;
import com.wakacommerce.core.catalog.service.CatalogService;
import com.wakacommerce.openadmin.web.controller.entity.AdminBasicEntityController;
import com.wakacommerce.openadmin.web.form.entity.EntityForm;
import com.wakacommerce.openadmin.web.form.entity.EntityFormAction;
import com.wakacommerce.openadmin.web.form.entity.Field;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller("blAdminCategoryController")
@RequestMapping("/" + AdminCategoryController.SECTION_KEY)
public class AdminCategoryController extends AdminBasicEntityController {
    
    protected static final String SECTION_KEY = "category";
    
    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;
    
    @Override
    protected String getSectionKey(Map<String, String> pathVars) {
        //allow external links to work for ToOne items
        if (super.getSectionKey(pathVars) != null) {
            return super.getSectionKey(pathVars);
        }
        return SECTION_KEY;
    }
    
    protected boolean getTreeViewEnabled() {
        return BLCSystemProperty.resolveBooleanSystemProperty("admin.category.treeViewEnabled");
    }

    @Override
    protected void modifyEntityForm(EntityForm ef, Map<String, String> pathVars) {
        Field overrideGeneratedUrl = ef.findField("overrideGeneratedUrl");
        overrideGeneratedUrl.setFieldType(SupportedFieldType.HIDDEN.toString().toLowerCase());
    }

    @Override
    protected void modifyAddEntityForm(EntityForm ef, Map<String, String> pathVars) {
        Field overrideGeneratedUrl = ef.findField("overrideGeneratedUrl");
        overrideGeneratedUrl.setFieldType(SupportedFieldType.HIDDEN.toString().toLowerCase());
        boolean overriddenUrl = Boolean.parseBoolean(overrideGeneratedUrl.getValue());
        Field fullUrl = ef.findField("url");
        fullUrl.withAttribute("overriddenUrl", overriddenUrl)
            .withAttribute("sourceField", "name")
            .withAttribute("toggleField", "overrideGeneratedUrl")
            .withFieldType(SupportedFieldType.GENERATED_URL.toString().toLowerCase());
    }
    
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String viewEntityList(HttpServletRequest request, HttpServletResponse response, Model model,
            @PathVariable Map<String, String> pathVars,
            @RequestParam MultiValueMap<String, String> requestParams) throws Exception {
        String returnPath = super.viewEntityList(request, response, model, pathVars, requestParams);

        if (getTreeViewEnabled()) {
            return entityListWithTreeView(model);
        } else {
            return returnPath;
        }
    }

    @SuppressWarnings("unchecked")
    protected String entityListWithTreeView(Model model) {
        List<Category> parentCategories = catalogService.findAllParentCategories();
        model.addAttribute("parentCategories", parentCategories);
        
        List<EntityFormAction> mainActions = (List<EntityFormAction>) model.asMap().get("mainActions");
        
        mainActions.add(new EntityFormAction("CategoryTreeView")
            .withButtonClass("show-category-tree-view")
            .withDisplayText("Category_Tree_View"));
        
        mainActions.add(new EntityFormAction("CategoryListView")
            .withButtonClass("show-category-list-view active")
            .withDisplayText("Category_List_View"));
        
        model.addAttribute("viewType", "categoryTree");
        return "modules/defaultContainer";
    }

    @Override
    public String[] getSectionCustomCriteria() {
            return new String[]{"categoryDirectEdit"};
        }
}
