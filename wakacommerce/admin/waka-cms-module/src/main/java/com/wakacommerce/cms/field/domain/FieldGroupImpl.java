package com.wakacommerce.cms.field.domain;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.wakacommerce.common.copy.CreateResponse;
import com.wakacommerce.common.copy.MultiTenantCopyContext;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransform;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransformMember;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransformTypes;
import com.wakacommerce.common.extensibility.jpa.copy.ProfileEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_FLD_GROUP")
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
@DirectCopyTransform({
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.SANDBOX, skipOverlaps = true),
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.MULTITENANT_SITE)
})
public class FieldGroupImpl implements FieldGroup, ProfileEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "FieldGroupId")
    @GenericGenerator(
        name="FieldGroupId",
        strategy="com.wakacommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="segment_value", value="FieldGroupImpl"),
            @Parameter(name="entity_name", value="com.wakacommerce.cms.field.domain.FieldGroupImpl")
        }
    )
    @Column(name = "FLD_GROUP_ID")
    protected Long id;

    @Column (name = "NAME")
    protected String name;

    @Column (name = "INIT_COLLAPSED_FLAG")
    protected Boolean initCollapsedFlag = false;

    @OneToMany(mappedBy = "fieldGroup", targetEntity = FieldDefinitionImpl.class, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCMSElements")
    @OrderBy("fieldOrder")
    @BatchSize(size = 20)
    protected List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Boolean getInitCollapsedFlag() {
        return initCollapsedFlag;
    }

    @Override
    public void setInitCollapsedFlag(Boolean initCollapsedFlag) {
        this.initCollapsedFlag = initCollapsedFlag;
    }

    @Override
    public List<FieldDefinition> getFieldDefinitions() {
        return fieldDefinitions;
    }

    @Override
    public void setFieldDefinitions(List<FieldDefinition> fieldDefinitions) {
        this.fieldDefinitions = fieldDefinitions;
    }

    @Override
    public <G extends FieldGroup> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context)
            throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        FieldGroup cloned = createResponse.getClone();
        cloned.setInitCollapsedFlag(initCollapsedFlag);
        cloned.setName(name);
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            FieldDefinition clonedDef = fieldDefinition.createOrRetrieveCopyInstance(context).getClone();
            cloned.getFieldDefinitions().add(clonedDef);
        }
        return createResponse;
    }
}

