package com.wakacommerce.cms.structure.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;

import com.wakacommerce.common.admin.domain.AdminMainEntity;
import com.wakacommerce.common.copy.CreateResponse;
import com.wakacommerce.common.copy.MultiTenantCopyContext;
import com.wakacommerce.common.extensibility.jpa.clone.ClonePolicyArchive;
import com.wakacommerce.common.extensibility.jpa.clone.ClonePolicyMapOverride;
import com.wakacommerce.common.extensibility.jpa.clone.IgnoreEnterpriseBehavior;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransform;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransformMember;
import com.wakacommerce.common.extensibility.jpa.copy.DirectCopyTransformTypes;
import com.wakacommerce.common.extensibility.jpa.copy.ProfileEntity;
import com.wakacommerce.common.presentation.AdminPresentation;
import com.wakacommerce.common.presentation.AdminPresentationClass;
import com.wakacommerce.common.presentation.AdminPresentationToOneLookup;
import com.wakacommerce.common.presentation.PopulateToOneFieldsEnum;
import com.wakacommerce.common.presentation.RequiredOverride;
import com.wakacommerce.common.presentation.override.AdminPresentationMergeEntry;
import com.wakacommerce.common.presentation.override.AdminPresentationMergeOverride;
import com.wakacommerce.common.presentation.override.AdminPresentationMergeOverrides;
import com.wakacommerce.common.presentation.override.PropertyType;
import com.wakacommerce.openadmin.audit.AdminAuditable;
import com.wakacommerce.openadmin.audit.AdminAuditableListener;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SC")
@EntityListeners(value = { AdminAuditableListener.class })
@AdminPresentationMergeOverrides(
    {
        @AdminPresentationMergeOverride(name = "auditable.createdBy.id", mergeEntries = {
            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.READONLY, booleanOverrideValue = true),
            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.VISIBILITY, overrideValue = "HIDDEN_ALL")
        }),
        @AdminPresentationMergeOverride(name = "auditable.updatedBy.id", mergeEntries = {
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.READONLY, booleanOverrideValue = true),
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.VISIBILITY, overrideValue = "HIDDEN_ALL")
        }),
        @AdminPresentationMergeOverride(name = "auditable.createdBy.name", mergeEntries = {
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.READONLY, booleanOverrideValue = true),
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.VISIBILITY, overrideValue = "HIDDEN_ALL")
	    }),
        
        @AdminPresentationMergeOverride(name = "auditable.updatedBy.name", mergeEntries = {
            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.READONLY, booleanOverrideValue = true),
            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.VISIBILITY, overrideValue = "HIDDEN_ALL")
        }),
        @AdminPresentationMergeOverride(name = "auditable.dateCreated", mergeEntries = {
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.READONLY, booleanOverrideValue = true),
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.VISIBILITY, overrideValue = "HIDDEN_ALL")
        }),
        @AdminPresentationMergeOverride(name = "auditable.dateUpdated", mergeEntries = {
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.READONLY, booleanOverrideValue = true),
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.VISIBILITY, overrideValue = "HIDDEN_ALL")
		}),
        @AdminPresentationMergeOverride(name = "structuredContentType.name", mergeEntries = {
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.READONLY, booleanOverrideValue = true),
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.VISIBILITY, overrideValue = "HIDDEN_ALL")
        }),
        @AdminPresentationMergeOverride(name = "structuredContentType.structuredContentFieldTemplate.name", mergeEntries = {
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.READONLY, booleanOverrideValue = true),
	            @AdminPresentationMergeEntry(propertyType = PropertyType.AdminPresentation.VISIBILITY, overrideValue = "HIDDEN_ALL")
		})
    }
)
@AdminPresentationClass(populateToOneFields = PopulateToOneFieldsEnum.TRUE, friendlyName = "结构化内容")
@DirectCopyTransform({
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.SANDBOX, skipOverlaps=true),
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.MULTITENANT_SITE)
})
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
public class StructuredContentImpl implements StructuredContent, AdminMainEntity, ProfileEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "StructuredContentId")
    @GenericGenerator(
        name="StructuredContentId",
        strategy="com.wakacommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="segment_value", value="StructuredContentImpl"),
            @Parameter(name="entity_name", value="com.wakacommerce.cms.structure.domain.StructuredContentImpl")
        }
    )
    @Column(name = "SC_ID")
    protected Long id;

    @Embedded
    @AdminPresentation(excluded = true)
    protected AdminAuditable auditable = new AdminAuditable();

    @AdminPresentation(friendlyName = "内容名称", order = 1, 
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        prominent = true, gridOrder = 1)
    @Column(name = "CONTENT_NAME", nullable = false)
    @Index(name="CONTENT_NAME_INDEX", columnNames={"CONTENT_NAME", "ARCHIVED_FLAG", "SC_TYPE_ID"})
    protected String contentName;

    @Column(name = "PRIORITY", nullable = false)
    @AdminPresentation(friendlyName = "优先级", order = 3,
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description)
    @Index(name="CONTENT_PRIORITY_INDEX", columnNames={"PRIORITY"})
    protected Integer priority;

    @ManyToMany(targetEntity = StructuredContentRuleImpl.class, cascade = {CascadeType.ALL})
    @JoinTable(name = "BLC_SC_RULE_MAP", inverseJoinColumns = @JoinColumn(name = "SC_RULE_ID", referencedColumnName = "SC_RULE_ID"))
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL})
    @MapKeyColumn(name = "MAP_KEY", nullable = false)
    @Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
    @IgnoreEnterpriseBehavior
    Map<String, StructuredContentRule> structuredContentMatchRules = new HashMap<String, StructuredContentRule>();

    @OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, targetEntity = StructuredContentItemCriteriaImpl.class, cascade={CascadeType.ALL})
    @JoinTable(name = "BLC_QUAL_CRIT_SC_XREF", joinColumns = @JoinColumn(name = "SC_ID"), inverseJoinColumns = @JoinColumn(name = "SC_ITEM_CRITERIA_ID"))
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL})
    @Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
    @IgnoreEnterpriseBehavior
    protected Set<StructuredContentItemCriteria> qualifyingItemCriteria = new HashSet<StructuredContentItemCriteria>();

    @ManyToOne(targetEntity = StructuredContentTypeImpl.class)
    @JoinColumn(name="SC_TYPE_ID")
    @AdminPresentation(friendlyName = "内容类型", order = 2, prominent = true,
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        requiredOverride = RequiredOverride.REQUIRED)
    @AdminPresentationToOneLookup(lookupDisplayProperty = "name", forcePopulateChildProperties = true)
    protected StructuredContentType structuredContentType;

    @OneToMany(mappedBy = "structuredContent", targetEntity = StructuredContentFieldXrefImpl.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "key")
    @BatchSize(size = 20)
    @ClonePolicyMapOverride
    @ClonePolicyArchive
    protected Map<String, StructuredContentFieldXref> structuredContentFields = new HashMap<String, StructuredContentFieldXref>();

    @Transient
    protected Map<String, StructuredContentField> legacyStructuredContentFields = new HashMap<String, StructuredContentField>();

    @AdminPresentation(friendlyName = "下线", order = 4, 
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description)
    @Column(name = "OFFLINE_FLAG")
    @Index(name="SC_OFFLN_FLG_INDX", columnNames={"OFFLINE_FLAG"})
    protected Boolean offlineFlag = false;
    
    @Transient
    protected Map<String, String> fieldValuesMap = null;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getContentName() {
        return contentName;
    }

    @Override
    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    @Override
    public StructuredContentType getStructuredContentType() {
        return structuredContentType;
    }

    @Override
    public void setStructuredContentType(StructuredContentType structuredContentType) {
        this.structuredContentType = structuredContentType;
    }

    @Override
    public Map<String, StructuredContentFieldXref> getStructuredContentFieldXrefs() {
        return structuredContentFields;
    }

    @Override
    public void setStructuredContentFieldXrefs(@Nullable Map<String, StructuredContentFieldXref> structuredContentFields) {
        this.structuredContentFields = structuredContentFields;
    }
    
    @Override
    public String getFieldValue(String fieldName) {
        if (structuredContentFields.containsKey(fieldName)) {
            return getStructuredContentFieldXrefs().get(fieldName).getStructuredContentField().getValue();
        }
        return null;
    }
    
    @Override
    public void setFieldValues(Map<String, String> fieldValuesMap) {
        this.fieldValuesMap = fieldValuesMap;
    }

    @Override
    public Map<String, String> getFieldValues() {
        if (fieldValuesMap == null) {
            fieldValuesMap = new HashMap<String, String>();
            for (Entry<String, StructuredContentFieldXref> entry : getStructuredContentFieldXrefs().entrySet()) {
                fieldValuesMap.put(entry.getKey(), entry.getValue().getStructuredContentField().getValue());
            }
        }
        return fieldValuesMap;
    }

    @Override
    public Boolean getOfflineFlag() {
        if (offlineFlag == null) {
            return Boolean.FALSE;
        } else {
            return offlineFlag;
        }
    }

    @Override
    public void setOfflineFlag(Boolean offlineFlag) {
        this.offlineFlag = offlineFlag;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public AdminAuditable getAuditable() {
        return auditable;
    }

    @Override
    public void setAuditable(AdminAuditable auditable) {
        this.auditable = auditable;
    }

    @Override
    public Map<String, StructuredContentRule> getStructuredContentMatchRules() {
        return structuredContentMatchRules;
    }

    @Override
    public void setStructuredContentMatchRules(Map<String, StructuredContentRule> structuredContentMatchRules) {
        this.structuredContentMatchRules = structuredContentMatchRules;
    }

    @Override
    public Set<StructuredContentItemCriteria> getQualifyingItemCriteria() {
        return qualifyingItemCriteria;
    }

    @Override
    public void setQualifyingItemCriteria(Set<StructuredContentItemCriteria> qualifyingItemCriteria) {
        this.qualifyingItemCriteria = qualifyingItemCriteria;
    }
    
    @Override
    public String getMainEntityName() {
        return getContentName();
    }

    @Override
    public <G extends StructuredContent> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        StructuredContent cloned = createResponse.getClone();
        cloned.setContentName(contentName);
        cloned.setOfflineFlag(offlineFlag);
        cloned.setPriority(priority);
        if (structuredContentType != null) {
            CreateResponse<StructuredContentType> clonedType = structuredContentType.createOrRetrieveCopyInstance(context);
            cloned.setStructuredContentType(clonedType.getClone());
        }
        for(StructuredContentItemCriteria itemCriteria : qualifyingItemCriteria){
            CreateResponse<StructuredContentItemCriteria> clonedItem = itemCriteria.createOrRetrieveCopyInstance(context);
            StructuredContentItemCriteria clonedCritera = clonedItem.getClone();
            cloned.getQualifyingItemCriteria().add(clonedCritera);
        }
        for(Entry<String, StructuredContentRule> entry : structuredContentMatchRules.entrySet()){
            CreateResponse<StructuredContentRule> clonedItem = entry.getValue().createOrRetrieveCopyInstance(context);
            StructuredContentRule clonedRule = clonedItem.getClone();
            cloned.getStructuredContentMatchRules().put(entry.getKey(),clonedRule);

        }
        for(Entry<String, StructuredContentFieldXref> entry : structuredContentFields.entrySet() ){
            CreateResponse<StructuredContentFieldXref> clonedItem = entry.getValue().createOrRetrieveCopyInstance(context);
            StructuredContentFieldXref clonedContentFieldXref = clonedItem.getClone();
            cloned.getStructuredContentFieldXrefs().put(entry.getKey(),clonedContentFieldXref);
        }

        return createResponse;
    }

    public static class Presentation {
        public static class Tab {
            public static class Name {
                public static final String Rules = "StructuredContentImpl_Rules_Tab";
            }
            
            public static class Order {
                public static final int Rules = 1000;
            }
        }
            
        public static class Group {
            public static class Name {
                public static final String Description = "描述";
                public static final String Internal = "内部";
                public static final String Rules = "规则";
            }
            
            public static class Order {
                public static final int Description = 1000;
                public static final int Internal = 2000;
                public static final int Rules = 1000;
            }
        }
    }

}
