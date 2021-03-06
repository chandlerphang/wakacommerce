

package com.wakacommerce.common.web.payment.expression;

import java.util.Map;

import com.wakacommerce.common.extension.ExtensionHandler;
import com.wakacommerce.common.extension.ExtensionResultStatusType;

/**
 *  
 */
public interface PaymentGatewayFieldExtensionHandler extends ExtensionHandler {

    public ExtensionResultStatusType mapFieldName(String fieldNameKey, Map<String, String> fieldNameMap);

}
