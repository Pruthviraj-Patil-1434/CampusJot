package com.campusjot.project.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusjot.project.model.Group;

@Repository
public interface GroupRepo extends JpaRepository<Group, String> { 
    
    List<Group> findByOwnerUsername(String username);
	Optional<Group> findByGid(String code);
	Optional<Group> findByGname(String groupName);
	
	
	  @Query("SELECT g FROM Group g LEFT JOIN FETCH g.contributors WHERE g.gid = :gid")
	    Optional<Group> findByGidWithContributors(@Param("gid") String gid);
}