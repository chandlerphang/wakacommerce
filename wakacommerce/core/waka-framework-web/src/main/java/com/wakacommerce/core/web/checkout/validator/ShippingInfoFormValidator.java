
package com.wakacommerce.core.web.checkout.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.wakacommerce.common.web.form.BroadleafFormType;
import com.wakacommerce.common.web.validator.BroadleafCommonAddressValidator;
import com.wakacommerce.core.web.checkout.model.ShippingInfoForm;

@Component("blShippingInfoFormValidator")
public class ShippingInfoFormValidator extends BroadleafCommonAddressValidator implements Validator {

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz) {
        return clazz.equals(ShippingInfoForm.class);
    }

    public void validate(Object obj, Errors errors) {
        ShippingInfoForm shippingInfoForm = (ShippingInfoForm) obj;
        super.validate(BroadleafFormType.SHIPPING_FORM, shippingInfoForm.getAddress(), errors);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fulfillmentOptionId", "fulfillmentOptionId.required");
    }
}
