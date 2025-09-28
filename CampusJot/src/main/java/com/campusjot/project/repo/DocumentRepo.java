package com.campusjot.project.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.campusjot.project.model.Document;

@Repository 
public interface DocumentRepo extends JpaRepository<Document,Long> {
	
	Document findById(int did);
	
	List<Document> findBySubjectSid(int sid);
	
	@Query(
		    value = "SELECT d.file_name FROM document d WHERE d.subject_sid = ?1",
		    nativeQuery = true
		)
		List<String> findFilesBySid(int sid);
		
		
	Document findFirstBySubjectSid(int sid);
	
	@Transactional
	@Modifying
	void deleteByUrl(String url);
	void deleteById(Long id);
}
