
package com.wakacommerce.openadmin.server.dao.provider.metadata.request;

import com.wakacommerce.openadmin.server.dao.DynamicEntityDao;

/**
 * Contains the requested field, metadata and support classes.
 *
 * 
 */
public class LateStageAddMetadataRequest {

    private final String fieldName;
    private final Class<?> parentClass;
    private final Class<?> targetClass;
    private final DynamicEntityDao dynamicEntityDao;
    private final String prefix;

    public LateStageAddMetadataRequest(String fieldName, Class<?> parentClass, Class<?> targetClass, DynamicEntityDao dynamicEntityDao, String prefix) {
        this.fieldName = fieldName;
        this.parentClass = parentClass;
        this.targetClass = targetClass;
        this.dynamicEntityDao = dynamicEntityDao;
        this.prefix = prefix;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<?> getParentClass() {
        return parentClass;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public DynamicEntityDao getDynamicEntityDao() {
        return dynamicEntityDao;
    }

    public String getPrefix() {
        return prefix;
    }
}
