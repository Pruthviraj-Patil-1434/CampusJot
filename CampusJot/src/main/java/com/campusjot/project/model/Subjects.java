package com.campusjot.project.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Subjects {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int sid;
	
	String subjectName;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_gid", nullable = false)
	private Group group;
	
	
	 @OneToMany(mappedBy = "subject")
	private List<Document> documents = new ArrayList<>();

	 
	 
	public int getSid() {
		return sid;
	}

	 public void setSid(int sid) {
		 this.sid = sid;
	 }

	public List<Document> getDocuments() {
		return documents;
	}

	 public void setDocuments(List<Document> documents) {
		 this.documents = documents;
	 }

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
