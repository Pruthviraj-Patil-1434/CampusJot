package com.campusjot.project.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusjot.project.model.Document;
import com.campusjot.project.model.Subjects;

@Repository
public interface SubjectRepo extends JpaRepository<Subjects, Integer>{
	
	@Query("SELECT s FROM Subjects s WHERE s.group.gid = :groupId")
    List<Subjects> findAllByGroupId(@Param("groupId") String groupId);
	
	Subjects findBySid(int sid);

}
