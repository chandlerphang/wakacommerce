  
package com.wakacommerce.cms.structure.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.wakacommerce.common.copy.CreateResponse;
import com.wakacommerce.common.copy.MultiTenantCopyContext;
import com.wakacommerce.common.extensibility.jpa.clone.ClonePolicy;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransform;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransformMember;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransformTypes;
import com.wakacommerce.common.extensibility.jpa.copy.ProfileEntity;
import com.wakacommerce.common.presentation.AdminPresentation;
import com.wakacommerce.common.presentation.client.VisibilityEnum;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SC_FLD_MAP")
@DirectCopyTransform({
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.SANDBOX, skipOverlaps = true),
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.MULTITENANT_SITE)
})
public class StructuredContentFieldXrefImpl implements StructuredContentFieldXref, Serializable, ProfileEntity {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "StructuredContentFieldId")
    @GenericGenerator(
            name = "StructuredContentFieldId",
            strategy = "com.wakacommerce.common.persistence.IdOverrideTableGenerator",
            parameters = {
                    @Parameter(name = "segment_value", value = "StructuredContentFieldXrefImpl"),
                    @Parameter(name = "entity_name", value = "com.wakacommerce.cms.structure.domain.StructuredContentFieldXrefImpl")
            })
    @Column(name = "BLC_SC_SC_FIELD_ID")
    protected Long id;

    @ManyToOne(targetEntity = StructuredContentImpl.class, optional = false, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "SC_ID")
    @AdminPresentation(excluded = true)
    protected StructuredContent structuredContent;

    @ManyToOne(targetEntity = StructuredContentFieldImpl.class, cascade = { CascadeType.ALL })
    @JoinColumn(name = "SC_FLD_ID")
    @ClonePolicy
    protected StructuredContentField structuredContentField;

    @Column(name = "MAP_KEY", nullable = false)
    @AdminPresentation(visibility = VisibilityEnum.HIDDEN_ALL)
    protected String key;

    public StructuredContentFieldXrefImpl() {
        //Default constructor for JPA...
    }

    public StructuredContentFieldXrefImpl(StructuredContent sc, StructuredContentField scField, String key) {
        this.structuredContent = sc;
        this.structuredContentField = scField;
        this.key = key;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setStructuredContent(StructuredContent sc) {
        this.structuredContent = sc;
    }

    @Override
    public StructuredContent getStructuredContent() {
        return structuredContent;
    }

    @Override
    public void setStrucuturedContentField(StructuredContentField scField) {
        this.structuredContentField = scField;
    }

    @Override
    public StructuredContentField getStructuredContentField() {
        return structuredContentField;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public <G extends StructuredContentFieldXref> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        StructuredContentFieldXref cloned = createResponse.getClone();
        cloned.setKey(key);
        if (structuredContent != null) {
            cloned.setStructuredContent(structuredContent.createOrRetrieveCopyInstance(context).getClone());
        }
        if (structuredContentField != null) {
            CreateResponse<StructuredContentField> clonedFieldRsp = structuredContentField
                    .createOrRetrieveCopyInstance(context);
            cloned.setStrucuturedContentField(clonedFieldRsp.getClone());
        }
        return createResponse;
    }
}
