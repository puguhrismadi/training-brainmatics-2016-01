package com.braimatics.training.controller;

import com.braimatics.training.dao.MateriDao;
import com.braimatics.training.entity.Materi;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.collections.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/materi")
public class MateriController {
    
    @Autowired private MateriDao md;
    
    @Value("classpath:/report/materi.jrxml") 
    private Resource materiJrxml;
    private JasperReport materiJasper;
    
    private JasperReport getMateriJasper() throws IOException, JRException{
        if(materiJasper == null){
            materiJasper = JasperCompileManager.compileReport(materiJrxml.getInputStream());
        }
        return materiJasper;
    }
    
    private JasperPrint generateMateriJasperReport() throws JRException, IOException {
        Map<String, Object> reportParameters = new HashMap<>();
        reportParameters.put("tanggalCetak", new Date());
        JRBeanCollectionDataSource reportData 
                = new JRBeanCollectionDataSource(IteratorUtils.toList(md.findAll().iterator()));
        JasperPrint materiReport = JasperFillManager.fillReport(getMateriJasper(), reportParameters, 
                reportData);
        return materiReport;
    }
    
    @RequestMapping("/pdf")
    @ResponseBody
    public void generatePdf(HttpServletResponse response) throws JRException, IOException{
        JasperPrint materiReport = generateMateriJasperReport();
        response.setContentType("application/x-pdf");
        response.setHeader("Content-disposition", "attachment; filename=daftar-materi.pdf");
        JasperExportManager.exportReportToPdfStream(materiReport, response.getOutputStream());
    }
    
    @RequestMapping("/xls")
    @ResponseBody
    public void generateXls(HttpServletResponse response) throws JRException, IOException{
        JasperPrint materiReport = generateMateriJasperReport();
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment; filename=daftar-materi.xlsx");
        JRXlsxExporter exporter = new JRXlsxExporter();
        
        exporter.setExporterInput(new SimpleExporterInput(materiReport));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
        exporter.exportReport();
    }

    @RequestMapping("list")
    public ModelMap daftarMateri(Pageable page){
        ModelMap mm = new ModelMap();
        
        mm.addAttribute("daftarMateri", md.findAll(page));
        
        return mm;
    }
    
    @RequestMapping("view")
    public void detailMateri(){}
    
    @RequestMapping(value = "form", method = RequestMethod.GET)
    public ModelMap tampilkanForm(@RequestParam(required = false) String id){
        ModelMap mm = new ModelMap();
        
        mm.addAttribute("materi", new Materi());
        
        if(StringUtils.hasText(id)){
            Materi m = md.findOne(id);
            if(m != null){
                mm.addAttribute("materi", m);
            }
        }
        
        return mm;
    }
    
    @RequestMapping(value = "form", method = RequestMethod.POST)
    public String prosesForm(@ModelAttribute @Valid Materi m, BindingResult hasilValidasi){
        
        if(hasilValidasi.hasErrors()) {
            return "/materi/form";
        }
        
        md.save(m);
        return "redirect:list";
    }
}
