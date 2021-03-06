
package com.wakacommerce.core.web.api.endpoint.checkout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;

import com.wakacommerce.core.checkout.service.CheckoutService;
import com.wakacommerce.core.checkout.service.exception.CheckoutException;
import com.wakacommerce.core.checkout.service.workflow.CheckoutResponse;
import com.wakacommerce.core.order.domain.Order;
import com.wakacommerce.core.order.service.OrderService;
import com.wakacommerce.core.payment.domain.OrderPayment;
import com.wakacommerce.core.payment.service.OrderPaymentService;
import com.wakacommerce.core.web.api.BroadleafWebServicesException;
import com.wakacommerce.core.web.api.endpoint.BaseEndpoint;
import com.wakacommerce.core.web.api.wrapper.OrderPaymentWrapper;
import com.wakacommerce.core.web.api.wrapper.OrderWrapper;
import com.wakacommerce.core.web.order.CartState;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * This endpoint depends on JAX-RS to provide checkout services.  It should be extended by components that actually wish 
 * to provide an endpoint.  The annotations such as @Path, @Scope, @Context, @PathParam, @QueryParam, 
 * @GET, @POST, @PUT, and @DELETE are purposely not provided here to allow implementors finer control over 
 * the details of the endpoint.
 * <p/>
 * User:   
 * Date: 4/10/12
 */
public abstract class CheckoutEndpoint extends BaseEndpoint {

    private static final Log LOG = LogFactory.getLog(CheckoutEndpoint.class);

    @Resource(name="blCheckoutService")
    protected CheckoutService checkoutService;

    @Resource(name="blOrderService")
    protected OrderService orderService;

    @Resource(name="blOrderPaymentService")
    protected OrderPaymentService orderPaymentService;

    public List<OrderPaymentWrapper> findPaymentsForOrder(HttpServletRequest request) {
        Order cart = CartState.getCart();
        if (cart != null && cart.getPayments() != null && !cart.getPayments().isEmpty()) {
            List<OrderPayment> payments = cart.getPayments();
            List<OrderPaymentWrapper> paymentWrappers = new ArrayList<OrderPaymentWrapper>();
            for (OrderPayment payment : payments) {
                OrderPaymentWrapper orderPaymentWrapper = (OrderPaymentWrapper) context.getBean(OrderPaymentWrapper.class.getName());
                orderPaymentWrapper.wrapSummary(payment, request);
                paymentWrappers.add(orderPaymentWrapper);
            }
            return paymentWrappers;
        }

        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.CART_NOT_FOUND);
    }

    public OrderPaymentWrapper addPaymentToOrder(HttpServletRequest request,
                                                              OrderPaymentWrapper wrapper) {
        Order cart = CartState.getCart();
        if (cart != null) {
            OrderPayment orderPayment = wrapper.unwrap(request, context);

            if (orderPayment.getOrder() != null && orderPayment.getOrder().getId().equals(cart.getId())) {
                orderPayment = orderPaymentService.save(orderPayment);
                OrderPayment savedPayment = orderService.addPaymentToOrder(cart, orderPayment, null);
                OrderPaymentWrapper orderPaymentWrapper = (OrderPaymentWrapper) context.getBean(OrderPaymentWrapper.class.getName());
                orderPaymentWrapper.wrapSummary(savedPayment, request);
                return orderPaymentWrapper;
            }
        }

        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.CART_NOT_FOUND);

    }

    public OrderWrapper removePaymentFromOrder(HttpServletRequest request, OrderPaymentWrapper wrapper) {

        Order cart = CartState.getCart();
        if (cart != null) {
            OrderPayment orderPayment = wrapper.unwrap(request, context);

            if (orderPayment.getOrder() != null && orderPayment.getOrder().getId().equals(cart.getId())) {
                OrderPayment paymentToRemove = null;
                for (OrderPayment payment : cart.getPayments()) {
                    if (payment.getId().equals(orderPayment.getId())) {
                        paymentToRemove = payment;
                    }
                }

                orderService.removePaymentFromOrder(cart, paymentToRemove);

                OrderWrapper orderWrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                orderWrapper.wrapDetails(cart, request);
                return orderWrapper;
            }
        }

        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.CART_NOT_FOUND);
    }

    public OrderWrapper performCheckout(HttpServletRequest request) {
        Order cart = CartState.getCart();
        if (cart != null) {
            try {
                CheckoutResponse response = checkoutService.performCheckout(cart);
                Order order = response.getOrder();
                OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                wrapper.wrapDetails(order, request);
                return wrapper;
            } catch (CheckoutException e) {
                throw BroadleafWebServicesException.build(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .addMessage(BroadleafWebServicesException.CHECKOUT_PROCESSING_ERROR);
            }
        }

        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.CART_NOT_FOUND);

    }
}
