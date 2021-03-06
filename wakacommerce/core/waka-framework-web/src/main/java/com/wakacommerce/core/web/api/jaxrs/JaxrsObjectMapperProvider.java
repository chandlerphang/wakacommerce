  
package com.wakacommerce.core.web.api.jaxrs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule.Priority;
import com.wakacommerce.core.web.api.WrapperOverrideTypeModifier;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;


/**
 * <p>
 * Customized provider for a Jackson ObjectMapper that ensures singleelement objects are serialized in JSON array syntax
 * both on Serialization and Deserialization.
 * 
 * <p>
 * This also ensures that the {@link JaxbAnnotationModule} is registered with the {@link ObjectMapper}.
 * 
 *     
 * @deprecated along with the other JAXRS components, this is deprecated in favor of using Spring MVC for REST services
 */
@Provider
@Produces(value = { MediaType.APPLICATION_JSON })
@Consumes(value = { MediaType.APPLICATION_JSON })
@Component("blJaxrsObjectMapperProvider")
@Conditional(IsJaxrsAvailableCondition.class)
@Deprecated
public class JaxrsObjectMapperProvider implements ContextResolver<ObjectMapper>, InitializingBean {

    private static final Log LOG = LogFactory.getLog(JaxrsObjectMapperProvider.class);
    
    @Resource(name = "blWrapperOverrideTypeModifier")
    protected WrapperOverrideTypeModifier typeModifier;
    
    protected ObjectMapper mapper;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // serializing to singleelement arrays is enabled by default but just in case they change this in the future...
        mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false);
        
        // Register the JAXB annotation module 
        JaxbAnnotationModule jaxbModule = new JaxbAnnotationModule();
        // Make sure that JAXB is the primary serializer (technically the default behavior but let's be explicit)
        jaxbModule.setPriority(Priority.PRIMARY);
        mapper.registerModule(new JaxbAnnotationModule());
        
        mapper.setTypeFactory(TypeFactory.defaultInstance().withModifier(typeModifier));
    }
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

}