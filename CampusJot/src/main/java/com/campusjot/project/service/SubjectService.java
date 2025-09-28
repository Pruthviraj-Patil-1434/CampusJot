package com.campusjot.project.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.campusjot.project.model.Subjects;

import com.campusjot.project.repo.SubjectRepo;

@Service
public class SubjectService {
	
	@Autowired
	SubjectRepo subjectRepository;
	
   
	public List<Subjects> getAllSubjects(String groupId){
		return subjectRepository.findAllByGroupId(groupId);
	}
	
	public Subjects getSubjectById(int sid) {
		return subjectRepository.findBySid(sid);
	}
	
	
}