package com.campusjot.project.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne; // Import this
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="groups")
public class Group {

	@Id
	private String gid;	
	private String gname;


	@ManyToOne
	@JoinColumn(name = "owner_username", referencedColumnName = "username") // Foreign key column in 'groups' table
	private Client owner;
	
	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Subjects> subjects = new HashSet<>();
	

	public Set<Subjects> getSubjects() {
		return subjects;
	}

	public void setSubjects(Set<Subjects> subjects) {
		this.subjects = subjects;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(
		name = "group_contributors", 
		joinColumns = @JoinColumn(name = "group_id"),
		inverseJoinColumns = @JoinColumn(name = "client_username") 
	)
	private Set<Client> contributors = new HashSet<>();
	
	public void addContributor(Client client) {
		this.contributors.add(client);
	}

	public Group() {
		super();
	}

	// --- CHANGE #2: Updated the constructor to accept a Client object ---
	public Group(String gid, String gname, Client owner) {
		super();
		this.gid = gid;
		this.gname = gname;
		this.owner = owner;
	}
	
	public String getGid() { return gid; }
	public String getGname() { return gname; }
	
	
	public Client getOwner() { return owner; }

	public Set<Client> getContributors() {
		return contributors;
	}
}