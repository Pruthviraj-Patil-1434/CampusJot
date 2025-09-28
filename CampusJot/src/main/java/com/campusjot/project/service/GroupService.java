package com.campusjot.project.service;

import com.campusjot.project.model.Client;
import com.campusjot.project.model.Group;
import com.campusjot.project.model.Subjects;
import com.campusjot.project.repo.GroupRepo;
import com.campusjot.project.repo.UserRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class GroupService {

	@Autowired
	private GroupRepo groupRepository;
	@Autowired
	private UserRepo clientRepository;
	
	@Autowired
	private SupabaseStorageService storageService;

	@Value("${supabase.url}")
	private String SUPABASE_URL;
	@Value("${supabase.bucket}")
	private String BUCKET_NAME;
	@Value("${supabase.service_role_key}")
	private String API_KEY;

	public Group createGroup(String groupName, Client owner, List<String> contributorEmails) {
		// 1. Generate a random code and create the final group identifier
		String randomCode = generateRandomCode(4);
		String groupIdentifier = groupName.concat("_" + randomCode);

		// 2. Create the folder in your Supabase bucket
		createSupabaseFolder(groupIdentifier);

		// 3. Create the Group entity object
		Group newGroup = new Group(randomCode, groupIdentifier, owner);

		// 4. Process the list of contributor emails
		for (String email : contributorEmails) {
			Optional<Client> existingClient = clientRepository.findByEmail(email);
			if (existingClient.isPresent()) {
				newGroup.addContributor(existingClient.get());
				System.out.println("✅ User '" + email + "' found. Adding to group.");
			} else {
				sendInvitationEmail(email, groupName);
				System.out.println("❌ User '" + email + "' not found. Sending invitation email.");
			}
		}

		// 5. Save the group and its relationships to the database
		return groupRepository.save(newGroup);
	}

	private void createSupabaseFolder(String folderName) {
		String filePath = folderName + "/.keep";
		String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath;

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + API_KEY);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		HttpEntity<byte[]> entity = new HttpEntity<>("".getBytes(), headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException("Failed to create Supabase folder: " + response.getBody());
		}
	}

	private String generateRandomCode(int length) {
		Random random = new Random();
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	private void sendInvitationEmail(String email, String groupName) {
		System.out.println("--- Sending Invite to " + email + " for group " + groupName + " ---");
	}

	public List<Group> getAllGroups(String username) {
		return groupRepository.findByOwnerUsername(username);
	}

	public List<Group> getGroups(String username) {
		Client client = clientRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

		return new ArrayList<>(client.getGroups());
	}

	public void addSubjectToGroup(String groupname, String subjectName) {

		System.out.println("GroupName" + groupname);

		Group group = groupRepository.findByGname(groupname)
				.orElseThrow(() -> new RuntimeException("Group not found with code: " + groupname));

		String fullFolderName = group.getGname();
		String filePath = fullFolderName + "/" + subjectName + "/.keep";
		String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath;

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + API_KEY);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		HttpEntity<byte[]> entity = new HttpEntity<>("".getBytes(), headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {

			throw new RuntimeException("Failed to create subject folder in Supabase: " + response.getBody());
		}

		Subjects newSubject = new Subjects();
		newSubject.setSubjectName(subjectName);

		newSubject.setGroup(group);
		group.getSubjects().add(newSubject);

		groupRepository.save(group);

	}

	public Optional<Group> getGroupByName(String groupName) {
		return groupRepository.findByGname(groupName);
	}

	public boolean addContributorToGroup(String groupId, String clientUsername) {

		try {

			Optional<Group> groupOpt = groupRepository.findByGid(groupId);
			Optional<Client> clientOpt = clientRepository.findByUsername(clientUsername);

			if (clientOpt.isEmpty()) {
				System.out.println(groupId);
				System.out.println(clientUsername);
				System.out.println("Objects are empty");
				return false;
			}

			Group group = groupOpt.get();
			Client client = clientOpt.get();

			group.getContributors().add(client);
			groupRepository.save(group);
			return true;

		} catch (Exception e) {
			System.out.println("Failed to join");
			e.printStackTrace();
			return false;
		}
	}

	public Group findGroupAndContributors(String gid) {
		return groupRepository.findByGidWithContributors(gid)
				.orElseThrow(() -> new RuntimeException("Group not found with id: " + gid));
	}

	public Optional<Group> getGroupById(String gid) {
		return groupRepository.findByGid(gid);
	}

	public void saveGroup(Group g) {
		groupRepository.save(g);
	}

	public void save(Group g) {
		groupRepository.save(g);
	}

	@Transactional
    public boolean deleteGroupById(String groupId) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            System.err.println("Group not found with ID: " + groupId);
            return false;
        }

        Group groupToDelete = groupOptional.get();
        
        String folderPathInBucket = "groups/" + groupToDelete.getGname();

        boolean bucketDeletionSuccess = storageService.deleteFolder(folderPathInBucket);

        if (!bucketDeletionSuccess) {
            throw new RuntimeException("Failed to delete group folder from bucket. Halting database deletion.");
        }

        try {
            for (Client contributor : groupToDelete.getContributors()) {
                contributor.getGroups().remove(groupToDelete);
            }
            
            groupRepository.delete(groupToDelete);
            return true;

        } catch (Exception e) {
            System.err.println("An error occurred during database deletion for group " + groupId);
            throw new RuntimeException("DB deletion failed after bucket cleanup.", e);
        }
    }

}