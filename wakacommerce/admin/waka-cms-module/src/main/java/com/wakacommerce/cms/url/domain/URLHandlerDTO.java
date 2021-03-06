package com.wakacommerce.cms.url.domain;

import com.wakacommerce.cms.url.type.URLRedirectType;
import com.wakacommerce.common.copy.CreateResponse;
import com.wakacommerce.common.copy.MultiTenantCopyContext;

public class URLHandlerDTO implements URLHandler {

    private static final long serialVersionUID = 1L;
    protected Long id = null;
    protected String incomingURL = "";
    protected String newURL;
    protected String urlRedirectType;

    public URLHandlerDTO(String newUrl, URLRedirectType redirectType) {
        setUrlRedirectType(redirectType);
        setNewURL(newUrl);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIncomingURL() {
        return incomingURL;
    }

    public void setIncomingURL(String incomingURL) {
        this.incomingURL = incomingURL;
    }

    public String getNewURL() {
        return newURL;
    }

    public void setNewURL(String newURL) {
        this.newURL = newURL;
    }

    @Override
    public URLRedirectType getUrlRedirectType() {
        return URLRedirectType.getInstance(urlRedirectType);
    }

    @Override
    public void setUrlRedirectType(URLRedirectType redirectType) {
        this.urlRedirectType = redirectType.getType();
    }

    @Override
    public <G extends URLHandler> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        URLHandler cloned = createResponse.getClone();
        cloned.setIncomingURL(incomingURL);
        cloned.setNewURL(newURL);
        cloned.setUrlRedirectType( URLRedirectType.getInstance(urlRedirectType));
        return  createResponse;
    }
}
