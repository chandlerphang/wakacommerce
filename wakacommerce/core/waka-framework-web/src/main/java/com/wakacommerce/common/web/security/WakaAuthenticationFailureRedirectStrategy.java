package com.wakacommerce.common.web.security;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import com.wakacommerce.common.web.controller.WakaControllerUtility;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @ hui
 */
@Component("blAuthenticationFailureRedirectStrategy")
public class WakaAuthenticationFailureRedirectStrategy implements RedirectStrategy {
    
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        if (WakaControllerUtility.isAjaxRequest(request)) {
             url = updateUrlForAjax(url);
        }
        redirectStrategy.sendRedirect(request, response, url);
    }

    public String updateUrlForAjax(String url) {
        String blcAjax = WakaControllerUtility.WK_AJAX_PARAMETER;
        if (url != null && url.indexOf("?") > 0) {
            url = url + "&" + blcAjax + "=true";
        } else {
            url = url + "?" + blcAjax + "=true";
        }
        return url;
    }
    
    public RedirectStrategy getRedirectStrategy() {
        return redirectStrategy;
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }
}