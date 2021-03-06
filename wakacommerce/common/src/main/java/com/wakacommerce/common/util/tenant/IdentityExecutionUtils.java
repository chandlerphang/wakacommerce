
package com.wakacommerce.common.util.tenant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ejb.HibernateEntityManager;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.wakacommerce.common.site.domain.Catalog;
import com.wakacommerce.common.site.domain.Site;
import com.wakacommerce.common.util.TransactionUtils;
import com.wakacommerce.common.web.WakaRequestContext;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * The utility methods in this class provide a way to ignore the currently configured site/catalog contexts and instead
 * explicitly run operations in the specified context.
 * 
 * 
 */
public class IdentityExecutionUtils {

    private static final Log LOG = LogFactory.getLog(IdentityExecutionUtils.class);

    public static <T, G extends Throwable> T runOperationByIdentifier(IdentityOperation<T, G> operation, Site site, Site profile, Catalog catalog,
                                                              PlatformTransactionManager transactionManager) throws G {
        IdentityUtilContext context = new IdentityUtilContext();
        context.setIdentifier(site);
        IdentityUtilContext.setUtilContext(context);

        WakaRequestContext brc = WakaRequestContext.getWakaRequestContext();
        Site previousSite = brc.getNonPersistentSite();
        Catalog previousCatalog = brc.getCurrentCatalog();
        Site previousProfile = brc.getCurrentProfile();
        
        boolean isNew = initRequestContext(site, profile, catalog);

        activateSession();
        
        TransactionContainer container = null;
        if (transactionManager != null) {
            container = establishTransaction(transactionManager);
        }
        
        boolean isError = false;
        try {
            return operation.execute();
        } catch (RuntimeException e) {
            isError = true;
            throw e;
        } finally {
            if (container != null) {
                finalizeTransaction(transactionManager, container, isError);
            }
            IdentityUtilContext.setUtilContext(null);
            if (isNew) {
                WakaRequestContext.setWakaRequestContext(null);
            }
            WakaRequestContext.getWakaRequestContext().setNonPersistentSite(previousSite);
            WakaRequestContext.getWakaRequestContext().setCurrentCatalog(previousCatalog);
            WakaRequestContext.getWakaRequestContext().setCurrentProfile(previousProfile);
        }
    }

    public static <T, G extends Throwable> T runOperationByIdentifier(IdentityOperation<T, G> operation, Site site, Catalog catalog) throws G {
        return runOperationByIdentifier(operation, site, null, catalog, null);
    }

    public static <T, G extends Throwable> T runOperationByIdentifier(IdentityOperation<T, G> operation, Site site, Site profile, Catalog catalog) throws G {
        return runOperationByIdentifier(operation, site, profile, catalog, null);
    }

    public static <T, G extends Throwable> T runOperationByIdentifier(IdentityOperation<T, G> operation, Site site) throws G {
        return runOperationByIdentifier(operation, site, null, null, null);
    }

    public static <T, G extends Throwable> T runOperationByIdentifier(IdentityOperation<T, G> operation, Site site, Site profile) throws G {
        return runOperationByIdentifier(operation, site, profile, null);
    }

    public static <T, G extends Throwable> T runOperationAndIgnoreIdentifier(IdentityOperation<T, G> operation) throws G {
        return runOperationAndIgnoreIdentifier(operation, null);
    }
    
    public static <T, G extends Throwable> T runOperationAndIgnoreIdentifier(IdentityOperation<T, G> operation, 
            PlatformTransactionManager transactionManager) throws G {
        WakaRequestContext brc = WakaRequestContext.getWakaRequestContext();
        Site previousSite = brc.getNonPersistentSite();
        Catalog previousCatalog = brc.getCurrentCatalog();
        Site previousProfile = brc.getCurrentProfile();
    
        boolean isNew = initRequestContext(null, null, null);
        boolean isIgnoringSite = WakaRequestContext.getWakaRequestContext().getIgnoreSite();
        WakaRequestContext.getWakaRequestContext().setIgnoreSite(true);

        activateSession();
        
        TransactionContainer container = null;
        if (transactionManager != null) {
            container = establishTransaction(transactionManager);
        }
        boolean isError = false;
        try {
            return operation.execute();
        } catch (RuntimeException e) {
            isError = true;
            throw e;
        } finally {
            if (container != null) {
                finalizeTransaction(transactionManager, container, isError);
            }
            
            if (isNew) {
                WakaRequestContext.setWakaRequestContext(null);
            }
            WakaRequestContext.getWakaRequestContext().setIgnoreSite(isIgnoringSite);
            WakaRequestContext.getWakaRequestContext().setNonPersistentSite(previousSite);
            WakaRequestContext.getWakaRequestContext().setCurrentCatalog(previousCatalog);
            WakaRequestContext.getWakaRequestContext().setCurrentProfile(previousProfile);
        }
    }

    private static boolean initRequestContext(Site site, Site profile, Catalog catalog) {
        boolean isNew = false;
        WakaRequestContext requestContext = WakaRequestContext.getWakaRequestContext();

        if (requestContext == null) {
            requestContext = new WakaRequestContext();
            WakaRequestContext.setWakaRequestContext(requestContext);
            isNew = true;
        }

        requestContext.setNonPersistentSite(site);
        requestContext.setCurrentCatalog(catalog);
        requestContext.setCurrentProfile(profile);
        
        if (site != null) {
            requestContext.setIgnoreSite(false);
        }

        return isNew;
    }

    private static void activateSession() {
        Map<Object, Object> resourceMap = TransactionSynchronizationManager.getResourceMap();
        for (Map.Entry<Object, Object> entry : resourceMap.entrySet()) {
            if (entry.getKey() instanceof EntityManagerFactory && entry.getValue() instanceof EntityManagerHolder) {
                ((HibernateEntityManager) ((EntityManagerHolder) entry.getValue()).getEntityManager()).getSession();
            }
        }
    }

    private static void finalizeTransaction(PlatformTransactionManager transactionManager, TransactionContainer
            container, boolean error) {
        TransactionUtils.finalizeTransaction(container.status, transactionManager, error);
        for (Map.Entry<Object, Object> entry : container.usedResources.entrySet()) {
            if (!TransactionSynchronizationManager.hasResource(entry.getKey())) {
                TransactionSynchronizationManager.bindResource(entry.getKey(), entry.getValue());
            }
        }
    }

    private static TransactionContainer establishTransaction(PlatformTransactionManager transactionManager) {
        Map<Object, Object> usedResources = new HashMap<Object, Object>();
        Map<Object, Object> resources = TransactionSynchronizationManager.getResourceMap();
        for (Map.Entry<Object, Object> entry : resources.entrySet()) {
            if ((entry.getKey() instanceof EntityManagerFactory  || entry.getKey() instanceof DataSource) &&
                    TransactionSynchronizationManager.hasResource(entry.getKey())) {
                usedResources.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<Object, Object> entry : usedResources.entrySet()) {
            TransactionSynchronizationManager.unbindResource(entry.getKey());
        }

        TransactionStatus status;
        try {
            status = TransactionUtils.createTransaction(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                    transactionManager, false);
        } catch (RuntimeException e) {
            throw e;
        }
        return new TransactionContainer(status, usedResources);
    }

    private static class TransactionContainer {
        TransactionStatus status;
        Map<Object, Object> usedResources;

        private TransactionContainer(TransactionStatus status, Map<Object, Object> usedResources) {
            this.status = status;
            this.usedResources = usedResources;
        }

        public TransactionStatus getStatus() {
            return status;
        }

        public void setStatus(TransactionStatus status) {
            this.status = status;
        }

        public Map<Object, Object> getUsedResources() {
            return usedResources;
        }

        public void setUsedResources(Map<Object, Object> usedResources) {
            this.usedResources = usedResources;
        }
    }
}
