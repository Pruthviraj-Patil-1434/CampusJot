package com.campusjot.project.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import com.campusjot.project.model.Document;
import com.campusjot.project.model.Group;
import com.campusjot.project.model.Subjects;
import com.campusjot.project.repo.DocumentRepo;
import com.campusjot.project.repo.SubjectRepo;

@Service
public class DocumentService {

	@Autowired
	DocumentRepo documentRepo;
	
	@Autowired
    RestTemplate restTemplate;
	
	@Autowired
	SubjectRepo subjectRepository;

	@Value("${supabase.url}")
	private String SUPABASE_URL;
	@Value("${supabase.bucket}")
	private String BUCKET_NAME;
	@Value("${supabase.service_role_key}")
	private String API_KEY;

	public List<Document> getAllDocuments(int sid) {
		return documentRepo.findBySubjectSid(sid);
	}

	@Transactional
	public void uploadFileToSubject(int subjectId, MultipartFile file) throws IOException {

		Subjects subject = subjectRepository.findById(subjectId)
				.orElseThrow(() -> new RuntimeException("Subject with ID " + subjectId + " not found."));

		Group group = subject.getGroup();

		// group name extracted by fetching subject
		String supabasePath = group.getGname() + "/" + subject.getSubjectName() + "/" + file.getOriginalFilename();

		String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + supabasePath;

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + API_KEY);
		headers.setContentType(MediaType.valueOf(file.getContentType()));

		HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException("Failed to upload file to Supabase: " + response.getBody());
		}

		Document document = new Document();
		document.setFileName(file.getOriginalFilename());
		document.setFileType(file.getContentType());
		document.setUrl(url);
		document.setSubject(subject);

		documentRepo.save(document);

		System.out.println("File uploaded successfully and metadata saved.");
	}

	@Transactional
	public boolean createFile(int subjectId, String fileName) {

		Subjects subject = subjectRepository.findById(subjectId)
				.orElseThrow(() -> new RuntimeException("Subject with ID " + subjectId + " not found."));

		Group group = subject.getGroup();

		String fullFileName = fileName + ".txt";
		String supabasePath = group.getGname() + "/" + subject.getSubjectName() + "/" + fullFileName;
		String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + supabasePath;

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + API_KEY);
		headers.setContentType(MediaType.TEXT_PLAIN);

		byte[] emptyContent = new byte[0];
		HttpEntity<byte[]> entity = new HttpEntity<>(emptyContent, headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			return false;
		}
		

		Document document = new Document();
		document.setFileName(fullFileName);
		document.setFileType(MediaType.TEXT_PLAIN_VALUE);
		document.setUrl(url);
		document.setSubject(subject);

		documentRepo.save(document);
		return true;
	}
	
	public String url(int subjectId) {
		Subjects subject = subjectRepository.findById(subjectId)
	            .orElseThrow(() -> new RuntimeException("Subject not found"));

	   return "/campusjot/subjects/chapters/" + subject.getSubjectName() + "/" + subjectId+"#";
	}
	
	public Document getDocument(int id) {
		Document d=documentRepo.findById(id);
		return  d;
	}
	
	public Document getDocumentBySid(int sid) {
		Document d=documentRepo.findFirstBySubjectSid(sid);
		return d;
	}
	
	@Transactional
	public boolean deleteFile(String fullUrlFromDb) {
	    if (fullUrlFromDb == null || fullUrlFromDb.isBlank()) {
	        System.err.println("URL is null or empty, cannot delete.");
	        return false;
	    }

	    try {
	    	
	    	 String encodedUrl = fullUrlFromDb.replace(" ", "%20");
	        URI uri = new URI(encodedUrl);
	        String path = uri.getPath();
	        
	        // More robustly find the start of the bucket name in the path
	        String bucketSearchString = "/" + BUCKET_NAME + "/";
	        int bucketNameIndex = path.indexOf(bucketSearchString);

	        // Check if the bucket name was found. If not, throw a clear error.
	        if (bucketNameIndex == -1) {
	            throw new IllegalArgumentException("Bucket '" + BUCKET_NAME + "' not found in URL path: " + path);
	        }

	        // Extract the path from the bucket name onwards (e.g., "groups/folder/file.txt")
	        // We add 1 to skip the leading slash "/"
	        String bucketAndFilePath = path.substring(bucketNameIndex + 1);
	        
	        String apiUrl = SUPABASE_URL + "/storage/v1/object/" + bucketAndFilePath;

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + API_KEY);
	        headers.set("apikey", API_KEY);

	        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

	        ResponseEntity<String> response = restTemplate.exchange(
	            apiUrl,
	            HttpMethod.DELETE,
	            requestEntity,
	            String.class
	        );

	        if (response.getStatusCode().is2xxSuccessful()) {
	            documentRepo.deleteByUrl(fullUrlFromDb);
	           // documentRepo.deleteById(null);
	            System.out.println("File deleted successfully from Supabase and DB: " + bucketAndFilePath);
	            return true;
	        } else {
	            System.err.println("Failed to delete file from Supabase. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
	            return false;
	        }

	    } catch (URISyntaxException e) {
	        System.err.println("Invalid URL format: " + fullUrlFromDb);
	        e.printStackTrace();
	        return false;
	    } catch (Exception e) {
	        // This will now catch our clearer "IllegalArgumentException" or other errors.
	    	e.printStackTrace();
	        System.err.println("An error occurred during file deletion: " + e.getMessage());
	        return false;
	    }
	}
	

}
