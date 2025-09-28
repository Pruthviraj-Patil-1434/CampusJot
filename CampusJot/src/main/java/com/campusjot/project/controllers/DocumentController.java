package com.campusjot.project.controllers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.campusjot.project.model.Document;
import com.campusjot.project.model.Subjects;
import com.campusjot.project.repo.DocumentRepo;
import com.campusjot.project.service.DocumentService;
import com.campusjot.project.service.SubjectService;
import com.campusjot.project.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class DocumentController {

    private final DocumentRepo documentRepo;

	@Autowired
	SubjectService subjectService;

	@Autowired
	DocumentService documentService;

	@Autowired
	UserService userService;

	@Autowired
	RestTemplate  restTemplate;
	
	@Value("${supabase.service_role_key}")
	private String SERVICE_ROLE_KEY;

    DocumentController(DocumentRepo documentRepo) {
        this.documentRepo = documentRepo;
    }
		
	@PostMapping("/subject/{subid}/upload")
	public String upload(@PathVariable("subid") int sid, @RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) { // Add RedirectAttributes
		
		Subjects subject=subjectService.getSubjectById(sid);
		
		
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("uploadSuccess", false);
			redirectAttributes.addFlashAttribute("uploadMessage", "Error: File cannot be empty.");
			return "redirect:/subjects/chapters/"+subject.getSubjectName()+"/"+sid+"#";
		}

		if (!"text/plain".equals(file.getContentType())) {
			redirectAttributes.addFlashAttribute("uploadSuccess", false);
			redirectAttributes.addFlashAttribute("uploadMessage", "Error: Only .txt files are allowed.");
			return "redirect:/subjects/chapters/"+subject.getSubjectName()+"/"+sid+"#";
		}

		try {
			documentService.uploadFileToSubject(sid, file);
			redirectAttributes.addFlashAttribute("uploadSuccess", true);
			redirectAttributes.addFlashAttribute("uploadMessage", "File uploaded successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("uploadSuccess", false);
			redirectAttributes.addFlashAttribute("uploadMessage", "Error: File upload failed.");
			System.out.println("File uplpad failed");
			e.printStackTrace();
		}

		return "redirect:/subjects/chapters/"+subject.getSubjectName()+"/"+sid+"#";
	}

	@PostMapping("/subjects/{subjectId}/createnote")
	public RedirectView createFile(@PathVariable("subjectId") int subjectId, @RequestParam("fileName") String fileName,
			RedirectAttributes redirectAttributes) {

		String redirectUrl = documentService.url(subjectId);

		boolean success = documentService.createFile(subjectId, fileName);

		if (success) {
			redirectAttributes.addFlashAttribute("success", true);
			redirectAttributes.addFlashAttribute("message", "File '" + fileName + ".txt' created successfully!");
		} else {
			redirectAttributes.addFlashAttribute("success", false);
			redirectAttributes.addFlashAttribute("message", "Error: Could not create the file.");
		}

		return new RedirectView(redirectUrl);
	}



	@GetMapping("/documents/edit/{id}")
	public ModelAndView editFile(@PathVariable int id) {
	    ModelAndView mav = new ModelAndView("editing");

	    Document doc = documentService.getDocument(id);
	    String subject = doc.getSubject().getSubjectName();
	    int sid=doc.getSubject().getSid();
	    String url = doc.getUrl();

	    try {
	        String fileContent = restTemplate.getForObject(url, String.class);
	        mav.addObject("fileContent", fileContent);
	        mav.addObject("url",url);
	    } catch (Exception e) {
	        e.printStackTrace();
	        mav.addObject("fileContent", "Error could not retrieve the file");
	    }

	    mav.addObject("document", doc);
	   mav.addObject("groupname", doc.getSubject().getGroup().getGname());
	    mav.addObject("subjectName", subject);
	    mav.addObject("subid", sid);
	    return mav;
	}

	
	
	@PostMapping("/documents/save")
	public String saveDocument(@RequestParam("documentId") int documentId,
	                           @RequestParam("content") String editedContent,
	                           RedirectAttributes redirectAttributes) {

	    Document doc = documentService.getDocument(documentId);
	    if (doc == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Document not found!");
	        return "redirect:/documents/edit/" + documentId;
	    }

	    String uploadUrl = doc.getUrl();

	    try {
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + SERVICE_ROLE_KEY);
	        headers.set("x-upsert", "true");
	        headers.setContentType(MediaType.TEXT_PLAIN);

	        byte[] fileContentBytes = editedContent.getBytes(StandardCharsets.UTF_8);
	        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileContentBytes, headers);

	        ResponseEntity<String> response = restTemplate.exchange(
	            uploadUrl,
	            HttpMethod.POST,
	            requestEntity,
	            String.class
	        );

	        if (response.getStatusCode().is2xxSuccessful()) {
	            redirectAttributes.addFlashAttribute("successMessage", "File saved successfully!");
	        } else {
	            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save file: " + response.getBody());
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while saving the file.");
	    }

	    return "redirect:/documents/edit/" + documentId;
	}
	
	@PostMapping("/document/delete")
	public String deleteDocument(@RequestParam	("documentId") int docId, RedirectAttributes redirectAttributes) {

	    Document documentToDelete = documentService.getDocument(docId);
	    int subjectId = documentToDelete.getSubject().getSid();
	    String subjectName = documentToDelete.getSubject().getSubjectName();
	    
	 /*   if (documentToDelete == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Document not found and could not be deleted.");
	        return "redirect:/groups";
	    }
	*/
	  
	    
	    String fullUrl = documentToDelete.getUrl();
	    boolean success = documentService.deleteFile(fullUrl);
	   // documentRepo.save(documentToDelete);
	    if (success) {
	        redirectAttributes.addFlashAttribute("successMessage", "File deleted successfully!");
	    } else {
	        redirectAttributes.addFlashAttribute("errorMessage", "Error: Could not delete the file.");
	    }

	    String redirectUrl = "/subjects/chapters/" + subjectName + "/" + subjectId;
	    return "redirect:" + redirectUrl;
	}
}
