
package com.wakacommerce.core.order.service.legacy;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wakacommerce.core.catalog.dao.CategoryDao;
import com.wakacommerce.core.catalog.dao.ProductDao;
import com.wakacommerce.core.catalog.dao.SkuDao;
import com.wakacommerce.core.catalog.domain.Category;
import com.wakacommerce.core.catalog.domain.Product;
import com.wakacommerce.core.catalog.domain.ProductBundle;
import com.wakacommerce.core.catalog.domain.ProductOption;
import com.wakacommerce.core.catalog.domain.ProductOptionValue;
import com.wakacommerce.core.catalog.domain.Sku;
import com.wakacommerce.core.catalog.domain.SkuBundleItem;
import com.wakacommerce.core.order.dao.FulfillmentGroupDao;
import com.wakacommerce.core.order.dao.FulfillmentGroupItemDao;
import com.wakacommerce.core.order.dao.OrderDao;
import com.wakacommerce.core.order.dao.OrderItemDao;
import com.wakacommerce.core.order.domain.BundleOrderItem;
import com.wakacommerce.core.order.domain.DiscreteOrderItem;
import com.wakacommerce.core.order.domain.FulfillmentGroup;
import com.wakacommerce.core.order.domain.FulfillmentGroupItem;
import com.wakacommerce.core.order.domain.Order;
import com.wakacommerce.core.order.domain.OrderItem;
import com.wakacommerce.core.order.domain.OrderItemAttribute;
import com.wakacommerce.core.order.domain.OrderItemAttributeImpl;
import com.wakacommerce.core.order.domain.PersonalMessage;
import com.wakacommerce.core.order.service.FulfillmentGroupService;
import com.wakacommerce.core.order.service.OrderItemService;
import com.wakacommerce.core.order.service.OrderServiceImpl;
import com.wakacommerce.core.order.service.call.BundleOrderItemRequest;
import com.wakacommerce.core.order.service.call.DiscreteOrderItemRequest;
import com.wakacommerce.core.order.service.call.FulfillmentGroupItemRequest;
import com.wakacommerce.core.order.service.call.FulfillmentGroupRequest;
import com.wakacommerce.core.order.service.call.GiftWrapOrderItemRequest;
import com.wakacommerce.core.order.service.call.OrderItemRequestDTO;
import com.wakacommerce.core.order.service.exception.ItemNotFoundException;
import com.wakacommerce.core.order.service.exception.RequiredAttributeNotProvidedException;
import com.wakacommerce.core.order.service.type.OrderItemType;
import com.wakacommerce.core.payment.dao.OrderPaymentDao;
import com.wakacommerce.core.payment.domain.OrderPayment;
import com.wakacommerce.core.pricing.service.exception.PricingException;
import com.wakacommerce.profile.core.domain.Address;
import com.wakacommerce.profile.core.domain.Customer;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @ hui
 */
@Deprecated
public class LegacyOrderServiceImpl extends OrderServiceImpl implements LegacyOrderService {

    private static final Log LOG = LogFactory.getLog(LegacyOrderServiceImpl.class);

    @Resource(name = "blFulfillmentGroupDao")
    protected FulfillmentGroupDao fulfillmentGroupDao;

    @Resource(name = "blFulfillmentGroupItemDao")
    protected FulfillmentGroupItemDao fulfillmentGroupItemDao;

    @Resource(name = "blOrderItemService")
    protected OrderItemService orderItemService;

    @Resource(name = "blOrderItemDao")
    protected OrderItemDao orderItemDao;

    @Resource(name = "blSkuDao")
    protected SkuDao skuDao;

    @Resource(name = "blProductDao")
    protected ProductDao productDao;

    @Resource(name = "blCategoryDao")
    protected CategoryDao categoryDao;
    
    @Resource(name = "blFulfillmentGroupService")
    protected FulfillmentGroupService fulfillmentGroupService;

    @Override
    public FulfillmentGroup findDefaultFulfillmentGroupForOrder(Order order) {
        return fulfillmentGroupDao.readDefaultFulfillmentGroupForOrder(order);
    }
    
    public DiscreteOrderItemRequest createDiscreteOrderItemRequest(Order order, BundleOrderItem bundleOrderItem, Sku sku, Product product, Category category, Integer quantity, Map<String, String> itemAttributes) {
        DiscreteOrderItemRequest itemRequest = new DiscreteOrderItemRequest();
        itemRequest.setOrder(order);
        itemRequest.setBundleOrderItem(bundleOrderItem);
        itemRequest.setCategory(category);
        itemRequest.setProduct(product);
        itemRequest.setQuantity(quantity);
        itemRequest.setSku(sku);
        itemRequest.setItemAttributes(itemAttributes);
        return itemRequest;
    }
    
    @Override
    public DiscreteOrderItemRequest createDiscreteOrderItemRequest(Long orderId, Long skuId, Long productId, Long categoryId, Integer quantity) {
        Sku sku = skuDao.readSkuById(skuId);
        Product product;
        if (productId != null) {
            product = productDao.readProductById(productId);
        } else {
            product = null;
        }
        Category category;
        if (categoryId != null) {
            category = categoryDao.readCategoryById(categoryId);
        } else {
            category = null;
        }
        
        Order order = orderDao.readOrderById(orderId);
        
        return createDiscreteOrderItemRequest(order, null, sku, product, category, quantity, null);
    }


    @Override
    public OrderItem addGiftWrapItemToOrder(Order order, GiftWrapOrderItemRequest itemRequest) throws PricingException {
        itemRequest.setOrder(order);
        return addGiftWrapItemToOrder(order, itemRequest, true);
    }

    @Override
    public OrderItem addBundleItemToOrder(Order order, BundleOrderItemRequest itemRequest) throws PricingException {
        return addBundleItemToOrder(order, itemRequest, true);
    }

    @Override
    public OrderItem addBundleItemToOrder(Order order, BundleOrderItemRequest itemRequest, boolean priceOrder) throws PricingException {
        itemRequest.setOrder(order);
        BundleOrderItem item = orderItemService.createBundleOrderItem(itemRequest);
        return addOrderItemToOrder(order, item, priceOrder);
    }
    
    @Override
    public Order removeItemFromOrder(Long orderId, Long itemId) throws PricingException {
        return removeItemFromOrder(orderId, itemId, true);
    }

    @Override
    public Order removeItemFromOrder(Long orderId, Long itemId, boolean priceOrder) throws PricingException {
        Order order = findOrderById(orderId);
        OrderItem orderItem = orderItemService.readOrderItemById(itemId);

        return removeItemFromOrder(order, orderItem, priceOrder);
    }
    
    @Override
    public Order removeItemFromOrder(Order order, OrderItem item) throws PricingException {
        return removeItemFromOrder(order, item, true);
    }

    @Override
    public Order removeItemFromOrder(Order order, OrderItem item, boolean priceOrder) throws PricingException {
        fulfillmentGroupService.removeOrderItemFromFullfillmentGroups(order, item);
        OrderItem itemFromOrder = order.getOrderItems().remove(order.getOrderItems().indexOf(item));
        itemFromOrder.setOrder(null);
        orderItemService.delete(itemFromOrder);
        order = updateOrder(order, priceOrder);
        return order;
    }
    
    public Order moveItemToOrder(Order originalOrder, Order destinationOrder, OrderItem item) throws PricingException {
        return moveItemToOrder(originalOrder, destinationOrder, item, true);
    }
    
    public Order moveItemToOrder(Order originalOrder, Order destinationOrder, OrderItem item, boolean priceOrder) throws PricingException {
        fulfillmentGroupService.removeOrderItemFromFullfillmentGroups(originalOrder, item);
        OrderItem itemFromOrder = originalOrder.getOrderItems().remove(originalOrder.getOrderItems().indexOf(item));
        itemFromOrder.setOrder(null);
        originalOrder = updateOrder(originalOrder, priceOrder);
        addOrderItemToOrder(destinationOrder, item, priceOrder);
        return destinationOrder;
    }

    @Override
    public OrderPayment addPaymentToOrder(Order order, OrderPayment payment) {
        return addPaymentToOrder(order, payment, null);
    }

    @Override
    public FulfillmentGroup addFulfillmentGroupToOrder(FulfillmentGroupRequest fulfillmentGroupRequest) throws PricingException {
        return addFulfillmentGroupToOrder(fulfillmentGroupRequest, true);
    }

    @Override
    public FulfillmentGroup addFulfillmentGroupToOrder(FulfillmentGroupRequest fulfillmentGroupRequest, boolean priceOrder) throws PricingException {
        FulfillmentGroup fg = fulfillmentGroupDao.create();
        fg.setAddress(fulfillmentGroupRequest.getAddress());
        fg.setOrder(fulfillmentGroupRequest.getOrder());
        fg.setPhone(fulfillmentGroupRequest.getPhone());
        fg.setMethod(fulfillmentGroupRequest.getMethod());
        fg.setService(fulfillmentGroupRequest.getService());

        for (int i=0; i< fulfillmentGroupRequest.getFulfillmentGroupItemRequests().size(); i++) {
            FulfillmentGroupItemRequest request = fulfillmentGroupRequest.getFulfillmentGroupItemRequests().get(i);
            boolean shouldPriceOrder = (priceOrder && (i == (fulfillmentGroupRequest.getFulfillmentGroupItemRequests().size() - 1)));
            fg = addItemToFulfillmentGroup(request.getOrderItem(), fg, request.getQuantity(), shouldPriceOrder);
        }

        return fg;
    }
    
    @Override
    public FulfillmentGroup addFulfillmentGroupToOrder(Order order, FulfillmentGroup fulfillmentGroup) throws PricingException {
        return addFulfillmentGroupToOrder(order, fulfillmentGroup, true);
    }

    @Override
    public FulfillmentGroup addFulfillmentGroupToOrder(Order order, FulfillmentGroup fulfillmentGroup, boolean priceOrder) throws PricingException {
        FulfillmentGroup dfg = findDefaultFulfillmentGroupForOrder(order);
        if (dfg == null) {
            fulfillmentGroup.setPrimary(true);
        } else if (dfg.equals(fulfillmentGroup)) {
            // API user is trying to re-add the default fulfillment group to the
            // same order
            fulfillmentGroup.setPrimary(true);
            order.getFulfillmentGroups().remove(dfg);
            // fulfillmentGroupDao.delete(dfg);
        }

        fulfillmentGroup.setOrder(order);
        // 1) For each item in the new fulfillment group
        for (FulfillmentGroupItem fgItem : fulfillmentGroup.getFulfillmentGroupItems()) {
            // 2) Find the item's existing fulfillment group
            for (FulfillmentGroup fg : order.getFulfillmentGroups()) {
                // If the existing fulfillment group is different than passed in
                // fulfillment group
                if (!fg.equals(fulfillmentGroup)) {
                    // 3) remove item from it's existing fulfillment
                    // group
                    fg.getFulfillmentGroupItems().remove(fgItem);
                }
            }
        }
        fulfillmentGroup = fulfillmentGroupDao.save(fulfillmentGroup);
        order.getFulfillmentGroups().add(fulfillmentGroup);
        int fulfillmentGroupIndex = order.getFulfillmentGroups().size() - 1;
        order = updateOrder(order, priceOrder);
        return order.getFulfillmentGroups().get(fulfillmentGroupIndex);
    }
    
    @Override
    public FulfillmentGroup addItemToFulfillmentGroup(OrderItem item, FulfillmentGroup fulfillmentGroup, int quantity) throws PricingException {
        return addItemToFulfillmentGroup(item, fulfillmentGroup, quantity, true);
    }

    @Override
    public FulfillmentGroup addItemToFulfillmentGroup(OrderItem item, FulfillmentGroup fulfillmentGroup, int quantity, boolean priceOrder) throws PricingException {
        return addItemToFulfillmentGroup(item.getOrder(), item, fulfillmentGroup, quantity, priceOrder);
    }

    @Override
    public FulfillmentGroup addItemToFulfillmentGroup(Order order, OrderItem item, FulfillmentGroup fulfillmentGroup, int quantity, boolean priceOrder) throws PricingException {

        // 1) Find the item's existing fulfillment group, if any
        for (FulfillmentGroup fg : order.getFulfillmentGroups()) {
            Iterator<FulfillmentGroupItem> itr = fg.getFulfillmentGroupItems().iterator();
            while (itr.hasNext()) {
                FulfillmentGroupItem fgItem = itr.next();
                if (fgItem.getOrderItem().equals(item)) {
                    // 2) remove item from it's existing fulfillment group
                    itr.remove();
                    fulfillmentGroupItemDao.delete(fgItem);
                }
            }
        }

        if (fulfillmentGroup.getId() == null) {
            // API user is trying to add an item to a fulfillment group not created
            fulfillmentGroup = addFulfillmentGroupToOrder(order, fulfillmentGroup, priceOrder);
        }

        FulfillmentGroupItem fgi = createFulfillmentGroupItemFromOrderItem(item, fulfillmentGroup, quantity);
        fgi = fulfillmentGroupItemDao.save(fgi);

        // 3) add the item to the new fulfillment group
        fulfillmentGroup.addFulfillmentGroupItem(fgi);
        order = updateOrder(order, priceOrder);

        return fulfillmentGroup;
    }

    @Override
    public FulfillmentGroup addItemToFulfillmentGroup(OrderItem item, FulfillmentGroup fulfillmentGroup) throws PricingException {
        return addItemToFulfillmentGroup(item, fulfillmentGroup, true);
    }

    @Override
    public FulfillmentGroup addItemToFulfillmentGroup(OrderItem item, FulfillmentGroup fulfillmentGroup, boolean priceOrder) throws PricingException {
        return addItemToFulfillmentGroup(item, fulfillmentGroup, item.getQuantity(), priceOrder);
    }
    
    @Override
    public void updateItemQuantity(Order order, OrderItem item) throws ItemNotFoundException, PricingException {
        updateItemQuantity(order, item, true);
    }
    
    @Override
    public void updateItemQuantity(Order order, OrderItemRequestDTO orderItemRequestDTO) throws ItemNotFoundException, PricingException {
        OrderItem orderItem = null;
        for (DiscreteOrderItem doi : order.getDiscreteOrderItems()) {
            if (doi.getId().equals(orderItemRequestDTO.getOrderItemId())) {
                orderItem = doi;
            }
        }
        
        orderItem.setQuantity(orderItemRequestDTO.getQuantity());
        
        updateItemQuantity(order, orderItem, true);
    }
    

    @Override
    public void updateItemQuantity(Order order, OrderItem item, boolean priceOrder) throws ItemNotFoundException, PricingException {
        if (!order.getOrderItems().contains(item)) {
            throw new ItemNotFoundException("Order Item (" + item.getId() + ") not found in Order (" + order.getId() + ")");
        }
        
        if (item.getQuantity() == 0) {
            removeItemFromOrder(order, item);
        } else if (item.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        } else {
            OrderItem itemFromOrder = order.getOrderItems().get(order.getOrderItems().indexOf(item));
            itemFromOrder.setQuantity(item.getQuantity());
            order = updateOrder(order, priceOrder);
        } 
    }

    @Override
    public void removeAllFulfillmentGroupsFromOrder(Order order) throws PricingException {
        removeAllFulfillmentGroupsFromOrder(order, false);
    }

    @Override
    public void removeAllFulfillmentGroupsFromOrder(Order order, boolean priceOrder) throws PricingException {
        if (order.getFulfillmentGroups() != null) {
            for (Iterator<FulfillmentGroup> iterator = order.getFulfillmentGroups().iterator(); iterator.hasNext();) {
                FulfillmentGroup fulfillmentGroup = iterator.next();
                iterator.remove();
                fulfillmentGroupDao.delete(fulfillmentGroup);
            }
            updateOrder(order, priceOrder);
        }
    }
    
    @Override
    public void removeFulfillmentGroupFromOrder(Order order, FulfillmentGroup fulfillmentGroup) throws PricingException {
        removeFulfillmentGroupFromOrder(order, fulfillmentGroup, true);
    }

    @Override
    public void removeFulfillmentGroupFromOrder(Order order, FulfillmentGroup fulfillmentGroup, boolean priceOrder) throws PricingException {
        order.getFulfillmentGroups().remove(fulfillmentGroup);
        fulfillmentGroupDao.delete(fulfillmentGroup);
        updateOrder(order, priceOrder);
    }

    @Override
    public void removeNamedOrderForCustomer(String name, Customer customer) {
        Order namedOrder = findNamedOrderForCustomer(name, customer);
        cancelOrder(namedOrder);
    }

    @Override
    public List<OrderPayment> readPaymentInfosForOrder(Order order) {
        return paymentDao.readPaymentsForOrder(order);
    }
        
    protected boolean itemMatches(DiscreteOrderItem item1, DiscreteOrderItem item2) {
        // Must match on SKU and options
        if (item1.getSku() != null && item2.getSku() != null) {
            if (item1.getSku().getId().equals(item2.getSku().getId())) {
                // TODO: Compare options if product has product options
                return true;
            }
        } else {
            if (item1.getProduct() != null && item2.getProduct() != null) {
                if (item1.getProduct().getId().equals(item2.getProduct().getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected OrderItem findMatchingDiscreteItem(Order order, DiscreteOrderItem itemToFind) {
        for (int i=(order.getOrderItems().size()-1); i >= 0; i--) {
            OrderItem currentItem = (order.getOrderItems().get(i));
            if (currentItem instanceof DiscreteOrderItem) {
                DiscreteOrderItem discreteItem = (DiscreteOrderItem) currentItem;
                if (itemMatches(discreteItem, itemToFind)) {
                    return discreteItem;
                }

            } else if (currentItem instanceof BundleOrderItem) {
                for (DiscreteOrderItem discreteItem : (((BundleOrderItem) currentItem).getDiscreteOrderItems())) {
                    if (itemMatches(discreteItem, itemToFind)) {
                        return discreteItem;
                    }
                }
            }
        }
        return null;
    }

    protected boolean bundleItemMatches(BundleOrderItem item1, BundleOrderItem item2) {
        if (item1.getSku() != null && item2.getSku() != null) {
            return item1.getSku().getId().equals(item2.getSku().getId());
        }

        // Otherwise, scan the items.
        HashMap<Long, Integer> skuMap = new HashMap<Long, Integer>();
        for(DiscreteOrderItem item : item1.getDiscreteOrderItems()) {
            if (skuMap.get(item.getSku().getId()) == null) {
                skuMap.put(item.getSku().getId(), Integer.valueOf(item.getQuantity()));
            } else {
                Integer qty = skuMap.get(item.getSku().getId());
                skuMap.put(item.getSku().getId(), Integer.valueOf(qty + item.getQuantity()));
            }
        }

        // Now consume the quantities in the map
        for(DiscreteOrderItem item : item2.getDiscreteOrderItems()) {
            if (skuMap.containsKey(item.getSku().getId())) {
                Integer qty = skuMap.get(item.getSku().getId());
                Integer newQty = Integer.valueOf(qty - item.getQuantity());
                if (newQty.intValue() == 0) {
                    skuMap.remove(item.getSku().getId());
                } else if (newQty.intValue() > 0) {
                    skuMap.put(item.getSku().getId(), newQty);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return skuMap.isEmpty();
    }

    protected OrderItem findMatchingBundleItem(Order order, BundleOrderItem itemToFind) {
        for (int i=(order.getOrderItems().size()-1); i >= 0; i--) {
            OrderItem currentItem = (order.getOrderItems().get(i));
            if (currentItem instanceof BundleOrderItem) {
                if (bundleItemMatches((BundleOrderItem) currentItem, itemToFind)) {
                    return currentItem;
                }
            }
        }
        return null;
    }

    protected OrderItem findMatchingItem(Order order, OrderItem itemToFind) {
        if (itemToFind instanceof BundleOrderItem) {
            return findMatchingBundleItem(order, (BundleOrderItem) itemToFind);
        } else if (itemToFind instanceof DiscreteOrderItem) {
            return findMatchingDiscreteItem(order, (DiscreteOrderItem) itemToFind);
        } else {
            return null;
        }
    }

    @Override
    public OrderItem addOrderItemToBundle(Order order, BundleOrderItem bundle, DiscreteOrderItem newOrderItem, boolean priceOrder) throws PricingException {
        List<DiscreteOrderItem> orderItems = bundle.getDiscreteOrderItems();
        orderItems.add(newOrderItem);
        newOrderItem.setBundleOrderItem(bundle);

        order = updateOrder(order, priceOrder);

        return findMatchingItem(order, bundle);
    }
    
    @Override
    public Order removeItemFromBundle(Order order, BundleOrderItem bundle, OrderItem item, boolean priceOrder) throws PricingException {
        DiscreteOrderItem itemFromBundle = bundle.getDiscreteOrderItems().remove(bundle.getDiscreteOrderItems().indexOf(item));
        orderItemService.delete(itemFromBundle);
        itemFromBundle.setBundleOrderItem(null);
        order = updateOrder(order, priceOrder);
        
        return order;
    }

    @Override
    public Order addOrUpdateOrderItemAttributes(Order order, OrderItem item, Map<String,String> attributeValues, boolean priceOrder) throws ItemNotFoundException, PricingException {
        if (!order.getOrderItems().contains(item)) {
            throw new ItemNotFoundException("Order Item (" + item.getId() + ") not found in Order (" + order.getId() + ")");
        }
        OrderItem itemFromOrder = order.getOrderItems().get(order.getOrderItems().indexOf(item));
        
        Map<String,OrderItemAttribute> orderItemAttributes = itemFromOrder.getOrderItemAttributes();
        if (orderItemAttributes == null) {
            orderItemAttributes = new HashMap<String,OrderItemAttribute>();
            itemFromOrder.setOrderItemAttributes(orderItemAttributes);
        }
        
        boolean changeMade = false;
        for (String attributeName : attributeValues.keySet()) {
            String attributeValue = attributeValues.get(attributeName);
            OrderItemAttribute attribute = orderItemAttributes.get(attributeName);
            if (attribute != null && attribute.getValue().equals(attributeValue)) {
                // no change made.
                continue;
            } else {
                changeMade = true;
                if (attribute == null) {
                    attribute = new OrderItemAttributeImpl();
                    attribute.setOrderItem(itemFromOrder);
                    attribute.setName(attributeName);
                    attribute.setValue(attributeValue);
                } else if (attributeValue == null) {
                    orderItemAttributes.remove(attributeValue);
                } else {
                    attribute.setValue(attributeValue);
                }
            }
        }

        if (changeMade) {
            return updateOrder(order, priceOrder);
        } else {
            return order;
        }
    }

    @Override
    public Order removeOrderItemAttribute(Order order, OrderItem item, String attributeName, boolean priceOrder) throws ItemNotFoundException, PricingException {
        if (!order.getOrderItems().contains(item)) {
            throw new ItemNotFoundException("Order Item (" + item.getId() + ") not found in Order (" + order.getId() + ")");
        }
        OrderItem itemFromOrder = order.getOrderItems().get(order.getOrderItems().indexOf(item));

        boolean changeMade = false;
        Map<String,OrderItemAttribute> orderItemAttributes = itemFromOrder.getOrderItemAttributes();
        if (orderItemAttributes != null) {
            if (orderItemAttributes.containsKey(attributeName)) {
                changeMade = true;
                orderItemAttributes.remove(attributeName);
            }
        }

        if (changeMade) {
            return updateOrder(order, priceOrder);
        } else {
            return order;
        }
    }

    @Override
    public FulfillmentGroup createDefaultFulfillmentGroup(Order order, Address address) {
        for (FulfillmentGroup fulfillmentGroup : order.getFulfillmentGroups()) {
            if (fulfillmentGroup.isPrimary()) {
                return fulfillmentGroup;
            }
        }

        FulfillmentGroup newFg = fulfillmentGroupService.createEmptyFulfillmentGroup();
        newFg.setOrder(order);
        newFg.setPrimary(true);
        newFg.setAddress(address);
        for (OrderItem orderItem : order.getOrderItems()) {
            newFg.addFulfillmentGroupItem(createFulfillmentGroupItemFromOrderItem(orderItem, newFg, orderItem.getQuantity()));
        }
        return newFg;
    }

    public OrderDao getOrderDao() {
        return orderDao;
    }

    public void setOrderDao(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public OrderPaymentDao getPaymentInfoDao() {
        return paymentDao;
    }

    public void setPaymentInfoDao(OrderPaymentDao paymentInfoDao) {
        this.paymentDao = paymentInfoDao;
    }

    public FulfillmentGroupDao getFulfillmentGroupDao() {
        return fulfillmentGroupDao;
    }

    public void setFulfillmentGroupDao(FulfillmentGroupDao fulfillmentGroupDao) {
        this.fulfillmentGroupDao = fulfillmentGroupDao;
    }

    public FulfillmentGroupItemDao getFulfillmentGroupItemDao() {
        return fulfillmentGroupItemDao;
    }

    public void setFulfillmentGroupItemDao(FulfillmentGroupItemDao fulfillmentGroupItemDao) {
        this.fulfillmentGroupItemDao = fulfillmentGroupItemDao;
    }

    public OrderItemService getOrderItemService() {
        return orderItemService;
    }

    public void setOrderItemService(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @Override
    public Order findOrderByOrderNumber(String orderNumber) {
        return orderDao.readOrderByOrderNumber(orderNumber);
    }

    protected Order updateOrder(Order order, Boolean priceOrder) throws PricingException {
        if (priceOrder) {
            order = pricingService.executePricing(order);
        }
        return persistOrder(order);
    }


    protected Order persistOrder(Order order) {
        return orderDao.save(order);
    }

    protected FulfillmentGroupItem createFulfillmentGroupItemFromOrderItem(OrderItem orderItem, FulfillmentGroup fulfillmentGroup, int quantity) {
        FulfillmentGroupItem fgi = fulfillmentGroupItemDao.create();
        fgi.setFulfillmentGroup(fulfillmentGroup);
        fgi.setOrderItem(orderItem);
        fgi.setQuantity(quantity);
        return fgi;
    }
    
    protected void removeOrderItemFromFullfillmentGroup(Order order, OrderItem orderItem) {
        List<FulfillmentGroup> fulfillmentGroups = order.getFulfillmentGroups();
        for (FulfillmentGroup fulfillmentGroup : fulfillmentGroups) {
            Iterator<FulfillmentGroupItem> itr = fulfillmentGroup.getFulfillmentGroupItems().iterator();
            while (itr.hasNext()) {
                FulfillmentGroupItem fulfillmentGroupItem = itr.next();
                if (fulfillmentGroupItem.getOrderItem().equals(orderItem)) {
                    itr.remove();
                    fulfillmentGroupItemDao.delete(fulfillmentGroupItem);
                } else if (orderItem instanceof BundleOrderItem) {
                    BundleOrderItem bundleOrderItem = (BundleOrderItem) orderItem;
                    for (DiscreteOrderItem discreteOrderItem : bundleOrderItem.getDiscreteOrderItems()) {
                        if (fulfillmentGroupItem.getOrderItem().equals(discreteOrderItem)){
                            itr.remove();
                            fulfillmentGroupItemDao.delete(fulfillmentGroupItem);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected DiscreteOrderItemRequest createDiscreteOrderItemRequest(DiscreteOrderItem discreteOrderItem) {
        DiscreteOrderItemRequest itemRequest = new DiscreteOrderItemRequest();
        itemRequest.setCategory(discreteOrderItem.getCategory());
        itemRequest.setProduct(discreteOrderItem.getProduct());
        itemRequest.setQuantity(discreteOrderItem.getQuantity());
        itemRequest.setSku(discreteOrderItem.getSku());
        
        if (discreteOrderItem.getPersonalMessage() != null) {
            PersonalMessage personalMessage = orderItemService.createPersonalMessage();
            try {
                BeanUtils.copyProperties(personalMessage, discreteOrderItem.getPersonalMessage());
                personalMessage.setId(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            itemRequest.setPersonalMessage(personalMessage);
        }
        
        return itemRequest;
    }

    protected BundleOrderItemRequest createBundleOrderItemRequest(BundleOrderItem bundleOrderItem, List<DiscreteOrderItemRequest> discreteOrderItemRequests) {
        BundleOrderItemRequest bundleOrderItemRequest = new BundleOrderItemRequest();
        bundleOrderItemRequest.setCategory(bundleOrderItem.getCategory());
        bundleOrderItemRequest.setName(bundleOrderItem.getName());
        bundleOrderItemRequest.setQuantity(bundleOrderItem.getQuantity());
        bundleOrderItemRequest.setDiscreteOrderItems(discreteOrderItemRequests);
        return bundleOrderItemRequest;
    }

    protected Order validateOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId required when adding item to order.");
        }

        Order order = findOrderById(orderId);
        
        if (order == null) {
            throw new IllegalArgumentException("No order found matching passed in orderId " + orderId + " while trying to addItemToOrder.");
        }

        return order;
    }

    protected Product validateProduct(Long productId) {
        if (productId != null) {
            Product product = productDao.readProductById(productId);
            if (product == null) {
                throw new IllegalArgumentException("No product found matching passed in productId " + productId + " while trying to addItemToOrder.");
            }
            return product;
        }
        return null;
    }

    protected Category determineCategory(Product product, Long categoryId) {
        Category category = null;
        if (categoryId != null) {
            category = categoryDao.readCategoryById(categoryId);
        }

        if (category == null && product != null) {
            category = product.getDefaultCategory();
        }
        return category;
    }

    protected Sku determineSku(Product product, Long skuId, Map<String,String> attributeValues) {
        // Check whether the sku is correct given the product options.
        Sku sku = findMatchingSku(product, attributeValues);

        if (sku == null && skuId != null) {
            sku = skuDao.readSkuById(skuId);
        }

        if (sku == null && product != null) {
            // Set to the default sku
            if (product.getAdditionalSkus() != null && product.getAdditionalSkus().size() > 0) {
                throw new RequiredAttributeNotProvidedException("Unable to find non-default sku matching given options");
            }
            sku = product.getDefaultSku();
        }
        return sku;
    }

    protected Sku findMatchingSku(Product product, Map<String,String> attributeValues) {
        Map<String, String> attributeValuesForSku = new HashMap<String,String>();
        // Verify that required product-option values were set.

        if (product != null && product.getProductOptions() != null && product.getProductOptions().size() > 0) {
            for (ProductOption productOption : product.getProductOptions()) {
                if (productOption.getRequired()) {
                    if (attributeValues.get(productOption.getAttributeName()) == null) {
                        throw new RequiredAttributeNotProvidedException("Unable to add to cart. Required attribute was not provided: " + productOption.getAttributeName());
                    } else {
                        attributeValuesForSku.put(productOption.getAttributeName(), attributeValues.get(productOption.getAttributeName()));
                    }
                }
            }

            if (product !=null && product.getSkus() != null) {
                for (Sku sku : product.getSkus()) {
                   if (checkSkuForMatch(sku, attributeValuesForSku)) {
                       return sku;
                   }
                }
            }
        }

        return null;
    }

    protected boolean checkSkuForMatch(Sku sku, Map<String,String> attributeValues) {
        if (attributeValues == null || attributeValues.size() == 0) {
            return false;
        }

        for (String attributeName : attributeValues.keySet()) {
            boolean optionValueMatchFound = false;
            for (ProductOptionValue productOptionValue : sku.getProductOptionValues()) {
                if (productOptionValue.getProductOption().getAttributeName().equals(attributeName)) {
                    if (productOptionValue.getAttributeValue().equals(attributeValues.get(attributeName))) {
                        optionValueMatchFound = true;
                        break;
                    } else {
                        return false;
                    }
                }
            }

            if (optionValueMatchFound) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public Order addItemToOrder(Long orderId, OrderItemRequestDTO orderItemRequestDTO, boolean priceOrder) throws PricingException {
        if (orderItemRequestDTO.getQuantity() == null || orderItemRequestDTO.getQuantity() == 0) {
            LOG.debug("Not adding item to order because quantity is zero.");
            return null;
        }
        
        if (orderItemRequestDTO.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Order order = validateOrder(orderId);
        Product product = validateProduct(orderItemRequestDTO.getProductId());
        Sku sku = determineSku(product, orderItemRequestDTO.getSkuId(), orderItemRequestDTO.getItemAttributes());
        if (sku == null) {
            return null;
        }
        Category category = determineCategory(product, orderItemRequestDTO.getCategoryId());

        if (product == null || ! (product instanceof ProductBundle)) {
            DiscreteOrderItem item = orderItemService.createDiscreteOrderItem(createDiscreteOrderItemRequest(order, null, sku, product, category, orderItemRequestDTO.getQuantity(), orderItemRequestDTO.getItemAttributes()));
            item.setOrder(order);
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.add(item);
            return updateOrder(order, priceOrder);
        } else {
            ProductBundle bundle = (ProductBundle) product;
            BundleOrderItem bundleOrderItem = (BundleOrderItem) orderItemDao.create(OrderItemType.BUNDLE);
            bundleOrderItem.setQuantity(orderItemRequestDTO.getQuantity());
            bundleOrderItem.setCategory(category);
            bundleOrderItem.setSku(sku);
            bundleOrderItem.setName(product.getName());
            bundleOrderItem.setProductBundle(bundle);
            bundleOrderItem.setOrder(order);

            for (SkuBundleItem skuBundleItem : bundle.getSkuBundleItems()) {
                Product bundleProduct = skuBundleItem.getBundle();
                Sku bundleSku = skuBundleItem.getSku();

                Category bundleCategory = determineCategory(bundleProduct, orderItemRequestDTO.getCategoryId());

                DiscreteOrderItem bundleDiscreteItem = orderItemService.createDiscreteOrderItem(createDiscreteOrderItemRequest(null, bundleOrderItem, bundleSku, bundleProduct, bundleCategory, skuBundleItem.getQuantity(), orderItemRequestDTO.getItemAttributes()));
                bundleDiscreteItem.setBundleOrderItem(bundleOrderItem);
                bundleDiscreteItem.setSkuBundleItem(skuBundleItem);
                bundleOrderItem.getDiscreteOrderItems().add(bundleDiscreteItem);
            }

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.add(bundleOrderItem);
            return updateOrder(order, priceOrder);
        }
    }

    /* *********************************************************************************
     * DEPRECATED METHODS                                                              *
     * *********************************************************************************/
    
    @Override
    @Deprecated
    public OrderItem addDiscreteItemToOrder(Order order, DiscreteOrderItemRequest itemRequest) throws PricingException {
        return addDiscreteItemToOrder(order, itemRequest, true);
    }

    @Override
    @Deprecated
    public OrderItem addDiscreteItemToOrder(Order order, DiscreteOrderItemRequest itemRequest, boolean priceOrder) throws PricingException {
        itemRequest.setOrder(order);
        DiscreteOrderItem item = orderItemService.createDiscreteOrderItem(itemRequest);
        return addOrderItemToOrder(order, item, priceOrder);
    }
    
    @Override
    @Deprecated
    public OrderItem addSkuToOrder(Long orderId, Long skuId, Long productId, Long categoryId, Integer quantity) throws PricingException {
        return addSkuToOrder(orderId, skuId, productId, categoryId, quantity, true, null);
    }
    
    @Override
    @Deprecated
    public OrderItem addSkuToOrder(Long orderId, Long skuId, Long productId, Long categoryId, Integer quantity, Map<String,String> itemAttributes) throws PricingException {
        return addSkuToOrder(orderId, skuId, productId, categoryId, quantity, true, itemAttributes);
    }
    
    @Override
    @Deprecated
    public OrderItem addSkuToOrder(Long orderId, Long skuId, Long productId, Long categoryId, Integer quantity, boolean priceOrder) throws PricingException {
        return addSkuToOrder(orderId, skuId, productId, categoryId, quantity, priceOrder, null);
    }

    @Override
    @Deprecated
    public OrderItem addSkuToOrder(Long orderId, Long skuId, Long productId, Long categoryId, Integer quantity, boolean priceOrder, Map<String,String> itemAttributes) throws PricingException {
        if (orderId == null || (productId == null && skuId == null) || quantity == null) {
            return null;
        }

        OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
        orderItemRequestDTO.setCategoryId(categoryId);
        orderItemRequestDTO.setProductId(productId);
        orderItemRequestDTO.setSkuId(skuId);
        orderItemRequestDTO.setQuantity(quantity);
        orderItemRequestDTO.setItemAttributes(itemAttributes);

        Order order = addItemToOrder(orderId, orderItemRequestDTO, priceOrder);
        if (order == null) {
            return null;
        }
        return findLastMatchingItem(order, skuId, productId);
    }
    
    @Override
    @Deprecated
    public OrderItem addOrderItemToOrder(Order order, OrderItem newOrderItem, boolean priceOrder) throws PricingException {
        if (automaticallyMergeLikeItems) {
            OrderItem item = findMatchingItem(order, newOrderItem);
            if (item != null) {
                item.setQuantity(item.getQuantity() + newOrderItem.getQuantity());
                try {
                    updateItemQuantity(order, item, priceOrder);
                } catch (ItemNotFoundException e) {
                    LOG.error(e);
                }
                return findMatchingItem(order, newOrderItem);
            }
        }

        List<OrderItem> orderItems = order.getOrderItems();
        orderItems.add(newOrderItem);
        newOrderItem.setOrder(order);
        order = updateOrder(order, priceOrder);
        return findMatchingItem(order, newOrderItem);
    }
    
    @Override
    @Deprecated
    public OrderItem addOrderItemToOrder(Order order, OrderItem newOrderItem) throws PricingException {
        return addOrderItemToOrder(order, newOrderItem, true);
    }
    
    @Override
    @Deprecated
    public OrderItem addDynamicPriceDiscreteItemToOrder(Order order, DiscreteOrderItemRequest itemRequest, @SuppressWarnings("rawtypes") HashMap skuPricingConsiderations) throws PricingException {
        return addDynamicPriceDiscreteItemToOrder(order, itemRequest, skuPricingConsiderations, true);
    }

    @Override
    @Deprecated
    public OrderItem addDynamicPriceDiscreteItemToOrder(Order order, DiscreteOrderItemRequest itemRequest, @SuppressWarnings("rawtypes") HashMap skuPricingConsiderations, boolean priceOrder) throws PricingException {
        DiscreteOrderItem item = orderItemService.createDynamicPriceDiscreteOrderItem(itemRequest, skuPricingConsiderations);
        return addOrderItemToOrder(order, item, priceOrder);
    }
    
}