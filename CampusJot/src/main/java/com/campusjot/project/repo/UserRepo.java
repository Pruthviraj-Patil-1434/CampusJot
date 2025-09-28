package com.campusjot.project.repo;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusjot.project.model.Client;


@Repository
public interface UserRepo extends JpaRepository<Client, String>{
		Optional<Client> findByEmail(String email);
		Optional<Client> findByUsername(String username);
		
		@Query("SELECT c FROM Client c LEFT JOIN FETCH c.groups WHERE c.username = :username")
		 Optional<Client> findByUsernameWithGroups(@Param("username") String username);
		 
		
}
