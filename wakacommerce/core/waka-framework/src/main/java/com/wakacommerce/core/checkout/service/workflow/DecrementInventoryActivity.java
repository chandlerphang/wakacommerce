  
package com.wakacommerce.core.checkout.service.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.wakacommerce.core.catalog.domain.Sku;
import com.wakacommerce.core.inventory.service.ContextualInventoryService;
import com.wakacommerce.core.inventory.service.type.InventoryType;
import com.wakacommerce.core.order.domain.BundleOrderItem;
import com.wakacommerce.core.order.domain.DiscreteOrderItem;
import com.wakacommerce.core.order.domain.OrderItem;
import com.wakacommerce.core.workflow.BaseActivity;
import com.wakacommerce.core.workflow.ProcessContext;
import com.wakacommerce.core.workflow.state.ActivityStateManagerImpl;

/**
 * Decrements inventory
 * 
 *     
 */
public class DecrementInventoryActivity extends BaseActivity<ProcessContext<CheckoutSeed>> {

    @Resource(name = "blInventoryService")
    protected ContextualInventoryService inventoryService;
    
    public DecrementInventoryActivity() {
        super();
        super.setAutomaticallyRegisterRollbackHandler(false);
    }

    @Override
    public ProcessContext<CheckoutSeed> execute(ProcessContext<CheckoutSeed> context) throws Exception {
        CheckoutSeed seed = context.getSeedData();
        List<OrderItem> orderItems = seed.getOrder().getOrderItems();

        //map to hold skus and quantity purchased
        HashMap<Sku, Integer> skuInventoryMap = new HashMap<Sku, Integer>();

        for (OrderItem orderItem : orderItems) {
            if (orderItem instanceof DiscreteOrderItem) {
                Sku sku = ((DiscreteOrderItem) orderItem).getSku();
                Integer quantity = skuInventoryMap.get(sku);
                if (quantity == null) {
                    quantity = orderItem.getQuantity();
                } else {
                    quantity += orderItem.getQuantity();
                }
                if (InventoryType.CHECK_QUANTITY.equals(sku.getInventoryType())) {
                    skuInventoryMap.put(sku, quantity);
                }
            } else if (orderItem instanceof BundleOrderItem) {
                BundleOrderItem bundleItem = (BundleOrderItem) orderItem;
                if (InventoryType.CHECK_QUANTITY.equals(bundleItem.getSku().getInventoryType())) {
                    // add the bundle sku of quantities to decrement
                    skuInventoryMap.put(bundleItem.getSku(), bundleItem.getQuantity());
                }
                
                // Now add all of the discrete items within the bundl
                List<DiscreteOrderItem> discreteItems = bundleItem.getDiscreteOrderItems();
                for (DiscreteOrderItem discreteItem : discreteItems) {
                    if (InventoryType.CHECK_QUANTITY.equals(discreteItem.getSku().getInventoryType())) {
                        Integer quantity = skuInventoryMap.get(discreteItem.getSku());
                        if (quantity == null) {
                            quantity = (discreteItem.getQuantity() * bundleItem.getQuantity());
                        } else {
                            quantity += (discreteItem.getQuantity() * bundleItem.getQuantity());
                        }
                        skuInventoryMap.put(discreteItem.getSku(), quantity);
                    }
                }
            }
        }

        Map<String, Object> rollbackState = new HashMap<String, Object>();
        if (getRollbackHandler() != null && !getAutomaticallyRegisterRollbackHandler()) {
            if (getStateConfiguration() != null && !getStateConfiguration().isEmpty()) {
                rollbackState.putAll(getStateConfiguration());
            }
            // Register the map with the rollback state object early on; this allows the extension handlers to incrementally
            // add state while decrementing but still throw an exception
            ActivityStateManagerImpl.getStateManager().registerState(this, context, getRollbackRegion(), getRollbackHandler(), rollbackState);
        }
            
        if (!skuInventoryMap.isEmpty()) {
            Map<String, Object> contextualInfo = new HashMap<String, Object>();
            contextualInfo.put(ContextualInventoryService.ORDER_KEY, context.getSeedData().getOrder());
            contextualInfo.put(ContextualInventoryService.ROLLBACK_STATE_KEY, new HashMap<String, Object>());
            inventoryService.decrementInventory(skuInventoryMap, contextualInfo);
            
            if (getRollbackHandler() != null && !getAutomaticallyRegisterRollbackHandler()) {
                rollbackState.put(DecrementInventoryRollbackHandler.ROLLBACK_BLC_INVENTORY_DECREMENTED, skuInventoryMap);
                rollbackState.put(DecrementInventoryRollbackHandler.ROLLBACK_BLC_ORDER_ID, seed.getOrder().getId());
            }
            
            // add the rollback state that was used in the rollback handler
            rollbackState.put(DecrementInventoryRollbackHandler.EXTENDED_ROLLBACK_STATE, contextualInfo.get(ContextualInventoryService.ROLLBACK_STATE_KEY));
        }

        return context;
    }

}
