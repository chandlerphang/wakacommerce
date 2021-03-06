
package com.wakacommerce.core.offer.service.discount;

import com.wakacommerce.common.money.BankersRounding;
import com.wakacommerce.common.money.Money;
import com.wakacommerce.core.offer.domain.Offer;

/**
 * 
 *  
 *
 */
public class FulfillmentGroupOfferPotential {
    
    protected Offer offer;
    protected Money totalSavings = new Money(BankersRounding.zeroAmount());
    protected int priority;
    
    public Offer getOffer() {
        return offer;
    }
    
    public void setOffer(Offer offer) {
        this.offer = offer;
    }
    
    public Money getTotalSavings() {
        return totalSavings;
    }
    
    public void setTotalSavings(Money totalSavings) {
        this.totalSavings = totalSavings;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((offer == null) ? 0 : offer.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        FulfillmentGroupOfferPotential other = (FulfillmentGroupOfferPotential) obj;
        if (offer == null) {
            if (other.offer != null) {
                return false;
            }
        } else if (!offer.equals(other.offer)) {
            return false;
        }
        return true;
    }

}
