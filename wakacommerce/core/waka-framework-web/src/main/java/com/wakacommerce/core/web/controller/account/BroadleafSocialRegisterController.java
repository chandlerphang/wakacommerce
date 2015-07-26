
package com.wakacommerce.core.web.controller.account;

import org.apache.commons.lang.StringUtils;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.ServletWebRequest;

import com.wakacommerce.common.exception.ServiceException;
import com.wakacommerce.core.order.domain.NullOrderImpl;
import com.wakacommerce.core.order.domain.Order;
import com.wakacommerce.core.pricing.service.exception.PricingException;
import com.wakacommerce.core.web.order.CartState;
import com.wakacommerce.profile.core.domain.Customer;
import com.wakacommerce.profile.web.core.form.RegisterCustomerForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is an extension of BroadleafRegisterController
 * that utilizes Spring Social to register a customer from a Service Provider
 * such as Facebook or Twitter.
 *
 * To use: extend this class and provide @RequestMapping annotations
 *
 * @see com.wakacommerce.core.web.controller.account.BroadleafRegisterController
 *elbertbautista
 *
 */
public class BroadleafSocialRegisterController extends BroadleafRegisterController {

    //Pre-populate portions of the RegisterCustomerForm from ProviderSignInUtils.getConnection();
    public String register(RegisterCustomerForm registerCustomerForm, HttpServletRequest request,
                           HttpServletResponse response, Model model) {
        Connection<?> connection = ProviderSignInUtils.getConnection(new ServletWebRequest(request));
        if (connection != null) {
            UserProfile userProfile = connection.fetchUserProfile();
            Customer customer = registerCustomerForm.getCustomer();
            customer.setFirstName(userProfile.getFirstName());
            customer.setLastName(userProfile.getLastName());
            customer.setEmailAddress(userProfile.getEmail());
            if (isUseEmailForLogin()){
                customer.setUsername(userProfile.getEmail());
            } else {
                customer.setUsername(userProfile.getUsername());
            }
        }

        return super.register(registerCustomerForm, request, response, model);
    }

    //Calls ProviderSignInUtils.handlePostSignUp() after a successful registration
    public String processRegister(RegisterCustomerForm registerCustomerForm, BindingResult errors,
                                  HttpServletRequest request, HttpServletResponse response, Model model)
            throws ServiceException, PricingException {
        if (isUseEmailForLogin()) {
            Customer customer = registerCustomerForm.getCustomer();
            customer.setUsername(customer.getEmailAddress());
        }

        registerCustomerValidator.validate(registerCustomerForm, errors, isUseEmailForLogin());
        if (!errors.hasErrors()) {
            Customer newCustomer = customerService.registerCustomer(registerCustomerForm.getCustomer(),
                    registerCustomerForm.getPassword(), registerCustomerForm.getPasswordConfirm());
            assert(newCustomer != null);

            ProviderSignInUtils.handlePostSignUp(newCustomer.getUsername(), new ServletWebRequest(request));

            // The next line needs to use the customer from the input form and not the customer returned after registration
            // so that we still have the unencoded password for use by the authentication mechanism.
            loginService.loginCustomer(registerCustomerForm.getCustomer());

            // Need to ensure that the Cart on CartState is owned by the newly registered customer.
            Order cart = CartState.getCart();
            if (cart != null && !(cart instanceof NullOrderImpl) && cart.getEmailAddress() == null) {
                cart.setEmailAddress(newCustomer.getEmailAddress());
                orderService.save(cart, false);
            }

            String redirectUrl = registerCustomerForm.getRedirectUrl();
            if (StringUtils.isNotBlank(redirectUrl) && redirectUrl.contains(":")) {
                redirectUrl = null;
            }
            return StringUtils.isBlank(redirectUrl) ? getRegisterSuccessView() : "redirect:" + redirectUrl;
        } else {
            return getRegisterView();
        }
    }

}
