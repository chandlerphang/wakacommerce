package com.wakacommerce.profile.web.controller;

import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.wakacommerce.common.persistence.EntityConfiguration;
import com.wakacommerce.profile.core.domain.CustomerPhone;
import com.wakacommerce.profile.core.domain.Phone;
import com.wakacommerce.profile.core.service.CustomerPhoneService;
import com.wakacommerce.profile.web.controller.validator.CustomerPhoneValidator;
import com.wakacommerce.profile.web.controller.validator.PhoneValidator;
import com.wakacommerce.profile.web.core.CustomerState;
import com.wakacommerce.profile.web.core.model.PhoneNameForm;
import com.wakacommerce.profile.web.core.util.PhoneFormatter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller("blCustomerPhoneController")
@RequestMapping("/myaccount/phone")
public class CustomerPhoneController {
    private static final String prefix = "myAccount/phone/customerPhones";
    private static final String redirect = "redirect:/myaccount/phone/viewPhone.htm";

    @Resource(name="blCustomerPhoneService")
    private CustomerPhoneService customerPhoneService;
    @Resource(name="blCustomerPhoneValidator")
    private CustomerPhoneValidator customerPhoneValidator;
    @Resource(name="blCustomerState")
    private CustomerState customerState;
    @Resource(name="blEntityConfiguration")
    private EntityConfiguration entityConfiguration;
    @Resource(name="blPhoneFormatter")
    private PhoneFormatter phoneFormatter;
    @Resource(name="blPhoneValidator")
    private PhoneValidator phoneValidator;

    /* ??? -
     * Will these static defaults alter their ability to be
     * overwritten via the appContext?  TODO scc: test this scenario
     * */
    private String deletePhoneSuccessView = redirect;
    private String makePhoneDefaultSuccessView = redirect;
    private String savePhoneErrorView = prefix;
    private String savePhoneSuccessView = prefix;
    private String viewPhoneErrorView = prefix;
    private String viewPhoneSuccessView = prefix;

    @RequestMapping(value="deletePhone", method =  {
            RequestMethod.GET, RequestMethod.POST}
    )
    public String deletePhone(@RequestParam(required = true)
            Long customerPhoneId, HttpServletRequest request) {
        customerPhoneService.deleteCustomerPhoneById(customerPhoneId);

        request.setAttribute("phone.deletedPhone", "true");

        return deletePhoneSuccessView + customerPhoneId;
    }

    @ModelAttribute("phoneNameForm")
    public PhoneNameForm initPhoneNameForm(HttpServletRequest request, Model model) {
        PhoneNameForm form = new PhoneNameForm();
        form.setPhone((Phone) entityConfiguration.createEntityInstance("com.wakacommerce.profile.core.domain.Phone"));

        return form;
    }

    @RequestMapping(value="makePhoneDefault", method =  {
            RequestMethod.GET, RequestMethod.POST}
    )
    public String makePhoneDefault(@RequestParam(required = true)
            Long customerPhoneId, HttpServletRequest request) {
        CustomerPhone customerPhone = customerPhoneService.readCustomerPhoneById(customerPhoneId);
        customerPhoneService.makeCustomerPhoneDefault(customerPhone.getId(), customerPhone.getCustomer().getId());

        request.setAttribute("phone.madePhoneDefault", "true");

        return makePhoneDefaultSuccessView;
    }

    @RequestMapping(value="savePhone", method =  {
            RequestMethod.GET, RequestMethod.POST}
    )
    public String savePhone(@ModelAttribute("phoneNameForm")
            PhoneNameForm phoneNameForm, BindingResult errors, HttpServletRequest request, @RequestParam(required = false)
            Long customerPhoneId, @RequestParam(required = false)
            Long phoneId) {
        if (GenericValidator.isBlankOrNull(phoneNameForm.getPhoneName())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phoneName", "phoneName.required");
        }

        if(phoneId != null){
            phoneNameForm.getPhone().setId(phoneId);
        }

        phoneFormatter.formatPhoneNumber(phoneNameForm.getPhone());
        errors.pushNestedPath("phone");
        phoneValidator.validate(phoneNameForm.getPhone(), errors);
        errors.popNestedPath();

        if (!errors.hasErrors()) {
            CustomerPhone customerPhone = (CustomerPhone) entityConfiguration.createEntityInstance("com.wakacommerce.profile.core.domain.CustomerPhone");
            customerPhone.setCustomer(customerState.getCustomer(request));
            customerPhone.setPhoneName(phoneNameForm.getPhoneName());
            customerPhone.setPhone(phoneNameForm.getPhone());

            if ((customerPhoneId != null) && (customerPhoneId > 0)) {
                customerPhone.setId(customerPhoneId);
            }

            customerPhoneValidator.validate(customerPhone, errors);

            if (!errors.hasErrors()) {
                customerPhone = customerPhoneService.saveCustomerPhone(customerPhone);
                request.setAttribute("customerPhoneId", customerPhone.getId());
                request.setAttribute("phoneId", customerPhone.getPhone().getId());
            }

            return savePhoneSuccessView;
        } else {
            return savePhoneErrorView;
        }
    }

    public void setCustomerPhoneService(CustomerPhoneService customerPhoneService) {
        this.customerPhoneService = customerPhoneService;
    }

    public void setCustomerPhoneValidator(CustomerPhoneValidator customerPhoneValidator) {
        this.customerPhoneValidator = customerPhoneValidator;
    }

    public void setCustomerState(CustomerState customerState) {
        this.customerState = customerState;
    }

    public void setdeletePhoneSuccessView(String deletePhoneSuccessView) {
        this.deletePhoneSuccessView = deletePhoneSuccessView;
    }

    public void setEntityConfiguration(EntityConfiguration entityConfiguration) {
        this.entityConfiguration = entityConfiguration;
    }

    public void setmakePhoneDefaultSuccessView(String makePhoneDefaultSuccessView) {
        this.makePhoneDefaultSuccessView = makePhoneDefaultSuccessView;
    }

    public void setPhoneFormatter(PhoneFormatter phoneFormatter) {
        this.phoneFormatter = phoneFormatter;
    }

    public void setPhoneValidator(PhoneValidator phoneValidator) {
        this.phoneValidator = phoneValidator;
    }

    public void setsavePhoneErrorView(String savePhoneErrorView) {
        this.savePhoneErrorView = savePhoneErrorView;
    }

    public void setsavePhoneSuccessView(String savePhoneSuccessView) {
        this.savePhoneSuccessView = savePhoneSuccessView;
    }

    public void setviewPhoneErrorView(String viewPhoneErrorView) {
        this.viewPhoneErrorView = viewPhoneErrorView;
    }

    public void setviewPhoneSuccessView(String viewPhoneSuccessView) {
        this.viewPhoneSuccessView = viewPhoneSuccessView;
    }

    @RequestMapping(value="viewPhone", method =  {
            RequestMethod.GET, RequestMethod.POST}
    )
    public String viewPhone(@RequestParam(required = false)
            Long customerPhoneId, HttpServletRequest request, @ModelAttribute("phoneNameForm")
            PhoneNameForm phoneNameForm, BindingResult errors) {
        if (customerPhoneId == null) {
            return viewPhoneSuccessView;
        } else {
            Long currCustomerId = customerState.getCustomer(request).getId();
            CustomerPhone cPhone = customerPhoneService.readCustomerPhoneById(customerPhoneId);

            if (cPhone != null) {
                // TODO: verify this is the current customers phone
                //? - do we really need this since we read the phone with the currCustomerId?
                if (!cPhone.getCustomer().getId().equals(currCustomerId)) {
                    return viewPhoneErrorView;
                }

                phoneNameForm.setPhone(cPhone.getPhone());
                phoneNameForm.setPhoneName(cPhone.getPhoneName());
                request.setAttribute("customerPhoneId", cPhone.getId());
                request.setAttribute("phoneId", cPhone.getPhone().getId());

                return viewPhoneSuccessView;
            } else {
                return viewPhoneErrorView;
            }
        }
    }
}
