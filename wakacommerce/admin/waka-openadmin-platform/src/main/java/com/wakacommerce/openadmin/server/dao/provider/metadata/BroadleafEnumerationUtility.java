package com.wakacommerce.openadmin.server.dao.provider.metadata;

import org.springframework.stereotype.Component;

import com.wakacommerce.common.WakaEnumType;
import com.wakacommerce.common.util.Tuple;
import com.wakacommerce.openadmin.server.dao.DynamicEntityDao;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@Component("blBroadleafEnumerationUtility")
public class BroadleafEnumerationUtility {

    @SuppressWarnings("rawtypes")
    public List<Tuple<String, String>> getEnumerationValues(String broadleafEnumerationClass, DynamicEntityDao dynamicEntityDao) {
        try {
            Map<String, String> enumVals;
            Class<?> broadleafEnumeration = Class.forName(broadleafEnumerationClass);  
    
            Method typeMethod = broadleafEnumeration.getMethod("getType");
            Method friendlyTypeMethod = broadleafEnumeration.getMethod("getFriendlyType");
            Field types = dynamicEntityDao.getFieldManager().getField(broadleafEnumeration, "TYPES");
            
            if (Comparable.class.isAssignableFrom(broadleafEnumeration)) {
                enumVals = new LinkedHashMap<String, String>();
                Set<WakaEnumType> blcEnumSet = new TreeSet<WakaEnumType>();
                if (types != null) {
                    Map typesMap = (Map) types.get(null);
                    for (Object value : typesMap.values()) {
                        blcEnumSet.add((WakaEnumType) value);
                    }
    
                    for (Object value : typesMap.values()) {
                        enumVals.put((String) friendlyTypeMethod.invoke(value), (String) typeMethod.invoke(value));
                    }
                }
            } else {
                enumVals = new TreeMap<String, String>();
                if (types != null) {
                    Map typesMap = (Map) types.get(null);
                    for (Object value : typesMap.values()) {
                        enumVals.put((String) friendlyTypeMethod.invoke(value), (String) typeMethod.invoke(value));
                    }
                } else {
                    Field[] fields = dynamicEntityDao.getAllFields(broadleafEnumeration);
                    for (Field field : fields) {
                        boolean isStatic = Modifier.isStatic(field.getModifiers());
                        if (isStatic && field.getType().isAssignableFrom(broadleafEnumeration)){
                            enumVals.put((String) friendlyTypeMethod.invoke(field.get(null)), (String) typeMethod.invoke(field.get(null)));
                        }
                    }
                }
            }
            
            List<Tuple<String, String>> enumerationValues = new ArrayList<Tuple<String, String>>();
            for (String key : enumVals.keySet()) {
                Tuple<String, String> t = new Tuple<String, String>(enumVals.get(key), key);
                enumerationValues.add(t);
            }
            return enumerationValues;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}