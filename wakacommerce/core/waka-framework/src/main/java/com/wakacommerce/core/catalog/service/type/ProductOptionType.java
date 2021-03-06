package com.wakacommerce.core.catalog.service.type;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.wakacommerce.common.WakaEnumType;

public class ProductOptionType implements Serializable, WakaEnumType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, ProductOptionType> TYPES = new LinkedHashMap<String, ProductOptionType>();

    public static final ProductOptionType COLOR  = new ProductOptionType("COLOR","颜色");
    public static final ProductOptionType SIZE  = new ProductOptionType("SIZE","尺寸");
    public static final ProductOptionType DATE  = new ProductOptionType("DATE","日期");
    public static final ProductOptionType TEXT  = new ProductOptionType("TEXT","文本");
    public static final ProductOptionType TEXTAREA = new ProductOptionType("TEXTAREA", "文本块");
    public static final ProductOptionType BOOLEAN  = new ProductOptionType("BOOLEAN","布尔");
    public static final ProductOptionType DECIMAL  = new ProductOptionType("DECIMAL","数值");
    public static final ProductOptionType INTEGER  = new ProductOptionType("INTEGER","整数");
    public static final ProductOptionType INPUT  = new ProductOptionType("INPUT","输入");
    public static final ProductOptionType PRODUCT  = new ProductOptionType("PRODUCT","商品");
    public static final ProductOptionType SELECT = new ProductOptionType("SELECT", "选择");

    public static ProductOptionType getInstance(final String type) {
        return TYPES.get(type);
    }

    private String type;
    private String friendlyType;

    public ProductOptionType() {
        //do nothing
    }

    public ProductOptionType(final String type, final String friendlyType) {
        this.friendlyType = friendlyType;
        setType(type);
    }

    public String getType() {
        return type;
    }

    public String getFriendlyType() {
        return friendlyType;
    }

    private void setType(final String type) {
        this.type = type;
        if (!TYPES.containsKey(type)) {
            TYPES.put(type, this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!getClass().isAssignableFrom(obj.getClass()))
            return false;
        ProductOptionType other = (ProductOptionType) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
