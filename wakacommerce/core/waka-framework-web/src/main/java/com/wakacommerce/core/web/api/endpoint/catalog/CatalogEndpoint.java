package com.wakacommerce.core.web.api.endpoint.catalog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;

import com.wakacommerce.common.exception.ServiceException;
import com.wakacommerce.common.file.service.StaticAssetPathService;
import com.wakacommerce.common.media.domain.Media;
import com.wakacommerce.common.security.service.ExploitProtectionService;
import com.wakacommerce.core.catalog.domain.Category;
import com.wakacommerce.core.catalog.domain.CategoryMediaXref;
import com.wakacommerce.core.catalog.domain.CategoryProductXref;
import com.wakacommerce.core.catalog.domain.Product;
import com.wakacommerce.core.catalog.domain.RelatedProduct;
import com.wakacommerce.core.catalog.domain.Sku;
import com.wakacommerce.core.catalog.domain.SkuAttribute;
import com.wakacommerce.core.catalog.service.CatalogService;
import com.wakacommerce.core.inventory.service.InventoryService;
import com.wakacommerce.core.search.domain.SearchCriteria;
import com.wakacommerce.core.search.domain.SearchFacetDTO;
import com.wakacommerce.core.search.domain.SearchResult;
import com.wakacommerce.core.search.service.SearchService;
import com.wakacommerce.core.web.api.BroadleafWebServicesException;
import com.wakacommerce.core.web.api.endpoint.BaseEndpoint;
import com.wakacommerce.core.web.api.wrapper.CategoriesWrapper;
import com.wakacommerce.core.web.api.wrapper.CategoryWrapper;
import com.wakacommerce.core.web.api.wrapper.InventoryWrapper;
import com.wakacommerce.core.web.api.wrapper.MediaWrapper;
import com.wakacommerce.core.web.api.wrapper.ProductWrapper;
import com.wakacommerce.core.web.api.wrapper.RelatedProductWrapper;
import com.wakacommerce.core.web.api.wrapper.SearchResultsWrapper;
import com.wakacommerce.core.web.api.wrapper.SkuAttributeWrapper;
import com.wakacommerce.core.web.api.wrapper.SkuWrapper;
import com.wakacommerce.core.web.service.SearchFacetDTOService;

/**
 * This class exposes catalog services as RESTful APIs.  It is dependent on
 * a JAX-RS implementation such as Jersey.  This class must be extended, with appropriate JAX-RS 
 * annotations, such as: <br></br> 
 * 
 * <code>javax.ws.rs.@Scope</code> <br></br> 
 * <code>javax.ws.rs.@Path</code> <br></br> 
 * <code>javax.ws.rs.@Produces</code> <br></br> 
 * <code>javax.ws.rs.@Consumes</code> <br></br> 
 * <code>javax.ws.rs.@Context</code> <br></br> 
 * etc... <br></br>
 * 
 * ... in the subclass.  The subclass must also be a Spring Bean.  The subclass can then override 
 * the methods, and specify custom inputs and outputs.  It will also specify 
 * <code>javax.ws.rs.@Path annotations</code>, <code>javax.ws.rs.@Context</code>, 
 * <code>javax.ws.rs.@PathParam</code>, <code>javax.ws.rs.@QueryParam</code>, 
 * <code>javax.ws.rs.@GET</code>, <code>javax.ws.rs.@POST</code>, etc...  Essentially, the subclass 
 * will override and extend the methods of this class, add new methods, and control the JAX-RS behavior 
 * using annotations according to the JAX-RS specification.
 *
 * User:   
 */
public abstract class CatalogEndpoint extends BaseEndpoint {

    @Resource(name="blCatalogService")
    protected CatalogService catalogService;

    @Resource(name = "blSearchService")
    protected SearchService searchService;

    @Resource(name = "blSearchFacetDTOService")
    protected SearchFacetDTOService facetService;

    @Resource(name = "blExploitProtectionService")
    protected ExploitProtectionService exploitProtectionService;

    @Resource(name = "blStaticAssetPathService")
    protected StaticAssetPathService staticAssetPathService;
    
    @Resource(name = "blInventoryService")
    protected InventoryService inventoryService;

    /**
     * Search for {@code Product} by product id
     *
     * @param id the product id
     * @return the product instance with the given product id
     */
    public ProductWrapper findProductById(HttpServletRequest request, Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            ProductWrapper wrapper;
            wrapper = (ProductWrapper) context.getBean(ProductWrapper.class.getName());
            wrapper.wrapDetails(product, request);
            return wrapper;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.PRODUCT_NOT_FOUND, id);
    }

    /**
     * This uses Broadleaf's search service to search for products or skus within a category.
     * @param request
     * @param q
     * @param categoryId
     * @param pageSize
     * @param page
     * @return
     */
    public SearchResultsWrapper findSearchResultsByCategoryAndQuery(HttpServletRequest request,
            Long categoryId,
            String q,
            Integer pageSize,
            Integer page) {
        try {
            if (StringUtils.isNotEmpty(q)) {
                q = StringUtils.trim(q);
                q = exploitProtectionService.cleanString(q);
            } else {
                throw BroadleafWebServicesException.build(HttpStatus.BAD_REQUEST.value())
                        .addMessage(BroadleafWebServicesException.SEARCH_QUERY_EMPTY);
            }
        } catch (ServiceException e) {
            throw BroadleafWebServicesException.build(HttpStatus.BAD_REQUEST.value())
                    .addMessage(BroadleafWebServicesException.SEARCH_QUERY_MALFORMED, q);
        }

        if (categoryId == null) {
            throw BroadleafWebServicesException.build(HttpStatus.BAD_REQUEST.value())
                    .addMessage(BroadleafWebServicesException.INVALID_CATEGORY_ID, categoryId);
        }

        Category category = null;
        category = catalogService.findCategoryById(categoryId);
        if (category == null) {
            throw BroadleafWebServicesException.build(HttpStatus.BAD_REQUEST.value())
                    .addMessage(BroadleafWebServicesException.INVALID_CATEGORY_ID, categoryId);
        }

        List<SearchFacetDTO> availableFacets = getSearchService().getSearchFacets();
        SearchCriteria searchCriteria = facetService.buildSearchCriteria(request, availableFacets);
        searchCriteria.setPageSize(pageSize);
        searchCriteria.setPage(page);
        try {
            SearchResult result = null;
            result = getSearchService().findSearchResultsByCategoryAndQuery(category, q, searchCriteria);
            facetService.setActiveFacetResults(result.getFacets(), request);

            SearchResultsWrapper wrapper = (SearchResultsWrapper) context.getBean(SearchResultsWrapper.class.getName());
            wrapper.wrapDetails(result, request);
            return wrapper;
        } catch (ServiceException e) {
            throw BroadleafWebServicesException.build(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Queries for products or skus. The parameter q, which represents the query, is required. It can be any 
     * string, but is typically a name or keyword, similar to a search engine search.
     * @param request
     * @param q
     * @param pageSize
     * @param page
     * @return
     */
    public SearchResultsWrapper findSearchResultsByQuery(HttpServletRequest request,
            String q,
            Integer pageSize,
            Integer page) {
        try {
            if (StringUtils.isNotEmpty(q)) {
                q = StringUtils.trim(q);
                q = exploitProtectionService.cleanString(q);
            } else {
                throw BroadleafWebServicesException.build(HttpStatus.BAD_REQUEST.value())
                        .addMessage(BroadleafWebServicesException.SEARCH_QUERY_EMPTY);
            }
        } catch (ServiceException e) {
            throw BroadleafWebServicesException.build(HttpStatus.BAD_REQUEST.value())
                    .addMessage(BroadleafWebServicesException.SEARCH_QUERY_MALFORMED, q);
        }

        List<SearchFacetDTO> availableFacets = getSearchService().getSearchFacets();
        SearchCriteria searchCriteria = facetService.buildSearchCriteria(request, availableFacets);
        searchCriteria.setPageSize(pageSize);
        searchCriteria.setPage(page);
        try {
            SearchResult result = null;
            result = getSearchService().findSearchResultsByQuery(q, searchCriteria);
            facetService.setActiveFacetResults(result.getFacets(), request);

            SearchResultsWrapper wrapper = (SearchResultsWrapper) context.getBean(SearchResultsWrapper.class.getName());
            wrapper.wrapDetails(result, request);
            return wrapper;
        } catch (ServiceException e) {
            throw BroadleafWebServicesException.build(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .addMessage(BroadleafWebServicesException.SEARCH_ERROR);

        }
    }

    /**
     * Search for {@code Sku} instances for a given product
     *
     * @param id
     * @return the list of sku instances for the product
     */
    public List<SkuWrapper> findSkusByProductById(HttpServletRequest request, Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            List<Sku> skus = product.getAllSkus();
            List<SkuWrapper> out = new ArrayList<SkuWrapper>();
            if (skus != null) {
                for (Sku sku : skus) {
                    SkuWrapper wrapper = (SkuWrapper)context.getBean(SkuWrapper.class.getName());
                    wrapper.wrapSummary(sku, request);
                    out.add(wrapper);
                }
                return out;
            }
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.PRODUCT_NOT_FOUND, id);
    }
    
    public SkuWrapper findDefaultSkuByProductId(HttpServletRequest request, Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null && product.getDefaultSku() != null) {
            SkuWrapper wrapper = (SkuWrapper)context.getBean(SkuWrapper.class.getName());
            wrapper.wrapDetails(product.getDefaultSku(), request);
            return wrapper;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.PRODUCT_NOT_FOUND, id);
    }

    public CategoriesWrapper findAllCategories(HttpServletRequest request,
            String name,
            int limit,
            int offset) {
        List<Category> categories;
        if (name != null) {
            categories = catalogService.findCategoriesByName(name, limit, offset);
        } else {
            categories = catalogService.findAllCategories(limit, offset);
        }
        CategoriesWrapper wrapper = (CategoriesWrapper)context.getBean(CategoriesWrapper.class.getName());
        wrapper.wrapDetails(categories, request);
        return wrapper;
    }

    public CategoriesWrapper findSubCategories(HttpServletRequest request,
            Long id,
            int limit,
            int offset,
            boolean active) {
        Category category = catalogService.findCategoryById(id);
        if (category != null) {
            List<Category> categories;
            CategoriesWrapper wrapper = (CategoriesWrapper)context.getBean(CategoriesWrapper.class.getName());
            if (active) {
                categories = catalogService.findActiveSubCategoriesByCategory(category, limit, offset);
            } else {
                categories = catalogService.findAllSubCategories(category, limit, offset);
            }
            wrapper.wrapDetails(categories, request);
            return wrapper;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.CATEGORY_NOT_FOUND, id);

    }

    public CategoriesWrapper findActiveSubCategories(HttpServletRequest request,
            Long id,
            int limit,
            int offset) {
        return findSubCategories(request, id, limit, offset, true);
    }

    public CategoryWrapper findCategoryById(HttpServletRequest request,
            Long id,
            int productLimit,
            int productOffset,
            int subcategoryLimit,
            int subcategoryOffset) {
        Category cat = catalogService.findCategoryById(id);
        if (cat != null) {

            //Explicitly setting these request attributes because the CategoryWrapper.wrap() method needs them
            request.setAttribute("productLimit", productLimit);
            request.setAttribute("productOffset", productOffset);
            request.setAttribute("subcategoryLimit", subcategoryLimit);
            request.setAttribute("subcategoryOffset", subcategoryOffset);

            CategoryWrapper wrapper = (CategoryWrapper)context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(cat, request);
            return wrapper;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.CATEGORY_NOT_FOUND, id);
    }

    /**
     * Allows you to search for a category by ID or by name.
     * @param request
     * @param searchParameter
     * @param productLimit
     * @param productOffset
     * @param subcategoryLimit
     * @param subcategoryOffset
     * @return
     */
    public CategoryWrapper findCategoryByIdOrName(HttpServletRequest request,
            String searchParameter,
            int productLimit,
            int productOffset,
            int subcategoryLimit,
            int subcategoryOffset) {

        Category cat = null;

        if (searchParameter != null) {
            try {
                cat = catalogService.findCategoryById(Long.parseLong(searchParameter));
            } catch (NumberFormatException e) {
                List<Category> categories = catalogService.findCategoriesByName(searchParameter);
                if (categories != null && !categories.isEmpty()) {
                    cat = categories.get(0);
                }
            }
        }
        if (cat != null) {

            //Explicitly setting these request attributes because the CategoryWrapper.wrap() method needs them
            request.setAttribute("productLimit", productLimit);
            request.setAttribute("productOffset", productOffset);
            request.setAttribute("subcategoryLimit", subcategoryLimit);
            request.setAttribute("subcategoryOffset", subcategoryOffset);

            CategoryWrapper wrapper = (CategoryWrapper) context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(cat, request);
            return wrapper;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.CATEGORY_NOT_FOUND, searchParameter);
    }

    public List<RelatedProductWrapper> findUpSaleProductsByProduct(HttpServletRequest request,
            Long id,
            int limit,
            int offset) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            List<RelatedProductWrapper> out = new ArrayList<RelatedProductWrapper>();

            //TODO: Write a service method that accepts offset and limit
            List<RelatedProduct> relatedProds = product.getUpSaleProducts();
            if (relatedProds != null) {
                for (RelatedProduct prod : relatedProds) {
                    RelatedProductWrapper wrapper = (RelatedProductWrapper)context.getBean(RelatedProductWrapper.class.getName());
                    wrapper.wrapSummary(prod, request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.PRODUCT_NOT_FOUND, id);
    }

    public List<RelatedProductWrapper> findCrossSaleProductsByProduct(HttpServletRequest request,
            Long id,
            int limit,
            int offset) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            List<RelatedProductWrapper> out = new ArrayList<RelatedProductWrapper>();

            //TODO: Write a service method that accepts offset and limit
            List<RelatedProduct> xSellProds = product.getCrossSaleProducts();
            if (xSellProds != null) {
                for (RelatedProduct prod : xSellProds) {
                    RelatedProductWrapper wrapper = (RelatedProductWrapper)context.getBean(RelatedProductWrapper.class.getName());
                    wrapper.wrapSummary(prod, request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.PRODUCT_NOT_FOUND, id);
    }
    
    public List<SkuAttributeWrapper> findSkuAttributesForSku(HttpServletRequest request,
            Long id) {
        Sku sku = catalogService.findSkuById(id);
        if (sku != null) {
            ArrayList<SkuAttributeWrapper> out = new ArrayList<SkuAttributeWrapper>();
            if (sku.getSkuAttributes() != null) {
                for (Map.Entry<String, SkuAttribute> entry : sku.getSkuAttributes().entrySet()) {
                    SkuAttributeWrapper wrapper = (SkuAttributeWrapper)context.getBean(SkuAttributeWrapper.class.getName());
                    wrapper.wrapSummary(entry.getValue(), request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.SKU_NOT_FOUND, id);
    }

    public List<MediaWrapper> findMediaForSku(HttpServletRequest request,
            Long id) {
        Sku sku = catalogService.findSkuById(id);
        if (sku != null) {
            List<MediaWrapper> medias = new ArrayList<MediaWrapper>();
            if (sku.getSkuMedia() != null && ! sku.getSkuMedia().isEmpty()) {
                for (Media media : sku.getSkuMedia().values()) {
                    MediaWrapper wrapper = (MediaWrapper)context.getBean(MediaWrapper.class.getName());
                    wrapper.wrapSummary(media, request);
                    if (wrapper.isAllowOverrideUrl()){
                        wrapper.setUrl(staticAssetPathService.convertAssetPath(media.getUrl(), request.getContextPath(), request.isSecure()));
                    }
                    medias.add(wrapper);
                }
            }
            return medias;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.SKU_NOT_FOUND, id);
    }

    public SkuWrapper findSkuById(HttpServletRequest request,
            Long id) {
        Sku sku = catalogService.findSkuById(id);
        if (sku != null) {
            SkuWrapper wrapper = (SkuWrapper)context.getBean(SkuWrapper.class.getName());
            wrapper.wrapDetails(sku, request);
            return wrapper;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.SKU_NOT_FOUND, id);
    }
    
    public List<InventoryWrapper> findInventoryForSkus(HttpServletRequest request, List<Long> ids) {
        List<Sku> skus = catalogService.findSkusByIds(ids);
        if (CollectionUtils.isNotEmpty(skus)) {
            Map<Sku, Integer> quantities = inventoryService.retrieveQuantitiesAvailable(new HashSet<Sku>(skus));
            List<InventoryWrapper> out = new ArrayList<InventoryWrapper>();
            for (Map.Entry<Sku, Integer> entry : quantities.entrySet()) {
                InventoryWrapper wrapper = (InventoryWrapper)context.getBean(InventoryWrapper.class.getName());
                wrapper.wrapSummary(entry.getKey(), entry.getValue(), request);
                out.add(wrapper);
            }
            return out;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.SKU_NOT_FOUND, skus.toArray());
    }

    public List<MediaWrapper> findMediaForProduct(HttpServletRequest request,
            Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            ArrayList<MediaWrapper> out = new ArrayList<MediaWrapper>();
            Map<String, Media> media = product.getMedia();
            if (media != null) {
                for (Media med : media.values()) {
                    MediaWrapper wrapper = (MediaWrapper)context.getBean(MediaWrapper.class.getName());
                    wrapper.wrapSummary(med, request);
                    if (wrapper.isAllowOverrideUrl()){
                        wrapper.setUrl(staticAssetPathService.convertAssetPath(med.getUrl(), request.getContextPath(), request.isSecure()));
                    }
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.PRODUCT_NOT_FOUND, id);
    }

    public List<MediaWrapper> findMediaForCategory(HttpServletRequest request,
            Long id) {
        Category category = catalogService.findCategoryById(id);
        if (category != null) {
            ArrayList<MediaWrapper> out = new ArrayList<MediaWrapper>();
            Map<String, CategoryMediaXref> media = category.getCategoryMediaXref();
            for (CategoryMediaXref med : media.values()) {
                MediaWrapper wrapper = (MediaWrapper)context.getBean(MediaWrapper.class.getName());
                wrapper.wrapSummary(med.getMedia(), request);
                out.add(wrapper);
            }
            return out;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.CATEGORY_NOT_FOUND, id);
    }

    public CategoriesWrapper findParentCategoriesForProduct(HttpServletRequest request,
            Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            CategoriesWrapper wrapper = (CategoriesWrapper)context.getBean(CategoriesWrapper.class.getName());
            List<Category> categories = new ArrayList<Category>();
            for (CategoryProductXref categoryXref : product.getAllParentCategoryXrefs()) {
                categories.add(categoryXref.getCategory());
            }
            wrapper.wrapDetails(categories, request);
            return wrapper;
        }
        throw BroadleafWebServicesException.build(HttpStatus.NOT_FOUND.value())
                .addMessage(BroadleafWebServicesException.PRODUCT_NOT_FOUND, id);
    }

    protected SearchService getSearchService() {
        return searchService;
    }
}

