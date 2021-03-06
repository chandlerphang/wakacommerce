package com.wakacommerce.cms.structure.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.wakacommerce.cms.field.domain.FieldGroup;
import com.wakacommerce.cms.field.domain.FieldGroupImpl;
import com.wakacommerce.common.copy.CreateResponse;
import com.wakacommerce.common.copy.MultiTenantCopyContext;
import com.wakacommerce.common.presentation.AdminPresentation;
import com.wakacommerce.common.presentation.AdminPresentationClass;
import com.wakacommerce.common.presentation.PopulateToOneFieldsEnum;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SC_FLD_TMPLT")
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
@AdminPresentationClass(populateToOneFields = PopulateToOneFieldsEnum.TRUE, friendlyName = "StructuredContentFieldTemplateImpl_baseStructuredContentFieldTemplate")
public class StructuredContentFieldTemplateImpl implements StructuredContentFieldTemplate {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "StructuredContentFieldTemplateId")
    @GenericGenerator(
        name="StructuredContentFieldTemplateId",
        strategy="com.wakacommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="segment_value", value="StructuredContentFieldTemplateImpl"),
            @Parameter(name="entity_name", value="com.wakacommerce.cms.structure.domain.StructuredContentFieldTemplateImpl")
        }
    )
    @Column(name = "SC_FLD_TMPLT_ID")
    protected Long id;

    @AdminPresentation(
		friendlyName = "StructuredContentFieldTemplateImpl_Field_Template_Name", 
		order = 1, 
		gridOrder = 2, 
		group = "StructuredContentFieldTemplateImpl_Details", 
		prominent = true
    )
    @Column (name = "NAME")
    protected String name;

    @ManyToMany(targetEntity = FieldGroupImpl.class, cascade = {CascadeType.ALL})
    @JoinTable(name = "BLC_SC_FLDGRP_XREF", 
    	joinColumns = @JoinColumn(name = "SC_FLD_TMPLT_ID", referencedColumnName = "SC_FLD_TMPLT_ID"), 
    	inverseJoinColumns = @JoinColumn(name = "FLD_GROUP_ID", referencedColumnName = "FLD_GROUP_ID"))
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCMSElements")
    @OrderColumn(name = "GROUP_ORDER")
    @BatchSize(size = 20)
    protected List<FieldGroup> fieldGroups = new ArrayList<FieldGroup>();

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
    public List<FieldGroup> getFieldGroups() {
        return fieldGroups;
    }

    @Override
    public void setFieldGroups(List<FieldGroup> fieldGroups) {
        this.fieldGroups = fieldGroups;
    }

    @Override
    public <G extends StructuredContentFieldTemplate> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        StructuredContentFieldTemplate cloned = createResponse.getClone();
        cloned.setName(name);
        for(FieldGroup entry : fieldGroups){
            CreateResponse<FieldGroup> clonedGroupRsp = entry.createOrRetrieveCopyInstance(context);
            FieldGroup clonedGroup = clonedGroupRsp.getClone();
            cloned.getFieldGroups().add(clonedGroup);
        }

        return createResponse;
    }
}

