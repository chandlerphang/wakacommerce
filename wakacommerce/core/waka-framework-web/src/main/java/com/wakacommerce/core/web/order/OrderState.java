
package com.wakacommerce.core.web.order;

import com.wakacommerce.core.order.dao.OrderDao;
import com.wakacommerce.core.order.domain.Order;
import com.wakacommerce.profile.core.domain.Customer;

import java.util.HashMap;

import javax.annotation.Resource;

/**
 * This class is used as a request-scope container for the current
 * orderid. As a result, items that need the order during the control
 * flow of a single request may retrieve the order from this object. OrderState
 * utilizes the DAO to retrieve the full order from its dehydrated state in the
 * Hibernate cache.
 * 
 *  
 *
 */
public class OrderState {

    private final HashMap<Long, Long> orders = new HashMap<Long, Long>();

    @Resource(name = "blOrderDao")
    protected OrderDao orderDao;

    public Order getOrder(Customer customer) {
        if (orders.get(customer.getId()) == null) {
            return null;
        }
        Order order = orderDao.readOrderById(orders.get(customer.getId()));
        return order;
    }

    public Order setOrder(Customer customer, Order order) {
        if (customer != null && order != null) {
            orders.put(customer.getId(), order.getId());
        }
        return order;
    }

}
