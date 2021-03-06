package com.wakacommerce.openadmin.server.dao.provider.metadata.request;

import org.hibernate.mapping.Property;
import org.hibernate.type.Type;

import com.wakacommerce.common.presentation.client.SupportedFieldType;
import com.wakacommerce.openadmin.dto.MergedPropertyType;
import com.wakacommerce.openadmin.server.dao.DynamicEntityDao;

import java.util.List;

public class AddMetadataFromMappingDataRequest {

    private final List<Property> componentProperties;
    private final SupportedFieldType type;
    private final SupportedFieldType secondaryType;
    private final Type requestedEntityType;
    private final String propertyName;
    private final MergedPropertyType mergedPropertyType;
    private final DynamicEntityDao dynamicEntityDao;

    public AddMetadataFromMappingDataRequest(List<Property> componentProperties, SupportedFieldType type, SupportedFieldType secondaryType, Type requestedEntityType, String propertyName, MergedPropertyType mergedPropertyType, DynamicEntityDao dynamicEntityDao) {
        this.componentProperties = componentProperties;
        this.type = type;
        this.secondaryType = secondaryType;
        this.requestedEntityType = requestedEntityType;
        this.propertyName = propertyName;
        this.mergedPropertyType = mergedPropertyType;
        this.dynamicEntityDao = dynamicEntityDao;
    }

    public List<Property> getComponentProperties() {
        return componentProperties;
    }

    public SupportedFieldType getType() {
        return type;
    }

    public SupportedFieldType getSecondaryType() {
        return secondaryType;
    }

    public Type getRequestedEntityType() {
        return requestedEntityType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public MergedPropertyType getMergedPropertyType() {
        return mergedPropertyType;
    }

    public DynamicEntityDao getDynamicEntityDao() {
        return dynamicEntityDao;
    }
}
