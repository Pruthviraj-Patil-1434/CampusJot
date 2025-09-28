package com.campusjot.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany; // Import this
import java.util.Set; // Import this

@Entity
public class Client {

	private String email;
	@Id
	private String username;
	private String password;

	// --- ADD THIS ---
	// This completes the relationship, pointing back to the 'contributors' field in the Group class.
	@ManyToMany(mappedBy = "contributors")
	private Set<Group> groups;
	
	
	// --- Your existing methods ---
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	// --- ADD GETTERS AND SETTERS FOR 'groups' ---
	public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

	@Override
	public String toString() {
		return "Client [email=" + email + ", username=" + username + "]";
	}
}