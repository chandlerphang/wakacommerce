package com.wakacommerce.cms.admin.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wakacommerce.cms.file.domain.ImageStaticAssetImpl;
import com.wakacommerce.cms.file.domain.StaticAsset;
import com.wakacommerce.cms.file.service.StaticAssetService;
import com.wakacommerce.cms.file.service.StaticAssetStorageService;
import com.wakacommerce.common.persistence.EntityConfiguration;
import com.wakacommerce.openadmin.server.service.handler.CustomPersistenceHandler;
import com.wakacommerce.openadmin.web.controller.AdminAbstractController;
import com.wakacommerce.openadmin.web.form.component.ListGrid;
import com.wakacommerce.openadmin.web.form.component.ListGrid.Type;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller("blAdminAssetUploadController")
@RequestMapping("/{sectionKey}")
public class AdminAssetUploadController extends AdminAbstractController {

    @Resource(name = "blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;
    
    @Resource(name = "blStaticAssetStorageService")
    protected StaticAssetStorageService staticAssetStorageService;
    
    @Resource(name = "blStaticAssetService")
    protected StaticAssetService staticAssetService;
    
    @Resource(name = "blAdminAssetController")
    protected AdminAssetController assetController;


    @RequestMapping(value = "/{id}/chooseAsset", method = RequestMethod.GET)
    public String chooseMediaForMapKey(HttpServletRequest request, HttpServletResponse response, Model model, 
            @PathVariable(value = "sectionKey") String sectionKey, 
            @PathVariable(value = "id") String id,
            @RequestParam MultiValueMap<String, String> requestParams) throws Exception {
        Map<String, String> pathVars = new HashMap<String, String>();
        pathVars.put("sectionKey", AdminAssetController.SECTION_KEY);
        assetController.viewEntityList(request, response, model, pathVars, requestParams);
        
        ListGrid listGrid = (ListGrid) model.asMap().get("listGrid");
        listGrid.setPathOverride("/" + sectionKey + "/" + id + "/chooseAsset");
        listGrid.setListGridType(Type.ASSET);
        
        String userAgent = request.getHeader("User-Agent");
        model.addAttribute("isIE", userAgent.contains("MSIE"));
        
        model.addAttribute("viewType", "modal/selectAsset");
        model.addAttribute("currentUrl", request.getRequestURL().toString());
        model.addAttribute("modalHeaderType", "selectAsset");

        model.addAttribute("currentParams", new ObjectMapper().writeValueAsString(requestParams));
        
        // We need these attributes to be set appropriately here
        model.addAttribute("entityId", id);
        model.addAttribute("sectionKey", sectionKey);
        return "modules/modalContainer";
    }
    
    @RequestMapping(value = "/{id}/uploadAsset", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> upload(HttpServletRequest request,
            @RequestParam("file") MultipartFile file, 
            @PathVariable(value="sectionKey") String sectionKey, @PathVariable(value="id") String id) throws IOException {
        Map<String, Object> responseMap = new HashMap<String, Object>();
        
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("entityType", sectionKey);
        properties.put("entityId", id);
        
        StaticAsset staticAsset = staticAssetService.createStaticAssetFromFile(file, properties);
        staticAssetStorageService.createStaticAssetStorageFromFile(file, staticAsset);

        String staticAssetUrlPrefix = staticAssetService.getStaticAssetUrlPrefix();
        if (staticAssetUrlPrefix != null && !staticAssetUrlPrefix.startsWith("/")) {
            staticAssetUrlPrefix = "/" + staticAssetUrlPrefix;
        }

        String assetUrl =  staticAssetUrlPrefix + staticAsset.getFullUrl();

        responseMap.put("adminDisplayAssetUrl", request.getContextPath() + assetUrl);
        responseMap.put("assetUrl", assetUrl);
        
        if (staticAsset instanceof ImageStaticAssetImpl) {
            responseMap.put("image", Boolean.TRUE);
            responseMap.put("assetThumbnail", assetUrl + "?smallAdminThumbnail");
            responseMap.put("assetLarge", assetUrl + "?largeAdminThumbnail");
        } else {
            responseMap.put("image", Boolean.FALSE);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity<Map<String, Object>>(responseMap, responseHeaders, HttpStatus.OK);
    }
    
    /**
     * Used by the Asset list view to upload an asset and then immediately show the
     * edit form for that record.
     * 
     * @param request
     * @param file
     * @param sectionKey
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/uploadAsset", method = RequestMethod.POST)
    public String upload(HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @PathVariable(value="sectionKey") String sectionKey) throws IOException {
        
        StaticAsset staticAsset = staticAssetService.createStaticAssetFromFile(file, null);
        staticAssetStorageService.createStaticAssetStorageFromFile(file, staticAsset);

        String staticAssetUrlPrefix = staticAssetService.getStaticAssetUrlPrefix();
        if (staticAssetUrlPrefix != null && !staticAssetUrlPrefix.startsWith("/")) {
            staticAssetUrlPrefix = "/" + staticAssetUrlPrefix;
        }
        
        return "redirect:/assets/" + staticAsset.getId();
    }

    @RequestMapping(value = "/{addlSectionKey}/{id}/chooseAsset", method = RequestMethod.GET)
    public String chooseMediaForMapKey(HttpServletRequest request, HttpServletResponse response, Model model,
            @PathVariable(value = "sectionKey") String sectionKey,
            @PathVariable(value = "addlSectionKey") String addlSectionKey,
            @PathVariable(value = "id") String id,
            @RequestParam MultiValueMap<String, String> requestParams) throws Exception {
        return chooseMediaForMapKey(request, response, model, sectionKey, id, requestParams);
    }

    @RequestMapping(value = "/{addlSectionKey}/{id}/uploadAsset",
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> upload(HttpServletRequest request,
            @RequestParam("file") MultipartFile file,            
            @PathVariable(value = "sectionKey") String sectionKey,
            @PathVariable(value = "addlSectionKey") String addlSectionKey,
            @PathVariable(value = "id") String id) throws IOException {
        return upload(request, file, sectionKey, id);
    }
}
