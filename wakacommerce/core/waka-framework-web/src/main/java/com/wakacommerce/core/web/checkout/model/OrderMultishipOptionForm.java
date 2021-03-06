
package com.wakacommerce.core.web.checkout.model;

import java.io.Serializable;
import java.util.List;

import com.wakacommerce.core.order.service.call.OrderMultishipOptionDTO;

/**
 * This form is used to bind multiship options in a way that doesn't require
 * the actual objects to be instantiated -- we handle that at the controller
 * level.
 * 
 * @see OrderMultishipOptionDTO
 * 
 * 
 */
public class OrderMultishipOptionForm implements Serializable {

    private static final long serialVersionUID = -5989681894142759293L;
    
    protected List<OrderMultishipOptionDTO> options;

    public List<OrderMultishipOptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<OrderMultishipOptionDTO> options) {
        this.options = options;
    }
    
}
