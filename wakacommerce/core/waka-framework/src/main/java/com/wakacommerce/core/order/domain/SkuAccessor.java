
package com.wakacommerce.core.order.domain;

import com.wakacommerce.core.catalog.domain.Sku;

/**
 * Interface indicating that an item contains a getSku method.
 *
 * Intended for use by subclasses of OrderItem that contain a sku.
 *
 *   
 */
public interface SkuAccessor {
    Sku getSku();
}
