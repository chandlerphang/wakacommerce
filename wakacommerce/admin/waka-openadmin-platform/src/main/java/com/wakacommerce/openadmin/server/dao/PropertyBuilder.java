
package com.wakacommerce.openadmin.server.dao;

import java.util.Map;

import com.wakacommerce.openadmin.dto.FieldMetadata;

/**
 * 
 */
public interface PropertyBuilder {

    public Map<String, FieldMetadata> execute(Boolean overridePopulateManyToOne);

}
