
package com.wakacommerce.core.offer.service.processor;

import java.util.List;

import com.wakacommerce.core.offer.domain.Offer;
import com.wakacommerce.core.offer.service.discount.FulfillmentGroupOfferPotential;
import com.wakacommerce.core.offer.service.discount.domain.PromotableCandidateFulfillmentGroupOffer;
import com.wakacommerce.core.offer.service.discount.domain.PromotableOrder;

/**
 * 
 *  
 *
 */
public interface FulfillmentGroupOfferProcessor extends OrderOfferProcessor {

    public void filterFulfillmentGroupLevelOffer(PromotableOrder order, List<PromotableCandidateFulfillmentGroupOffer> qualifiedFGOffers, Offer offer);

    public void calculateFulfillmentGroupTotal(PromotableOrder order);
    
    /**
     * Private method that takes a list of sorted CandidateOrderOffers and determines if each offer can be
     * applied based on the restrictions (stackable and/or combinable) on that offer.  OrderAdjustments
     * are create on the Order for each applied CandidateOrderOffer.  An offer with stackable equals false
     * cannot be applied to an Order that already contains an OrderAdjustment.  An offer with combinable
     * equals false cannot be applied to the Order if the Order already contains an OrderAdjustment.
     *
     * @param qualifiedFGOffers a sorted list of CandidateOrderOffer
     * @param order the Order to apply the CandidateOrderOffers
     * @return true if order offer applied; otherwise false
     */
    public boolean applyAllFulfillmentGroupOffers(List<PromotableCandidateFulfillmentGroupOffer> qualifiedFGOffers, PromotableOrder order);
    
    public List<FulfillmentGroupOfferPotential> removeTrailingNotCombinableFulfillmentGroupOffers(List<FulfillmentGroupOfferPotential> candidateOffers);
    
}
