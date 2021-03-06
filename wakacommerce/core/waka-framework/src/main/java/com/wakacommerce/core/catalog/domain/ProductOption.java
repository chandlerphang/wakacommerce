package com.wakacommerce.core.catalog.domain;

import java.io.Serializable;
import java.util.List;

import com.wakacommerce.common.copy.MultiTenantCloneable;
import com.wakacommerce.core.catalog.service.type.ProductOptionType;
import com.wakacommerce.core.catalog.service.type.ProductOptionValidationStrategyType;
import com.wakacommerce.core.catalog.service.type.ProductOptionValidationType;

/**
 * <p>商品选项在商品被添加到购物车前需要被指定值，比如尺寸，颜色等</p>
 * 
 * @author hui
 */
public interface ProductOption extends Serializable, MultiTenantCloneable<ProductOption> {
    
    public Long getId();
    public void setId(Long id);

    public ProductOptionType getType();
    public void setType(ProductOptionType type);

    public String getAttributeName();
    public void setAttributeName(String name);

    public String getLabel();
    public void setLabel(String label);

    public Boolean getRequired();
    public void setRequired(Boolean required);
    
    public Integer getDisplayOrder();
    public void setDisplayOrder(Integer displayOrder);

    public List<ProductOptionXref> getProductXrefs();
    public void setProductXrefs(List<ProductOptionXref> xrefs);

    public List<ProductOptionValue> getAllowedValues();
    public void setAllowedValues(List<ProductOptionValue> allowedValues);

    public Boolean getUseInSkuGeneration();
    public void setUseInSkuGeneration(Boolean useInSkuGeneration);
    
    public ProductOptionValidationType getProductOptionValidationType();
    public void setProductOptionValidationType(ProductOptionValidationType productOptionValidationType);

    void setErrorMessage(String errorMessage);
    void setErrorCode(String errorCode);
    String getErrorMessage();
    String getErrorCode();

    String getValidationString();
    void setValidationString(String validationString);

    void setProductOptionValidationStrategyType(ProductOptionValidationStrategyType productOptionValidationType);
    ProductOptionValidationStrategyType getProductOptionValidationStrategyType();

}
