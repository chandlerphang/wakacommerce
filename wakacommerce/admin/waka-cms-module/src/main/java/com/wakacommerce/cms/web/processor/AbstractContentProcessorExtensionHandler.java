/*
 * #%L
 * BroadleafCommerce CMS Module
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.wakacommerce.cms.web.processor;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;

import com.wakacommerce.common.extension.AbstractExtensionHandler;
import com.wakacommerce.common.extension.ExtensionResultStatusType;
import com.wakacommerce.common.web.deeplink.DeepLink;

import java.util.List;

/**
 * Abstract implementation of {@link ContentProcessorExtensionHandler}
 * 
 *Andre Azzolini (apazzolini)
 */
public abstract class AbstractContentProcessorExtensionHandler extends AbstractExtensionHandler 
        implements ContentProcessorExtensionHandler {

    @Override
    public ExtensionResultStatusType addAdditionalFieldsToModel(Arguments arguments, Element element) {
        return ExtensionResultStatusType.NOT_HANDLED;
    }

    @Override
    public ExtensionResultStatusType addExtensionFieldDeepLink(List<DeepLink> links, Arguments arguments, Element element) {
        return ExtensionResultStatusType.NOT_HANDLED;
    }
    
    @Override
    public ExtensionResultStatusType postProcessDeepLinks(List<DeepLink> links) {
        return ExtensionResultStatusType.NOT_HANDLED;
    }

}