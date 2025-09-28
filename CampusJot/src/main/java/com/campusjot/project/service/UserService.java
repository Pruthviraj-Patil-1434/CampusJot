package com.campusjot.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campusjot.project.model.Client;
import com.campusjot.project.repo.UserRepo;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService {
	
	@Autowired
	private UserRepo userRepo;
	
	HttpSession session;

	public void saveUser(Client u) {
			userRepo.save(u);
	}
	
	public Client getbyEmail(String email) {
		return userRepo.findByEmail(email).orElse(new Client());
	}
	
	
	public Client getbyUserName(String username) {   
	    return userRepo.findByUsernameWithGroups(username).orElse(null);
	}
	
	public Client getByUsernameWithGroups(String username) {
        // Use the new method that eagerly fetches the groups
        return userRepo.findByUsernameWithGroups(username)
                .orElse(null);
    }

	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}
	
	
	public Client getByUserNameOnly(String uname) {
		return userRepo.findByUsername(uname).orElseThrow(()->new RuntimeException("User not found with username:"));
	}
}
