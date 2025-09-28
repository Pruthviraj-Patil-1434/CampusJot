package com.campusjot.project.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.campusjot.project.model.Document;
import com.campusjot.project.model.Subjects;
import com.campusjot.project.service.DocumentService;
import com.campusjot.project.service.GroupService;
import com.campusjot.project.service.SubjectService;

@RestController
public class SubjectController {
	
	@Autowired
	GroupService groupService;
	
	@Autowired
	SubjectService subjectService;
	
	@Autowired
	DocumentService documentService;
	
	@GetMapping("/subjects/chapters/{subjectName}/{sid}")
	public ModelAndView getChapters(@PathVariable String subjectName,@PathVariable int sid) {
		ModelAndView md=new ModelAndView();
		System.out.println(sid);
		Subjects subject=subjectService.getSubjectById(sid);	
		List<Document> documents=documentService.getAllDocuments(sid);
		md.addObject("documents", documents);
		md.addObject("sid",sid);
		md.addObject("subjectName", subject.getSubjectName());
		md.addObject("group", subject.getGroup().getGname());
		md.addObject("admin", subject.getGroup().getOwner().getUsername());
		md.setViewName("chapters");
		return md;
	}
	

	
}
