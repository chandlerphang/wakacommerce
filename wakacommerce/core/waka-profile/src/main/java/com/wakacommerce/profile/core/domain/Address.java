
package com.wakacommerce.profile.core.domain;

import java.io.Serializable;

import com.wakacommerce.common.copy.MultiTenantCloneable;
import com.wakacommerce.common.i18n.domain.ISOCountry;

public interface Address extends Serializable, MultiTenantCloneable<Address> {

    public void setId(Long id);

    public Long getId();

    public void setAddressLine1(String addressLine1);

    public String getAddressLine1();

    public void setAddressLine2(String addressLine2);

    public String getAddressLine2();

    public void setAddressLine3(String addressLine3);

    public String getAddressLine3();

    public void setCity(String city);

    public String getCity();

    @Deprecated
    public void setState(State state);

    @Deprecated
    public State getState();

    public String getIsoCountrySubdivision();

    public void setIsoCountrySubdivision(String isoCountrySubdivision);

    public String getStateProvinceRegion();

    public void setStateProvinceRegion(String stateProvinceRegion);

    public void setPostalCode(String postalCode);

    public String getPostalCode();

    public String getCounty();

    public void setCounty(String county);

    public String getZipFour();

    public void setZipFour(String zipFour);

    @Deprecated
    public void setCountry(Country country);

    @Deprecated
    public Country getCountry();

    public ISOCountry getIsoCountryAlpha2();

    public void setIsoCountryAlpha2(ISOCountry isoCountryAlpha2);

    public String getTokenizedAddress();

    public void setTokenizedAddress(String tAddress);

    public Boolean getStandardized();

    public void setStandardized(Boolean standardized);

    public String getCompanyName();

    public void setCompanyName(String companyName);

    public boolean isDefault();

    public void setDefault(boolean isDefault);

    public String getFirstName();

    public void setFirstName(String firstName);

    public String getLastName();

    public void setLastName(String lastName);

    public String getFullName();

    public void setFullName(String fullName);

    @Deprecated
    public String getPrimaryPhone();

    @Deprecated
    public void setPrimaryPhone(String primaryPhone);

    @Deprecated
    public String getSecondaryPhone();

    @Deprecated
    public void setSecondaryPhone(String secondaryPhone);

    @Deprecated
    public String getFax();

    @Deprecated
    public void setFax(String fax);

    public Phone getPhonePrimary();

    public void setPhonePrimary(Phone phonePrimary);

    public Phone getPhoneSecondary();

    public void setPhoneSecondary(Phone phoneSecondary);
    
    public Phone getPhoneFax();

    public void setPhoneFax(Phone phone);

    public String getEmailAddress();

    public void setEmailAddress(String emailAddress);

    public boolean isBusiness();

    public void setBusiness(boolean isBusiness);

    public boolean isStreet();

    public void setStreet(boolean isStreet);

    public boolean isMailing();

    public void setMailing(boolean isMailing);

    public String getVerificationLevel();

    public void setVerificationLevel(String verificationLevel);

    public boolean isActive();

    public void setActive(boolean isActive);
}
