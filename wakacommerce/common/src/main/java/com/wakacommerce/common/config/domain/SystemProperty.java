package com.wakacommerce.common.config.domain;

import java.io.Serializable;

import com.wakacommerce.common.config.service.type.SystemPropertyFieldType;
import com.wakacommerce.common.copy.MultiTenantCloneable;

/**
 * This interface represents a System Property (name/value pair) stored in the database.  
 */
public interface SystemProperty extends Serializable, MultiTenantCloneable<SystemProperty> {

    /**
     * Unique id of the DB record
     * @return
     */
    public Long getId();

    /**
     * Sets the id of the DB record
     * @param id
     */
    public void setId(Long id);

    /**
     * The name of the property as it exists in property files (for example googleAnalytics.webPropertyId)
     * @return
     */
    public String getName();

    /**
     * Sets the property name.  
     * @param name
     */
    public void setName(String name);

    /**
     * Returns the property value.  
     * @param name
     */
    public String getValue();

    /**
     * Sets the property value.  
     * @param name
     */
    public void setValue(String value);

    /**
     * Returns the property field type.   If not set, returns STRING
     * @return
     */
    public SystemPropertyFieldType getPropertyType();

    /**
     * Sets the property field type.
     * @param type
     */
    public void setPropertyType(SystemPropertyFieldType type);

    /**
     * @return the friendly name of this property
     */
    public String getFriendlyName();

    /**
     * Sets the friendly name of this property
     * 
     * @param friendlyName
     */
    public void setFriendlyName(String friendlyName);

    /**
     * @return the griendly group name of this property
     */
    public String getFriendlyGroup();

    /**
     * Sets the friendly group name of this property
     * 
     * @param friendlyGroup
     */
    public void setFriendlyGroup(String friendlyGroup);

    /**
     * @return the friendly tab of this property
     */
    public String getFriendlyTab();

    /**
     * Sets the friendly tab of this property
     * 
     * @param friendlyTab
     */
    public void setFriendlyTab(String friendlyTab);

}
