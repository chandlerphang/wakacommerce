
package com.wakacommerce.common.util;

/**
 * 
 * @deprecated instead, use {@link BroadleafMergeResourceBundleMessageSource}
 */
@Deprecated
public class ResourceBundleExtensionPoint {

    private String[] basenameExtensions = new String[0];

    public String[] getBasenameExtensions() {
        return basenameExtensions;
    }

    public void setBasenameExtensions(String[] basenameExtensions) {
        this.basenameExtensions = basenameExtensions;
    }
}
