
package com.wakacommerce.core.order.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.wakacommerce.common.copy.CreateResponse;
import com.wakacommerce.common.copy.MultiTenantCopyContext;
import com.wakacommerce.common.presentation.AdminPresentation;
import com.wakacommerce.common.presentation.AdminPresentationClass;
import com.wakacommerce.common.presentation.client.VisibilityEnum;

import javax.persistence.*;
import java.lang.reflect.Method;

/**
 * Arbitrary attributes to add to an order-item.
 *
 * @see com.wakacommerce.core.order.domain.OrderItemAttribute
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="BLC_ORDER_ITEM_ATTRIBUTE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blOrderElements")
@AdminPresentationClass(friendlyName = "OrderItemAttributeImpl_baseProductAttribute")
public class OrderItemAttributeImpl implements OrderItemAttribute {

    public static final Log LOG = LogFactory.getLog(OrderItemAttributeImpl.class);
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(generator= "OrderItemAttributeId")
    @GenericGenerator(
        name="OrderItemAttributeId",
        strategy="com.wakacommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="segment_value", value="OrderItemAttributeImpl"),
            @Parameter(name="entity_name", value="com.wakacommerce.core.catalog.domain.OrderItemAttributeImpl")
        }
    )
    @Column(name = "ORDER_ITEM_ATTRIBUTE_ID")
    protected Long id;
    
    @Column(name = "NAME", nullable=false)
    @AdminPresentation(visibility = VisibilityEnum.HIDDEN_ALL)
    protected String name;

    @Column(name = "VALUE", nullable=false)
    @AdminPresentation(friendlyName = "OrderItemAttributeImpl_Attribute_Value", order=2, group = "OrderItemAttributeImpl_Description", prominent=true)
    protected String value;
    
    @ManyToOne(targetEntity = OrderItemImpl.class, optional=false)
    @JoinColumn(name = "ORDER_ITEM_ID")
    protected OrderItem orderItem;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
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
    public String toString() {
        return value;
    }

    @Override
    public OrderItem getOrderItem() {
        return orderItem;
    }

    @Override
    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public void checkCloneable(OrderItemAttribute itemAttribute) throws CloneNotSupportedException, SecurityException, NoSuchMethodException {
        Method cloneMethod = itemAttribute.getClass().getMethod("clone", new Class[]{});
        if (cloneMethod.getDeclaringClass().getName().startsWith("com.wakacommerce") && !itemAttribute.getClass().getName().startsWith("com.wakacommerce")) {
            //subclass is not implementing the clone method
            throw new CloneNotSupportedException("Custom extensions and implementations should implement clone in order to guarantee split and merge operations are performed accurately");
        }
    }

    @Override
    public OrderItemAttribute clone() {
        //instantiate from the fully qualified name via reflection
        OrderItemAttribute itemAttribute;
        try {
            itemAttribute = (OrderItemAttribute) Class.forName(this.getClass().getName()).newInstance();
            try {
                checkCloneable(itemAttribute);
            } catch (CloneNotSupportedException e) {
                LOG.warn("Clone implementation missing in inheritance hierarchy outside of Broadleaf: " + itemAttribute.getClass().getName(), e);
            }            
            itemAttribute.setName(name);
            itemAttribute.setOrderItem(orderItem);
            itemAttribute.setValue(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return itemAttribute;
    }


    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!getClass().isAssignableFrom(obj.getClass()))
            return false;
        
        if (value == null) {
            return false;
        }
        
        return value.equals(((OrderItemAttribute) obj).getValue());
    }

    @Override
    public <G extends OrderItemAttribute> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        OrderItemAttribute cloned = createResponse.getClone();
        cloned.setName(name);
        cloned.setValue(value);
        // dont clone
        cloned.setOrderItem(orderItem);
        return  createResponse;
    }
}
