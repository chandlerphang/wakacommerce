package com.wakacommerce.core.catalog.domain;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.wakacommerce.common.WakaEnumType;

public class RelatedProductTypeEnum implements Serializable, WakaEnumType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, RelatedProductTypeEnum> TYPES = new LinkedHashMap<String, RelatedProductTypeEnum>();

    public static final RelatedProductTypeEnum FEATURED = new RelatedProductTypeEnum("FEATURED", "特色");
    public static final RelatedProductTypeEnum UP_SALE = new RelatedProductTypeEnum("UP_SALE", "增销");
    public static final RelatedProductTypeEnum CROSS_SALE = new RelatedProductTypeEnum("CROSS_SALE", "联销");


    public static RelatedProductTypeEnum getInstance(final String type) {
        return TYPES.get(type);
    }

    private String type;
    private String friendlyType;

    public RelatedProductTypeEnum() {
        //do nothing
    }

    public RelatedProductTypeEnum(final String type, final String friendlyType) {
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
        } else {
            throw new RuntimeException("Cannot add the type: (" + type + "). It already exists as a type via " + getInstance(type).getClass().getName());
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
        RelatedProductTypeEnum other = (RelatedProductTypeEnum) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
