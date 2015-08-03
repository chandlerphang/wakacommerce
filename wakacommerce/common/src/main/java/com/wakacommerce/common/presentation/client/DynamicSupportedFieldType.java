
package com.wakacommerce.common.presentation.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wakacommerce.common.WakaEnumType;

/**
 * This extensible enumeration controls the field types that are available for users to choose from when creating
 * FieldDefinitions in the admin tool. This list should be a strict subset of {@link SupportedFieldType} and will
 * throw an exception if a non-matching type is added.
 * 
 */
public class DynamicSupportedFieldType implements Serializable, WakaEnumType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, DynamicSupportedFieldType> TYPES = new LinkedHashMap<String, DynamicSupportedFieldType>();

    public static final DynamicSupportedFieldType STRING = new DynamicSupportedFieldType("STRING", "String");
    public static final DynamicSupportedFieldType HTML = new DynamicSupportedFieldType("HTML", "Rich Text");
    public static final DynamicSupportedFieldType MONEY = new DynamicSupportedFieldType("MONEY", "Money");
    public static final DynamicSupportedFieldType COLOR = new DynamicSupportedFieldType("COLOR", "Color");
    public static final DynamicSupportedFieldType ASSET_LOOKUP = new DynamicSupportedFieldType("ASSET_LOOKUP", "Image");
    public static final DynamicSupportedFieldType PRODUCT_LOOKUP = new DynamicSupportedFieldType("ADDITIONAL_FOREIGN_KEY|com.wakacommerce.core.catalog.domain.Product", "Product Lookup");
    public static final DynamicSupportedFieldType CATEGORY_LOOKUP = new DynamicSupportedFieldType("ADDITIONAL_FOREIGN_KEY|com.wakacommerce.core.catalog.domain.Category", "Category Lookup");

    public static DynamicSupportedFieldType getInstance(final String type) {
        return TYPES.get(type);
    }

    private String type;
    private String friendlyType;

    public DynamicSupportedFieldType() {
        //do nothing
    }

    public DynamicSupportedFieldType(final String type, final String friendlyType) {
        verifyLegalType(type);
        this.friendlyType = friendlyType;
        setType(type);
    }
    
    /**
     * @param type
     * @throws IllegalArgumentException when the given type does not exist in {@link SupportedFieldType}
     */
    public static void verifyLegalType(String type) {
        if (type.contains("|")) {
            type = type.substring(0, type.indexOf('|'));
        }
        SupportedFieldType.valueOf(type);
    }
    
    /**
     * @return a cloned list of the currently known {@link DynamicSupportedFieldType}s.
     */
    public static List<DynamicSupportedFieldType> getTypes() {
        List<DynamicSupportedFieldType> list = new ArrayList<DynamicSupportedFieldType>(TYPES.size());
        for (Entry<String, DynamicSupportedFieldType> entry : TYPES.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
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
        DynamicSupportedFieldType other = (DynamicSupportedFieldType) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
