package com.wakacommerce.core.web.api.wrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.wakacommerce.common.file.service.StaticAssetPathService;
import com.wakacommerce.common.media.domain.Media;
import com.wakacommerce.common.money.Money;
import com.wakacommerce.common.util.xml.ISO8601DateAdapter;
import com.wakacommerce.core.catalog.domain.Product;
import com.wakacommerce.core.catalog.domain.ProductBundle;
import com.wakacommerce.core.catalog.domain.ProductOptionXref;
import com.wakacommerce.core.catalog.domain.RelatedProduct;
import com.wakacommerce.core.catalog.domain.SkuBundleItem;

@XmlRootElement(name = "product")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ProductWrapper extends BaseWrapper implements APIWrapper<Product> {

    @XmlElement
    protected Long id;

    @XmlElement
    protected String name;

    @XmlElement
    protected String description;

    @XmlElement
    protected String longDescription;

    @XmlElement
    protected Money retailPrice;

    @XmlElement
    protected Money salePrice;

    @XmlElement
    protected MediaWrapper primaryMedia;

    @XmlElement
    protected Boolean active;

    @XmlElement(name = "productOption")
    @XmlElementWrapper(name = "productOptions")
    protected List<ProductOptionWrapper> productOptions;

    // For bundles
    @XmlElement
    protected Integer priority;

    @XmlElement
    protected Money bundleItemsRetailPrice;

    @XmlElement
    protected Money bundleItemsSalePrice;

    //End for bundles

    @XmlElement
    @XmlJavaTypeAdapter(ISO8601DateAdapter.class)
    protected Date activeStartDate;

    @XmlElement
    @XmlJavaTypeAdapter(ISO8601DateAdapter.class)
    protected Date activeEndDate;

    @XmlElement
    protected String manufacturer;

    @XmlElement
    protected String model;

    @XmlElement
    protected String promoMessage;
    
    @XmlElement
    protected Long defaultCategoryId;

    @XmlElement(name = "upsaleProduct")
    @XmlElementWrapper(name = "upsaleProducts")
    protected List<RelatedProductWrapper> upsaleProducts;

    @XmlElement(name = "crossSaleProduct")
    @XmlElementWrapper(name = "crossSaleProducts")
    protected List<RelatedProductWrapper> crossSaleProducts;

    @XmlElement(name = "media")
    @XmlElementWrapper(name = "mediaItems")
    protected List<MediaWrapper> media;

    @XmlElement(name = "skuBundleItem")
    @XmlElementWrapper(name = "skuBundleItems")
    protected List<SkuBundleItemWrapper> skuBundleItems;

    @Override
    public void wrapDetails(Product model, HttpServletRequest request) {

        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.longDescription = model.getLongDescription();
        this.activeStartDate = model.getActiveStartDate();
        this.activeEndDate = model.getActiveEndDate();
        this.manufacturer = model.getManufacturer();
        this.model = model.getModel();
        this.promoMessage = model.getPromoMessage();
        this.active = model.isActive();

        if (model instanceof ProductBundle) {

            ProductBundle bundle = (ProductBundle) model;
            this.priority = bundle.getPriority();
            this.bundleItemsRetailPrice = bundle.getBundleItemsRetailPrice();
            this.bundleItemsSalePrice = bundle.getBundleItemsSalePrice();

            if (bundle.getSkuBundleItems() != null) {
                this.skuBundleItems = new ArrayList<SkuBundleItemWrapper>();
                List<SkuBundleItem> bundleItems = bundle.getSkuBundleItems();
                for (SkuBundleItem item : bundleItems) {
                    SkuBundleItemWrapper skuBundleItemsWrapper = (SkuBundleItemWrapper) context.getBean(SkuBundleItemWrapper.class.getName());
                    skuBundleItemsWrapper.wrapSummary(item, request);
                    this.skuBundleItems.add(skuBundleItemsWrapper);
                }
            }
        } else {
            this.retailPrice = model.getDefaultSku().getRetailPrice();
            this.salePrice = model.getDefaultSku().getSalePrice();
        }
        
        if(model.getProductOptionXrefs() != null && !model.getProductOptionXrefs().isEmpty()) {
        	
        	this.productOptions = new ArrayList<ProductOptionWrapper>();
            List<ProductOptionXref> optionXrefs = model.getProductOptionXrefs();
            for (ProductOptionXref optionXref : optionXrefs) {
                ProductOptionWrapper optionWrapper = (ProductOptionWrapper) context.getBean(ProductOptionWrapper.class.getName());
                optionWrapper.wrapSummary(optionXref.getProductOption(), request);
                this.productOptions.add(optionWrapper);
            }
        	
        }
        
        if (model.getMedia() != null && !model.getMedia().isEmpty()) {
            Media media = model.getMedia().get("primary");
            if (media != null) {
                StaticAssetPathService staticAssetPathService = (StaticAssetPathService) this.context.getBean("blStaticAssetPathService");
                primaryMedia = (MediaWrapper) context.getBean(MediaWrapper.class.getName());
                primaryMedia.wrapDetails(media, request);
                if (primaryMedia.isAllowOverrideUrl()) {
                    primaryMedia.setUrl(staticAssetPathService.convertAssetPath(media.getUrl(), request.getContextPath(), request.isSecure()));
                }
            }
        }
        
        if (model.getCategory() != null) {
            this.defaultCategoryId = model.getCategory().getId();
        }

        if (model.getUpSaleProducts() != null && !model.getUpSaleProducts().isEmpty()) {
            upsaleProducts = new ArrayList<RelatedProductWrapper>();
            for (RelatedProduct p : model.getUpSaleProducts()) {
                RelatedProductWrapper upsaleProductWrapper =
                        (RelatedProductWrapper) context.getBean(RelatedProductWrapper.class.getName());
                upsaleProductWrapper.wrapSummary(p, request);
                upsaleProducts.add(upsaleProductWrapper);
            }
        }

        if (model.getCrossSaleProducts() != null && !model.getCrossSaleProducts().isEmpty()) {
            crossSaleProducts = new ArrayList<RelatedProductWrapper>();
            for (RelatedProduct p : model.getCrossSaleProducts()) {
                RelatedProductWrapper crossSaleProductWrapper =
                        (RelatedProductWrapper) context.getBean(RelatedProductWrapper.class.getName());
                crossSaleProductWrapper.wrapSummary(p, request);
                crossSaleProducts.add(crossSaleProductWrapper);
            }
        }

        if (model.getMedia() != null && !model.getMedia().isEmpty()) {
            Map<String, Media> mediaMap = model.getMedia();
            media = new ArrayList<MediaWrapper>();
            StaticAssetPathService staticAssetPathService = (StaticAssetPathService) this.context.getBean("blStaticAssetPathService");
            for (Media med : mediaMap.values()) {
                MediaWrapper wrapper = (MediaWrapper) context.getBean(MediaWrapper.class.getName());
                wrapper.wrapSummary(med, request);
                if (wrapper.isAllowOverrideUrl()) {
                    wrapper.setUrl(staticAssetPathService.convertAssetPath(med.getUrl(), request.getContextPath(), request.isSecure()));
                }
                media.add(wrapper);
            }
        }
    }

    @Override
    public void wrapSummary(Product model, HttpServletRequest request) {
        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.longDescription = model.getLongDescription();
        this.active = model.isActive();

        if (model instanceof ProductBundle) {

            ProductBundle bundle = (ProductBundle) model;
            this.priority = bundle.getPriority();
            this.bundleItemsRetailPrice = bundle.getBundleItemsRetailPrice();
            this.bundleItemsSalePrice = bundle.getBundleItemsSalePrice();
        } else {
            this.retailPrice = model.getDefaultSku().getRetailPrice();
            this.salePrice = model.getDefaultSku().getSalePrice();
        }

        if (model.getProductOptionXrefs() != null && !model.getProductOptionXrefs().isEmpty()) {
            this.productOptions = new ArrayList<ProductOptionWrapper>();
            for (ProductOptionXref optionXref : model.getProductOptionXrefs()) {
                ProductOptionWrapper optionWrapper = (ProductOptionWrapper) context.getBean(ProductOptionWrapper.class.getName());
                optionWrapper.wrapSummary(optionXref.getProductOption(), request);
                this.productOptions.add(optionWrapper);
            }
        }

        if (model.getMedia() != null && !model.getMedia().isEmpty()) {
            Media media = model.getMedia().get("primary");
            if (media != null) {
                StaticAssetPathService staticAssetPathService = (StaticAssetPathService) this.context.getBean("blStaticAssetPathService");
                primaryMedia = (MediaWrapper) context.getBean(MediaWrapper.class.getName());
                primaryMedia.wrapDetails(media, request);
                if (primaryMedia.isAllowOverrideUrl()) {
                    primaryMedia.setUrl(staticAssetPathService.convertAssetPath(media.getUrl(), request.getContextPath(), request.isSecure()));
                }
            }
        }
    }

    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    
    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    
    /**
     * @return the longDescription
     */
    public String getLongDescription() {
        return longDescription;
    }

    
    /**
     * @param longDescription the longDescription to set
     */
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    
    /**
     * @return the retailPrice
     */
    public Money getRetailPrice() {
        return retailPrice;
    }

    
    /**
     * @param retailPrice the retailPrice to set
     */
    public void setRetailPrice(Money retailPrice) {
        this.retailPrice = retailPrice;
    }

    
    /**
     * @return the salePrice
     */
    public Money getSalePrice() {
        return salePrice;
    }

    
    /**
     * @param salePrice the salePrice to set
     */
    public void setSalePrice(Money salePrice) {
        this.salePrice = salePrice;
    }

    
    /**
     * @return the primaryMedia
     */
    public MediaWrapper getPrimaryMedia() {
        return primaryMedia;
    }

    
    /**
     * @param primaryMedia the primaryMedia to set
     */
    public void setPrimaryMedia(MediaWrapper primaryMedia) {
        this.primaryMedia = primaryMedia;
    }

    
    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    
    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    
    /**
     * @return the productOptions
     */
    public List<ProductOptionWrapper> getProductOptions() {
        return productOptions;
    }

    
    /**
     * @param productOptions the productOptions to set
     */
    public void setProductOptions(List<ProductOptionWrapper> productOptions) {
        this.productOptions = productOptions;
    }

    
    /**
     * @return the priority
     */
    public Integer getPriority() {
        return priority;
    }

    
    /**
     * @param priority the priority to set
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    
    /**
     * @return the bundleItemsRetailPrice
     */
    public Money getBundleItemsRetailPrice() {
        return bundleItemsRetailPrice;
    }

    
    /**
     * @param bundleItemsRetailPrice the bundleItemsRetailPrice to set
     */
    public void setBundleItemsRetailPrice(Money bundleItemsRetailPrice) {
        this.bundleItemsRetailPrice = bundleItemsRetailPrice;
    }

    
    /**
     * @return the bundleItemsSalePrice
     */
    public Money getBundleItemsSalePrice() {
        return bundleItemsSalePrice;
    }

    
    /**
     * @param bundleItemsSalePrice the bundleItemsSalePrice to set
     */
    public void setBundleItemsSalePrice(Money bundleItemsSalePrice) {
        this.bundleItemsSalePrice = bundleItemsSalePrice;
    }

    
    /**
     * @return the activeStartDate
     */
    public Date getActiveStartDate() {
        return activeStartDate;
    }

    
    /**
     * @param activeStartDate the activeStartDate to set
     */
    public void setActiveStartDate(Date activeStartDate) {
        this.activeStartDate = activeStartDate;
    }

    
    /**
     * @return the activeEndDate
     */
    public Date getActiveEndDate() {
        return activeEndDate;
    }

    
    /**
     * @param activeEndDate the activeEndDate to set
     */
    public void setActiveEndDate(Date activeEndDate) {
        this.activeEndDate = activeEndDate;
    }

    
    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    
    /**
     * @param manufacturer the manufacturer to set
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    
    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    
    /**
     * @param model the model to set
     */
    public void setModel(String model) {
        this.model = model;
    }

    
    /**
     * @return the promoMessage
     */
    public String getPromoMessage() {
        return promoMessage;
    }

    
    /**
     * @param promoMessage the promoMessage to set
     */
    public void setPromoMessage(String promoMessage) {
        this.promoMessage = promoMessage;
    }

    
    /**
     * @return the defaultCategoryId
     */
    public Long getDefaultCategoryId() {
        return defaultCategoryId;
    }

    
    /**
     * @param defaultCategoryId the defaultCategoryId to set
     */
    public void setDefaultCategoryId(Long defaultCategoryId) {
        this.defaultCategoryId = defaultCategoryId;
    }

    
    /**
     * @return the upsaleProducts
     */
    public List<RelatedProductWrapper> getUpsaleProducts() {
        return upsaleProducts;
    }

    
    /**
     * @param upsaleProducts the upsaleProducts to set
     */
    public void setUpsaleProducts(List<RelatedProductWrapper> upsaleProducts) {
        this.upsaleProducts = upsaleProducts;
    }

    
    /**
     * @return the crossSaleProducts
     */
    public List<RelatedProductWrapper> getCrossSaleProducts() {
        return crossSaleProducts;
    }

    
    /**
     * @param crossSaleProducts the crossSaleProducts to set
     */
    public void setCrossSaleProducts(List<RelatedProductWrapper> crossSaleProducts) {
        this.crossSaleProducts = crossSaleProducts;
    }

    /**
     * @return the media
     */
    public List<MediaWrapper> getMedia() {
        return media;
    }

    
    /**
     * @param media the media to set
     */
    public void setMedia(List<MediaWrapper> media) {
        this.media = media;
    }

    
    /**
     * @return the skuBundleItems
     */
    public List<SkuBundleItemWrapper> getSkuBundleItems() {
        return skuBundleItems;
    }

    
    /**
     * @param skuBundleItems the skuBundleItems to set
     */
    public void setSkuBundleItems(List<SkuBundleItemWrapper> skuBundleItems) {
        this.skuBundleItems = skuBundleItems;
    }

}
