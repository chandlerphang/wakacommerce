
package com.wakacommerce.openadmin.web.rulebuilder.grouping;

import java.util.ArrayList;
import java.util.List;

import com.wakacommerce.openadmin.web.rulebuilder.BLCOperator;

/**
 *  
 *  
 */
public class Group {

    private List<String> phrases = new ArrayList<String>();
    private List<Group> subGroups = new ArrayList<Group>();
    private BLCOperator operatorType;
    private Boolean isTopGroup = false;

    public List<String> getPhrases() {
        return phrases;
    }

    public BLCOperator getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(BLCOperator operatorType) {
        this.operatorType = operatorType;
    }

    public List<Group> getSubGroups() {
        return subGroups;
    }

    public Boolean getIsTopGroup() {
        return isTopGroup;
    }

    public void setIsTopGroup(Boolean isTopGroup) {
        this.isTopGroup = isTopGroup;
    }
}
