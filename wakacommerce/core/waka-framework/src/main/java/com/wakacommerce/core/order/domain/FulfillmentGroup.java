
package com.wakacommerce.core.order.domain;

import com.wakacommerce.common.money.Money;
import com.wakacommerce.core.offer.domain.CandidateFulfillmentGroupOffer;
import com.wakacommerce.core.offer.domain.FulfillmentGroupAdjustment;
import com.wakacommerce.core.order.service.type.FulfillmentGroupStatusType;
import com.wakacommerce.core.order.service.type.FulfillmentType;
import com.wakacommerce.profile.core.domain.Address;
import com.wakacommerce.profile.core.domain.Phone;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @ hui
 */
public interface FulfillmentGroup extends Serializable {

    public Long getId();

    public void setId(Long id);

    public Order getOrder();

    public void setOrder(Order order);
    
    public void setSequence(Integer sequence);

    public Integer getSequence();

    public FulfillmentOption getFulfillmentOption();

    public void setFulfillmentOption(FulfillmentOption fulfillmentOption);

    public Address getAddress();

    public void setAddress(Address address);

    @Deprecated
    public Phone getPhone();

    @Deprecated
    public void setPhone(Phone phone);

    public List<FulfillmentGroupItem> getFulfillmentGroupItems();

    public void setFulfillmentGroupItems(List<FulfillmentGroupItem> fulfillmentGroupItems);

    public void addFulfillmentGroupItem(FulfillmentGroupItem fulfillmentGroupItem);

    @Deprecated
    public String getMethod();

    @Deprecated
    public void setMethod(String fulfillmentMethod);

    public Money getRetailFulfillmentPrice();

    public void setRetailFulfillmentPrice(Money fulfillmentPrice);

    public Money getSaleFulfillmentPrice();

    public void setSaleFulfillmentPrice(Money fulfillmentPrice);

    public Money getFulfillmentPrice();

    public void setFulfillmentPrice(Money fulfillmentPrice);

    @Deprecated
    public Money getRetailShippingPrice();

    @Deprecated
    public void setRetailShippingPrice(Money retailShippingPrice);

    @Deprecated
    public Money getSaleShippingPrice();

    @Deprecated
    public void setSaleShippingPrice(Money saleShippingPrice);

    @Deprecated
    public Money getShippingPrice();

    @Deprecated
    public void setShippingPrice(Money shippingPrice);

    public String getReferenceNumber();

    public void setReferenceNumber(String referenceNumber);

    public FulfillmentType getType();

    void setType(FulfillmentType type);

    public List<CandidateFulfillmentGroupOffer> getCandidateFulfillmentGroupOffers();

    public void setCandidateFulfillmentGroupOffer(List<CandidateFulfillmentGroupOffer> candidateOffers);

    public void addCandidateFulfillmentGroupOffer(CandidateFulfillmentGroupOffer candidateOffer);

    public void removeAllCandidateOffers();

    public List<FulfillmentGroupAdjustment> getFulfillmentGroupAdjustments();

    public void setFulfillmentGroupAdjustments(List<FulfillmentGroupAdjustment> fulfillmentGroupAdjustments);

    public void removeAllAdjustments();

    public List<TaxDetail> getTaxes();

    public void setTaxes(List<TaxDetail> taxes);

    public Money getTotalTax();

    public void setTotalTax(Money totalTax);

    public Money getTotalItemTax();

    public void setTotalItemTax(Money totalItemTax);

    public Money getTotalFeeTax();

    public void setTotalFeeTax(Money totalFeeTax);

    public Money getTotalFulfillmentGroupTax();

    public void setTotalFulfillmentGroupTax(Money totalFulfillmentGroupTax);

    public String getDeliveryInstruction();

    public void setDeliveryInstruction(String deliveryInstruction);

    public PersonalMessage getPersonalMessage();

    public void setPersonalMessage(PersonalMessage personalMessage);

    public boolean isPrimary();

    public void setPrimary(boolean primary);

    public Money getMerchandiseTotal();

    public void setMerchandiseTotal(Money merchandiseTotal);

    public Money getTotal();

    public void setTotal(Money orderTotal);

    public FulfillmentGroupStatusType getStatus();

    public void setStatus(FulfillmentGroupStatusType status);
    
    public List<FulfillmentGroupFee> getFulfillmentGroupFees();

    public void setFulfillmentGroupFees(List<FulfillmentGroupFee> fulfillmentGroupFees);

    public void addFulfillmentGroupFee(FulfillmentGroupFee fulfillmentGroupFee);

    public void removeAllFulfillmentGroupFees();

    public Boolean isShippingPriceTaxable();

    public void setIsShippingPriceTaxable(Boolean isShippingPriceTaxable);

    @Deprecated
    public String getService();

    @Deprecated
    public void setService(String service);
    
    public List<DiscreteOrderItem> getDiscreteOrderItems();
    
    public Money getFulfillmentGroupAdjustmentsValue();

    public Boolean getShippingOverride();

    public void setShippingOverride(Boolean shippingOverride);
    
}
