package com.campusjot.project.dto;

import java.util.List;

public class CreateGroupRequest {
	private String groupName;
	private List<String> contributorEmails;

	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<String> getContributorEmails() {
		return contributorEmails;
	}

	public void setContributorEmails(List<String> contributorEmails) {
		this.contributorEmails = contributorEmails;
	}
}