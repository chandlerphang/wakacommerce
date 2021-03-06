
package com.wakacommerce.core.pricing.domain;

import java.io.Serializable;
import java.math.BigDecimal;

@Deprecated
public interface ShippingRate extends Serializable {

    public Long  getId();
    public void setId(Long id);
    public String getFeeType();
    public void setFeeType(String feeType);
    public String getFeeSubType();
    public void setFeeSubType(String feeSubType);
    public Integer getFeeBand();
    public void setFeeBand(Integer feeBand);
    public BigDecimal getBandUnitQuantity();
    public void setBandUnitQuantity(BigDecimal bandUnitQuantity);
    public BigDecimal getBandResultQuantity();
    public void setBandResultQuantity(BigDecimal bandResultQuantity);
    public Integer getBandResultPercent();
    public void setBandResultPercent(Integer bandResultPersent);

}
