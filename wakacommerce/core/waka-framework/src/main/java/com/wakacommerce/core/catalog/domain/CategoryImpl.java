package com.wakacommerce.core.catalog.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;

import com.wakacommerce.common.admin.domain.AdminMainEntity;
import com.wakacommerce.common.cache.Hydrated;
import com.wakacommerce.common.cache.HydratedSetup;
import com.wakacommerce.common.cache.engine.CacheFactoryException;
import com.wakacommerce.common.copy.CreateResponse;
import com.wakacommerce.common.copy.MultiTenantCopyContext;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransform;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransformMember;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransformTypes;
import com.wakacommerce.common.i18n.service.DynamicTranslationProvider;
import com.wakacommerce.common.persistence.ArchiveStatus;
import com.wakacommerce.common.persistence.Status;
import com.wakacommerce.common.presentation.AdminPresentation;
import com.wakacommerce.common.presentation.AdminPresentationAdornedTargetCollection;
import com.wakacommerce.common.presentation.AdminPresentationClass;
import com.wakacommerce.common.presentation.AdminPresentationMap;
import com.wakacommerce.common.presentation.AdminPresentationMapKey;
import com.wakacommerce.common.presentation.ValidationConfiguration;
import com.wakacommerce.common.presentation.client.SupportedFieldType;
import com.wakacommerce.common.presentation.client.VisibilityEnum;
import com.wakacommerce.common.template.TemplatePathContainer;
import com.wakacommerce.common.util.DateUtil;
import com.wakacommerce.common.util.UrlUtil;
import com.wakacommerce.common.web.Locatable;
import com.wakacommerce.core.inventory.service.type.InventoryType;
import com.wakacommerce.core.order.service.type.FulfillmentType;
import com.wakacommerce.core.search.domain.CategoryExcludedSearchFacet;
import com.wakacommerce.core.search.domain.CategoryExcludedSearchFacetImpl;
import com.wakacommerce.core.search.domain.CategorySearchFacet;
import com.wakacommerce.core.search.domain.CategorySearchFacetImpl;
import com.wakacommerce.core.search.domain.SearchFacet;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="BLC_CATEGORY")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCategories")
@AdminPresentationClass(friendlyName = "CategoryImpl_baseCategory")
@SQLDelete(sql="UPDATE BLC_CATEGORY SET ARCHIVED = 'Y' WHERE CATEGORY_ID = ?")
@DirectCopyTransform({
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.SANDBOX, skipOverlaps = true),
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.MULTITENANT_CATALOG)
})
public class CategoryImpl implements Category, Status, AdminMainEntity, Locatable, TemplatePathContainer {

    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(CategoryImpl.class);

    private static String buildLink(Category category, boolean ignoreTopLevel) {
        Category myCategory = category;
        List<Long> preventRecursionCategoryIds = new ArrayList<Long>();

        StringBuilder linkBuffer = new StringBuilder(50);
        while (myCategory != null && !preventRecursionCategoryIds.contains(myCategory.getId())) {
            preventRecursionCategoryIds.add(myCategory.getId());
            if (!ignoreTopLevel || myCategory.getParentCategory() != null) {
                if (linkBuffer.length() == 0) {
                    linkBuffer.append(myCategory.getUrlKey());
                } else if(myCategory.getUrlKey() != null && !"/".equals(myCategory.getUrlKey())){
                    linkBuffer.insert(0, myCategory.getUrlKey() + '/');
                }
            }
            myCategory = myCategory.getParentCategory();
        }

        return linkBuffer.toString();
    }

    private static void fillInURLMapForCategory(Map<String, List<Long>> categoryUrlMap, Category category, String startingPath, List<Long> startingCategoryList) throws CacheFactoryException {
        String urlKey = category.getUrlKey();
        if (urlKey == null) {
            throw new CacheFactoryException("Cannot create childCategoryURLMap - the urlKey for a category("+category.getId()+") was null");
        }

        String currentPath = "";
        if (! "/".equals(category.getUrlKey())) {
            currentPath = startingPath + "/" + category.getUrlKey();
        }

        List<Long> newCategoryList = new ArrayList<Long>(startingCategoryList);
        newCategoryList.add(category.getId());

        categoryUrlMap.put(currentPath, newCategoryList);
        for (CategoryXref currentCategory : category.getChildCategoryXrefs()) {
            fillInURLMapForCategory(categoryUrlMap, currentCategory.getSubCategory(), currentPath, newCategoryList);
        }
    }

    @Id
    @GeneratedValue(generator= "CategoryId")
    @GenericGenerator(
        name="CategoryId",
        strategy="com.wakacommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="segment_value", value="CategoryImpl"),
            @Parameter(name="entity_name", value="com.wakacommerce.core.catalog.domain.CategoryImpl")
        }
    )
    @Column(name = "CATEGORY_ID")
    @AdminPresentation(
    	friendlyName = "CategoryImpl_id", 
    	visibility = VisibilityEnum.HIDDEN_ALL)
    protected Long id;

    @Column(name = "NAME", nullable=false)
    @Index(name="CATEGORY_NAME_INDEX", columnNames={"NAME"})
    @AdminPresentation(
    	friendlyName = "CategoryImpl_name", 
    	order = 1000,
        group = Presentation.Group.Name.General,
        groupOrder = Presentation.Group.Order.General,
        prominent = true, 
        gridOrder = 1, 
        columnWidth = "300px")
    protected String name;

    @Column(name = "URL")
    @AdminPresentation(
    	friendlyName = "CategoryImpl_url", 
    	order = 2000,
        group = Presentation.Group.Name.General, 
        groupOrder = Presentation.Group.Order.General,
        prominent = true, 
        gridOrder = 2, 
        columnWidth = "300px",
        validationConfigurations = { @ValidationConfiguration(validationImplementation = "blUriPropertyValidator") })
    @Index(name="CATEGORY_URL_INDEX", columnNames={"URL"})
    protected String url;

    @Column(name = "OVERRIDE_GENERATED_URL")
    @AdminPresentation(
    	friendlyName = "CategoryImpl_overrideGeneratedUrl", 
    	group = Presentation.Group.Name.General,
    	groupOrder = Presentation.Group.Order.General,
        order = 2010)
    protected Boolean overrideGeneratedUrl = false;

    @Column(name = "URL_KEY")
    @Index(name="CATEGORY_URLKEY_INDEX", columnNames={"URL_KEY"})
    @AdminPresentation(
    	friendlyName = "CategoryImpl_urlKey",
        tab = Presentation.Tab.Name.Advanced, 
        tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, 
        groupOrder = Presentation.Group.Order.Advanced,
        excluded = true)
    protected String urlKey;

    @Column(name = "DESCRIPTION")
    @AdminPresentation(
    		friendlyName = "CategoryImpl_description",
            group = Presentation.Group.Name.General, 
            groupOrder = Presentation.Group.Order.General,
            largeEntry = true,
            excluded = true)
    protected String description;

    @Column(name = "ACTIVE_START_DATE")
    @AdminPresentation(
    		friendlyName = "CategoryImpl_activeStartDate", 
    		order = 1000,
            group = Presentation.Group.Name.ActiveDateRange, 
            groupOrder = Presentation.Group.Order.ActiveDateRange,
            defaultValue = "today")
    protected Date activeStartDate;

    @Column(name = "ACTIVE_END_DATE")
    @AdminPresentation(
    		friendlyName = "CategoryImpl_activeEndDate", 
    		order = 2000,
            group = Presentation.Group.Name.ActiveDateRange,
            groupOrder = Presentation.Group.Order.ActiveDateRange)
    protected Date activeEndDate;

    @Column(name = "DISPLAY_TEMPLATE")
    @AdminPresentation(
    		friendlyName = "CategoryImpl_displayTemplate", 
    		order = 1000,
            tab = Presentation.Tab.Name.Advanced, 
            tabOrder = Presentation.Tab.Order.Advanced,
            group = Presentation.Group.Name.Advanced, 
            groupOrder = Presentation.Group.Order.Advanced)
    protected String displayTemplate;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "LONG_DESCRIPTION", length = Integer.MAX_VALUE - 1)
    @AdminPresentation(
    		friendlyName = "CategoryImpl_longDescription", 
    		order = 3000,
            group = Presentation.Group.Name.General, 
            groupOrder = Presentation.Group.Order.General,
            largeEntry = true,
            fieldType = SupportedFieldType.HTML_BASIC)
    protected String longDescription;

    @OneToMany(targetEntity = CategoryXrefImpl.class, mappedBy = "category", orphanRemoval = true,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @OrderBy(value="displayOrder")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCategories")
    @BatchSize(size = 50)
    @AdminPresentationAdornedTargetCollection(
            targetObjectProperty = "subCategory",
            parentObjectProperty = "category",
            friendlyName = "CategoryImpl_allChildCategoryXrefs",
            sortProperty = "displayOrder",
            tab = Presentation.Tab.Name.Advanced, 
            tabOrder = Presentation.Tab.Order.Advanced,
            gridVisibleFields = { "name" })
    protected List<CategoryXref> allChildCategoryXrefs = new ArrayList<CategoryXref>(10);

    @OneToMany(targetEntity = CategoryXrefImpl.class, mappedBy = "subCategory", orphanRemoval = true,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @OrderBy(value="displayOrder")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCategories")
    @BatchSize(size = 50)
    @AdminPresentationAdornedTargetCollection(
            targetObjectProperty = "category",
            parentObjectProperty = "subCategory",
            friendlyName = "CategoryImpl_allParentCategoryXrefs",
            sortProperty = "displayOrder",
            tab = Presentation.Tab.Name.Advanced, 
            tabOrder = Presentation.Tab.Order.Advanced,
            gridVisibleFields = { "name" })
    protected List<CategoryXref> allParentCategoryXrefs = new ArrayList<CategoryXref>(10);

    @OneToMany(targetEntity = CategoryProductXrefImpl.class, mappedBy = "category", orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @OrderBy(value="displayOrder")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCategories")
    @BatchSize(size = 50)
    @AdminPresentationAdornedTargetCollection(
            targetObjectProperty = "product",
            parentObjectProperty = "category",
            friendlyName = "CategoryImpl_allProductXrefs",
            sortProperty = "displayOrder",
            tab = Presentation.Tab.Name.Products, 
            tabOrder = Presentation.Tab.Order.Products,
            gridVisibleFields = { "defaultSku.name" })
    protected List<CategoryProductXref> allProductXrefs = new ArrayList<CategoryProductXref>(10);

    @OneToMany(mappedBy = "category", targetEntity = CategoryMediaXrefImpl.class, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @MapKey(name = "key")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "blCategories")
    @BatchSize(size = 50)
    @AdminPresentationMap(
    	friendlyName = "CategoryImpl_categoryMedia",
        tab = Presentation.Tab.Name.Media, 
        tabOrder = Presentation.Tab.Order.Media,
        keyPropertyFriendlyName = "名称",
        deleteEntityUponRemove = true,
        mediaField = "media.url",
        toOneTargetProperty = "media",
        toOneParentProperty = "category",
        keys = {
            @AdminPresentationMapKey(keyName = "primary", friendlyKeyName = "主图"),
            @AdminPresentationMapKey(keyName = "alt1", friendlyKeyName = "副图1"),
            @AdminPresentationMapKey(keyName = "alt2", friendlyKeyName = "副图2"),
            @AdminPresentationMapKey(keyName = "alt3", friendlyKeyName = "副图3"),
            @AdminPresentationMapKey(keyName = "alt4", friendlyKeyName = "副图4"),
            @AdminPresentationMapKey(keyName = "alt5", friendlyKeyName = "副图5"),
            @AdminPresentationMapKey(keyName = "alt6", friendlyKeyName = "副图6")
        }
    )
    protected Map<String, CategoryMediaXref> categoryMedia = new HashMap<String, CategoryMediaXref>();

    @OneToMany(mappedBy = "category",targetEntity = FeaturedProductImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})   
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCategories")
    @OrderBy(value="sequence")
    @BatchSize(size = 50)
    @AdminPresentationAdornedTargetCollection(
    		friendlyName = "CategoryImpl_featuredProducts", 
    		order = 1000,
            tab = Presentation.Tab.Name.Marketing, 
            tabOrder = Presentation.Tab.Order.Marketing,
            targetObjectProperty = "product",
            sortProperty = "sequence",
            maintainedAdornedTargetFields = { "promotionMessage" },
            gridVisibleFields = { "defaultSku.name", "promotionMessage" })
    protected List<FeaturedProduct> featuredProducts = new ArrayList<FeaturedProduct>(10);
    
    @OneToMany(mappedBy = "category", targetEntity = CrossSaleProductImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCategories")
    @OrderBy(value="sequence")
    @AdminPresentationAdornedTargetCollection(
    		friendlyName = "CategoryImpl_crossSaleProducts", 
    		order = 2000,
            tab = Presentation.Tab.Name.Marketing, 
            tabOrder = Presentation.Tab.Order.Marketing,
            targetObjectProperty = "relatedSaleProduct",
            sortProperty = "sequence",
            maintainedAdornedTargetFields = { "promotionMessage" },
            gridVisibleFields = { "defaultSku.name", "promotionMessage" })
    protected List<RelatedProduct> crossSaleProducts = new ArrayList<RelatedProduct>();

    @OneToMany(mappedBy = "category", targetEntity = UpSaleProductImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCategories")
    @OrderBy(value="sequence")
    @AdminPresentationAdornedTargetCollection(
    		friendlyName = "CategoryImpl_upSaleProducts", 
    		order = 3000,
            tab = Presentation.Tab.Name.Marketing, 
            tabOrder = Presentation.Tab.Order.Marketing,
            targetObjectProperty = "relatedSaleProduct",
            sortProperty = "sequence",
            maintainedAdornedTargetFields = { "promotionMessage" },
            gridVisibleFields = { "defaultSku.name", "promotionMessage" })
    protected List<RelatedProduct> upSaleProducts  = new ArrayList<RelatedProduct>();
    
    @OneToMany(mappedBy = "category", targetEntity = CategorySearchFacetImpl.class, cascade = {CascadeType.ALL})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCategories")
    @OrderBy(value="sequence")
    @AdminPresentationAdornedTargetCollection(
    		friendlyName = "CategoryImpl_searchFacets", 
    		order = 1000,
            tab = Presentation.Tab.Name.SearchFacets, 
            tabOrder = Presentation.Tab.Order.SearchFacets,
            targetObjectProperty = "searchFacet",
            sortProperty = "sequence",
            gridVisibleFields = { "field", "label", "searchDisplayPriority" })
    @BatchSize(size = 50)
    protected List<CategorySearchFacet> searchFacets  = new ArrayList<CategorySearchFacet>();

    @OneToMany(mappedBy = "category", targetEntity = CategoryExcludedSearchFacetImpl.class, cascade = { CascadeType.ALL })
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "blCategories")
    @OrderBy(value = "sequence")
    @AdminPresentationAdornedTargetCollection(
    		friendlyName = "CategoryImpl_excludedSearchFacets", order = 2000,
            tab = Presentation.Tab.Name.SearchFacets, 
            tabOrder = Presentation.Tab.Order.SearchFacets,
            targetObjectProperty = "searchFacet",
            sortProperty = "sequence",
            gridVisibleFields = { "field", "label", "searchDisplayPriority" })
    @BatchSize(size = 50)
    protected List<CategoryExcludedSearchFacet> excludedSearchFacets = new ArrayList<CategoryExcludedSearchFacet>(10);

    @Column(name = "INVENTORY_TYPE")
    @AdminPresentation(
    		friendlyName = "CategoryImpl_inventoryType", 
    		order = 2000,
            helpText = "CategoryImpl_inventoryType_helpText",
            tab = Presentation.Tab.Name.Advanced,
            tabOrder = Presentation.Tab.Order.Advanced,
            group = Presentation.Group.Name.Advanced, 
            groupOrder = Presentation.Group.Order.Advanced,
            fieldType = SupportedFieldType.WAKA_ENUMERATION,
            wakaEnumeration = "com.wakacommerce.core.inventory.service.type.InventoryType")
    protected String inventoryType;
    
    @Column(name = "FULFILLMENT_TYPE")
    @AdminPresentation(friendlyName = "CategoryImpl_fulfillmentType", order = 3000,
            tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
            group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced,
            fieldType = SupportedFieldType.WAKA_ENUMERATION,
            wakaEnumeration = "com.wakacommerce.core.order.service.type.FulfillmentType")
    protected String fulfillmentType;

    @Embedded
    protected ArchiveStatus archiveStatus = new ArchiveStatus();

    @Transient
    @Hydrated(factoryMethod = "createChildCategoryIds")
    protected List<Long> childCategoryIds;

    @Transient
    protected List<CategoryXref> childCategoryXrefs = new ArrayList<CategoryXref>(50);

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return DynamicTranslationProvider.getValue(this, "name", name);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUrl() {
        if(url == null || url.equals("") || url.startsWith("/")) {
            return url;       
        } else if ((url.contains(":") && !url.contains("?")) || url.indexOf('?', url.indexOf(':')) != -1) {
            return url;
        } else {
            return "/" + url;
        }
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Boolean getOverrideGeneratedUrl() {
        return overrideGeneratedUrl == null ? false : overrideGeneratedUrl;
    }

    @Override
    public void setOverrideGeneratedUrl(Boolean overrideGeneratedUrl) {
        this.overrideGeneratedUrl = overrideGeneratedUrl == null ? false : overrideGeneratedUrl;
    }

    @Override
    public String getUrlKey() {
        if ((urlKey == null || "".equals(urlKey.trim())) && name != null) {
            return UrlUtil.generateUrlKey(name);
        }
        return urlKey;
    }

    @Override
    public String getGeneratedUrl() {
        return buildLink(this, false);
    }

    @Override
    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Date getActiveStartDate() {
        if ('Y'==getArchived()) {
            return null;
        }
        return activeStartDate;
    }

    @Override
    public void setActiveStartDate(Date activeStartDate) {
        this.activeStartDate = (activeStartDate == null) ? null : new Date(activeStartDate.getTime());
    }

    @Override
    public Date getActiveEndDate() {
        return activeEndDate;
    }

    @Override
    public void setActiveEndDate(Date activeEndDate) {
        this.activeEndDate = (activeEndDate == null) ? null : new Date(activeEndDate.getTime());
    }

    @Override
    public boolean isActive() {
        if (LOG.isDebugEnabled()) {
            if (!DateUtil.isActive(activeStartDate, activeEndDate, true)) {
                LOG.debug("category, " + id + ", inactive due to date");
            }
            if ('Y'==getArchived()) {
                LOG.debug("category, " + id + ", inactive due to archived status");
            }
        }
        return DateUtil.isActive(activeStartDate, activeEndDate, true) && 'Y'!=getArchived();
    }

    @Override
    public String getDisplayTemplate() {
        return displayTemplate;
    }

    @Override
    public void setDisplayTemplate(String displayTemplate) {
        this.displayTemplate = displayTemplate;
    }

    @Override
    public String getLongDescription() {
        return longDescription;
    }

    @Override
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    @Override
    public Category getParentCategory() {
        Category response = null;
        List<CategoryXref> xrefs = getAllParentCategoryXrefs();
        if (!CollectionUtils.isEmpty(xrefs)) {
            for (CategoryXref xref : xrefs) {
                if (xref.getCategory().isActive() && xref.getDefaultReference() != null && xref.getDefaultReference()) {
                    response = xref.getCategory();
                    break;
                }
            }
        }
        if (response == null) {
            if (!CollectionUtils.isEmpty(xrefs)) {
                for (CategoryXref xref : xrefs) {
                   if (xref.getCategory().isActive()) {
                        response = xref.getCategory();
                        break;
                    }
                }
            }
        }
        return response;
    }

    @Override
    public void setParentCategory(Category category) {
        List<CategoryXref> xrefs = getAllParentCategoryXrefs();
        boolean found = false;
        for (CategoryXref xref : xrefs) {
            if (xref.getCategory().equals(category)) {
                xref.setDefaultReference(true);
                found = true;
            } else if (xref.getDefaultReference() != null && xref.getDefaultReference()) {
                xref.setDefaultReference(null);
            }
        }
        if (!found && category != null) {
            CategoryXref xref = new CategoryXrefImpl();
            xref.setSubCategory(this);
            xref.setCategory(category);
            xref.setDefaultReference(true);
            allParentCategoryXrefs.add(xref);
        }
    }

    @Override
    public List<CategoryXref> getAllChildCategoryXrefs(){
        return allChildCategoryXrefs;
    }

    @Override
    public List<CategoryXref> getChildCategoryXrefs() {
        if (childCategoryXrefs.isEmpty()) {
            for (CategoryXref category : allChildCategoryXrefs) {
                if (category.getSubCategory().isActive()) {
                    childCategoryXrefs.add(category);
                }
            }
        }
        return Collections.unmodifiableList(childCategoryXrefs);
    }

    @Override
    public void setChildCategoryXrefs(List<CategoryXref> childCategories) {
        this.childCategoryXrefs.clear();
        for(CategoryXref category : childCategories){
            this.childCategoryXrefs.add(category);
        }
    }

    @Override
    public void setAllChildCategoryXrefs(List<CategoryXref> childCategories){
        allChildCategoryXrefs.clear();
        for(CategoryXref category : childCategories){
            allChildCategoryXrefs.add(category);
        }
    }

    @Override
    public boolean hasAllChildCategories(){
        return !allChildCategoryXrefs.isEmpty();
    }

    @Override
    public boolean hasChildCategories() {
        return !getChildCategoryXrefs().isEmpty();
    }

    @Override
    public List<Long> getChildCategoryIds() {
        if (childCategoryIds == null) {
            HydratedSetup.populateFromCache(this, "childCategoryIds");
        }
        return childCategoryIds;
    }

    @Override
    public void setChildCategoryIds(List<Long> childCategoryIds) {
        this.childCategoryIds = childCategoryIds;
    }

    public List<Long> createChildCategoryIds() {
        childCategoryIds = new ArrayList<Long>();
        for (CategoryXref category : allChildCategoryXrefs) {
            if (category.getSubCategory().isActive()) {
                childCategoryIds.add(category.getSubCategory().getId());
            }
        }
        return childCategoryIds;
    }

    public Map<String, List<Long>> createChildCategoryURLMap() {
        try {
            Map<String, List<Long>> newMap = new HashMap<String, List<Long>>(50);
            fillInURLMapForCategory(newMap, this, "", new ArrayList<Long>(10));
            return newMap;
        } catch (CacheFactoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Category> buildFullCategoryHierarchy(List<Category> currentHierarchy) {
        if (currentHierarchy == null) { 
            currentHierarchy = new ArrayList<Category>();
            currentHierarchy.add(this);
        }
        
        List<Category> myParentCategories = new ArrayList<Category>();
        if (getParentCategory() != null) {
            myParentCategories.add(getParentCategory());
        }
        if (!CollectionUtils.isEmpty(getAllParentCategoryXrefs())) {
            for (CategoryXref parent : getAllParentCategoryXrefs()) {
                myParentCategories.add(parent.getCategory());
            }
        }

        for (Category category : myParentCategories) {
            if (!currentHierarchy.contains(category)) {
                currentHierarchy.add(category);
                category.buildFullCategoryHierarchy(currentHierarchy);
            }
        }
        
        return currentHierarchy;
    }
    
    @Override
    public List<Category> buildCategoryHierarchy(List<Category> currentHierarchy) {
        if (currentHierarchy == null) {
            currentHierarchy = new ArrayList<Category>();
            currentHierarchy.add(this);
        }
        if (getParentCategory() != null && ! currentHierarchy.contains(getParentCategory())) {
            currentHierarchy.add(getParentCategory());
            getParentCategory().buildCategoryHierarchy(currentHierarchy);
        }
        return currentHierarchy;
    }

    @Override
    public List<CategoryXref> getAllParentCategoryXrefs() {
        return allParentCategoryXrefs;
    }

    @Override
    public void setAllParentCategoryXrefs(List<CategoryXref> allParentCategories) {
        this.allParentCategoryXrefs.clear();
        allParentCategoryXrefs.addAll(allParentCategories);
    }

    @Override
    public List<FeaturedProduct> getFeaturedProducts() {
        return featuredProducts;
    }

    @Override
    public void setFeaturedProducts(List<FeaturedProduct> featuredProducts) {
        this.featuredProducts.clear();
        for(FeaturedProduct featuredProduct : featuredProducts){
            this.featuredProducts.add(featuredProduct);
        }
    }
    
    @Override
    public List<RelatedProduct> getCrossSaleProducts() {
        return crossSaleProducts;
    }

    @Override
    public void setCrossSaleProducts(List<RelatedProduct> crossSaleProducts) {
        this.crossSaleProducts.clear();
        for(RelatedProduct relatedProduct : crossSaleProducts){
            this.crossSaleProducts.add(relatedProduct);
        }       
    }

    @Override
    public List<RelatedProduct> getUpSaleProducts() {
        return upSaleProducts;
    }
    
    @Override
    public List<RelatedProduct> getCumulativeCrossSaleProducts() {
        Set<RelatedProduct> returnProductsSet = new LinkedHashSet<RelatedProduct>();
                
        List<Category> categoryHierarchy = buildCategoryHierarchy(null);
        for (Category category : categoryHierarchy) {
            returnProductsSet.addAll(category.getCrossSaleProducts());
        }
        ArrayList<RelatedProduct> result = new ArrayList<RelatedProduct>(returnProductsSet);
        // all of the individual result sets were sorted, we need to sort the full result set
        Collections.sort(result, sequenceComparator);
        return result;
    }
    
    @Override
    public List<RelatedProduct> getCumulativeUpSaleProducts() {
        Set<RelatedProduct> returnProductsSet = new LinkedHashSet<RelatedProduct>();
        
        List<Category> categoryHierarchy = buildCategoryHierarchy(null);
        for (Category category : categoryHierarchy) {
            returnProductsSet.addAll(category.getUpSaleProducts());
        }
        ArrayList<RelatedProduct> result = new ArrayList<RelatedProduct>(returnProductsSet);
        // all of the individual result sets were sorted, we need to sort the full result set
        Collections.sort(result, sequenceComparator);
        return result;
    }

    @Override
    public List<FeaturedProduct> getCumulativeFeaturedProducts() {
        Set<FeaturedProduct> returnProductsSet = new LinkedHashSet<FeaturedProduct>();
        
        List<Category> categoryHierarchy = buildCategoryHierarchy(null);
        for (Category category : categoryHierarchy) {
            returnProductsSet.addAll(category.getFeaturedProducts());
        }
        ArrayList<FeaturedProduct> result = new ArrayList<FeaturedProduct>(returnProductsSet);
        // all of the individual result sets were sorted, we need to sort the full result set
        Collections.sort(result, sequenceComparator);
        return result;
    }
    
    @Override
    public void setUpSaleProducts(List<RelatedProduct> upSaleProducts) {
        this.upSaleProducts.clear();
        for(RelatedProduct relatedProduct : upSaleProducts){
            this.upSaleProducts.add(relatedProduct);
        }
        this.upSaleProducts = upSaleProducts;
    }

    @Override
    public List<CategoryProductXref> getActiveProductXrefs() {
        List<CategoryProductXref> result = new ArrayList<CategoryProductXref>();
        for (CategoryProductXref product : allProductXrefs) {
            if (product.getProduct().isActive()) {
                result.add(product);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<CategoryProductXref> getAllProductXrefs() {
        return allProductXrefs;
    }

    @Override
    public void setAllProductXrefs(List<CategoryProductXref> allProducts) {
        this.allProductXrefs.clear();
        allProductXrefs.addAll(allProducts);
    }

    @Override
    public List<CategorySearchFacet> getSearchFacets() {
        return searchFacets;
    }

    @Override
    public void setSearchFacets(List<CategorySearchFacet> searchFacets) {
        this.searchFacets = searchFacets;
    }

    @Override
    public List<CategoryExcludedSearchFacet> getExcludedSearchFacets() {
        return excludedSearchFacets;
    }

    @Override
    public void setExcludedSearchFacets(List<CategoryExcludedSearchFacet> excludedSearchFacets) {
        this.excludedSearchFacets = excludedSearchFacets;
    }
    
    @Override
    public InventoryType getInventoryType() {
        return InventoryType.getInstance(this.inventoryType);
    }

    @Override
    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType == null ? null : inventoryType.getType();
    }
    
    @Override
    public FulfillmentType getFulfillmentType() {
        return fulfillmentType == null ? null : FulfillmentType.getInstance(this.fulfillmentType);
    }
    
    @Override
    public void setFulfillmentType(FulfillmentType fulfillmentType) {
        this.fulfillmentType = fulfillmentType == null ? null : fulfillmentType.getType();
    }

    @Override
    public List<CategorySearchFacet> getCumulativeSearchFacets() {
        List<CategorySearchFacet> returnCategoryFacets = new ArrayList<CategorySearchFacet>();
        returnCategoryFacets.addAll(getSearchFacets());
        Collections.sort(returnCategoryFacets, facetPositionComparator);
        
        final Collection<SearchFacet> facets = CollectionUtils.collect(returnCategoryFacets, new Transformer() {
            
            @Override
            public Object transform(Object input) {
                return ((CategorySearchFacet) input).getSearchFacet();
            }
        });

        // Add in parent facets unless they are excluded
        List<CategorySearchFacet> parentFacets = null;
        if (getParentCategory() != null) {
            parentFacets = getParentCategory().getCumulativeSearchFacets();   
            CollectionUtils.filter(parentFacets, new Predicate() {
                @Override
                public boolean evaluate(Object arg) {
                    CategorySearchFacet csf = (CategorySearchFacet) arg;
                    return !getExcludedSearchFacets().contains(csf.getSearchFacet())
                            && !facets.contains(csf.getSearchFacet());
                }
            });
        }
        if (parentFacets != null) {
            returnCategoryFacets.addAll(parentFacets);
        }
        
        
        return returnCategoryFacets;
    }

    @Override
    public Map<String, CategoryMediaXref> getCategoryMediaXref() {
        return categoryMedia;
    }

    @Override
    public void setCategoryMediaXref(Map<String, CategoryMediaXref> categoryMediaXref) {
        this.categoryMedia = categoryMediaXref;
    }
    
    @Override
    public Character getArchived() {
       ArchiveStatus temp;
       if (archiveStatus == null) {
           temp = new ArchiveStatus();
       } else {
           temp = archiveStatus;
       }
       return temp.getArchived();
    }

    @Override
    public void setArchived(Character archived) {
        if (archiveStatus == null) {
            archiveStatus = new ArchiveStatus();
        }
        archiveStatus.setArchived(archived);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (url == null ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        CategoryImpl other = (CategoryImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }
    
    protected static Comparator<CategorySearchFacet> facetPositionComparator = new Comparator<CategorySearchFacet>() {
        @Override
        public int compare(CategorySearchFacet o1, CategorySearchFacet o2) {
            return o1.getSequence().compareTo(o2.getSequence());
        }
    };
    
    protected static Comparator sequenceComparator = new Comparator() {

        @Override
        public int compare(Object o1, Object o2) {
            try {
                return ((Comparable) PropertyUtils.getProperty(o1, "sequence")).compareTo(PropertyUtils.getProperty(o2, "sequence"));
            } catch (Exception e) {
                LOG.warn("Trying to compare objects that do not have a sequence property, assuming they are the same order");
                return 0;
            }
        }
    };

    @Override
    public <G extends Category> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        Category cloned = createResponse.getClone();
        cloned.setActiveEndDate(activeEndDate);
        cloned.setActiveStartDate(activeStartDate);
        cloned.setFulfillmentType(getFulfillmentType());
        cloned.setUrl(url);
        cloned.setUrlKey(urlKey);
        cloned.setOverrideGeneratedUrl(getOverrideGeneratedUrl());
        cloned.setName(name);
        cloned.setLongDescription(longDescription);
        cloned.setInventoryType(getInventoryType());
        cloned.setDisplayTemplate(displayTemplate);
        cloned.setDescription(description);
        for(CategoryXref entry : allParentCategoryXrefs){
            CategoryXref clonedEntry = entry.createOrRetrieveCopyInstance(context).getClone();
            cloned.getAllParentCategoryXrefs().add(clonedEntry);
        }
        if (getParentCategory() != null) {
            cloned.setParentCategory(getParentCategory().createOrRetrieveCopyInstance(context).getClone());
        }
        for(CategoryXref entry : allChildCategoryXrefs){
            CategoryXref clonedEntry = entry.createOrRetrieveCopyInstance(context).getClone();
            cloned.getAllChildCategoryXrefs().add(clonedEntry);
        }
        for(CategorySearchFacet entry : searchFacets){
            CategorySearchFacet clonedEntry = entry.createOrRetrieveCopyInstance(context).getClone();
            cloned.getSearchFacets().add(clonedEntry);
        }
        for(CategoryExcludedSearchFacet entry : excludedSearchFacets){
            CategoryExcludedSearchFacet clonedEntry = entry.createOrRetrieveCopyInstance(context).getClone();
            cloned.getExcludedSearchFacets().add(clonedEntry);
        }
        for(Map.Entry<String, CategoryMediaXref> entry : categoryMedia.entrySet()){
            CategoryMediaXrefImpl clonedEntry = ((CategoryMediaXrefImpl)entry.getValue()).createOrRetrieveCopyInstance(context).getClone();
            cloned.getCategoryMediaXref().put(entry.getKey(),clonedEntry);
        }

        //Don't clone the references to products - those will be handled by another MultiTenantCopier call

        return createResponse;
    }

    public static class Presentation {

        public static class Tab {

            public static class Name {

                public static final String Marketing = "CategoryImpl_tab_marketing";
                public static final String Media = "CategoryImpl_tab_media";
                public static final String Advanced = "CategoryImpl_tab_advanced";
                public static final String Products = "CategoryImpl_tab_products";
                public static final String SearchFacets = "CategoryImpl_tab_searchFacets";
            }

            public static class Order {
                public static final int Media = 2000;
                public static final int Marketing = 3000;
                public static final int Products = 4000;
                public static final int SearchFacets = 5000;
                public static final int Advanced = 6000;
                
            }
            
        }

        public static class Group {

            public static class Name {
                public static final String General = "CategoryImpl_grp_general";
                public static final String ActiveDateRange = "CategoryImpl_grp_date";
                public static final String Advanced = "CategoryImpl_grp_advanced";
            }

            public static class Order {

                public static final int General = 1000;
                public static final int ActiveDateRange = 2000;
                public static final int Advanced = 3000;
                
            }
        }
    }

    @Override
    public String getMainEntityName() {
        return getName();
    }

    @Override
    public String getLocation() {
        return getUrl();
    }

}
