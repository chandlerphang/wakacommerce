
package com.wakacommerce.core.web.order;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.wakacommerce.core.order.domain.Order;
import com.wakacommerce.profile.core.domain.Customer;

public class OrderStateAOP implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public Object processOrderRetrieval(ProceedingJoinPoint call) throws Throwable {
        Object returnValue;
        /*
         * we retrieve the OrderState instance directly from the application
         * context, as this bean has a request scope.
         */
        OrderState orderState = (OrderState) applicationContext.getBean("blOrderState");
        Customer customer = (Customer) call.getArgs()[0];
        Order order = orderState.getOrder(customer);
        if (order != null) {
            returnValue = order;
        } else {
            returnValue = call.proceed();
            returnValue = orderState.setOrder(customer, (Order) returnValue);
        }

        return returnValue;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
