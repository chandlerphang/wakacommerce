

package com.wakacommerce.common.sitemap.service;

import com.wakacommerce.common.sitemap.domain.SiteMapGeneratorConfiguration;


/**
 * Responsible for generating site map entries.   
 * 
 * Each SiteMapGenerator can generate 
 * 
 *bpolster
 *
 */
public interface SiteMapGenerator {
    
    /**
     * Returns true if this SiteMapGenerator is able to process the passed in siteMapGeneratorConfiguration.   
     * 
     * @param siteMapGeneratorConfiguration
     * @return
     */
    public boolean canHandleSiteMapConfiguration(SiteMapGeneratorConfiguration siteMapGeneratorConfiguration);
    
    /**
     * Typically, the generator will loop through and build a list of URLs to add to the sitemap by calling 
     * methods on the SiteMapBuilder.
     * 
     * @param siteMapGeneratorConfiguration
     * @param siteMapBuilder
     */
    public void addSiteMapEntries(SiteMapGeneratorConfiguration siteMapGeneratorConfiguration, SiteMapBuilder siteMapBuilder);

}