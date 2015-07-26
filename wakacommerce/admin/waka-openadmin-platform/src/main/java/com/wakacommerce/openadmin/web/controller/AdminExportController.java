
package com.wakacommerce.openadmin.web.controller;

import org.springframework.web.servlet.ModelAndView;

import com.wakacommerce.openadmin.server.service.export.AdminExporter;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 
 *Phillip Verheyden
 */
public class AdminExportController extends AdminAbstractController {
    
    @Resource(name = "blAdminExporters")
    protected List<AdminExporter> exporters;

    public ModelAndView export(HttpServletRequest request, HttpServletResponse response, Map<String, String> params) throws IOException {
        String exporterName = params.get("exporter");
        AdminExporter exporter = null;
        for (AdminExporter test : exporters) {
            if (test.getName().equals(exporterName)) {
                exporter = test;
            }
        }
        if (exporter == null) {
            throw new RuntimeException("Could not find exporter with name: " + exporterName);
        }
        
        response.setContentType("application/download");
        String fileName = exporter.getFileName();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        
        ServletOutputStream stream = response.getOutputStream();
        exporter.writeExport(stream, params);
        stream.flush();
        
        return null;
    }

    public List<AdminExporter> getExporters() {
        return exporters;
    }
    
    public void setExporters(List<AdminExporter> exporters) {
        this.exporters = exporters;
    }

}