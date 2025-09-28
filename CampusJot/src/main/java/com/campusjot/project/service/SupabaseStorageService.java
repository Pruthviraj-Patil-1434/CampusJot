package com.campusjot.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupabaseStorageService {
	
	@Autowired
    private RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.service_role_key}")
    private String API_KEY;

    @Value("${supabase.bucket}")
    private String BUCKET_NAME;


    private static class FileObject {
        public String name;
    }

    public boolean deleteFolder(String folderPath) {
        try {
            String listUrl = SUPABASE_URL + "/storage/v1/object/list/" + BUCKET_NAME;
            HttpHeaders headers = createHeaders();

            String requestBody = "{\"prefix\":\"" + folderPath + "\",\"options\":{\"limit\":1000}}";
            HttpEntity<String> listRequestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<FileObject[]> listResponse = restTemplate.exchange(
                listUrl, HttpMethod.POST, listRequestEntity, FileObject[].class);

            if (!listResponse.getStatusCode().is2xxSuccessful() || listResponse.getBody() == null) {
                System.err.println("Failed to list files in folder: " + folderPath);
                return false;
            }

            List<String> filesToDelete = Arrays.stream(listResponse.getBody())
                .map(file -> folderPath + "/" + file.name)
                .collect(Collectors.toList());

            if (filesToDelete.isEmpty()) {
                System.out.println("Folder is empty or does not exist, nothing to delete: " + folderPath);
                return true;
            }

            String deleteUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME;
            String deleteBody = "{\"prefixes\":" + toJsonArrayString(filesToDelete) + "}";
            HttpEntity<String> deleteRequestEntity = new HttpEntity<>(deleteBody, headers);

            ResponseEntity<String> deleteResponse = restTemplate.exchange(
                deleteUrl, HttpMethod.DELETE, deleteRequestEntity, String.class);

            if (deleteResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("Successfully deleted folder and its contents: " + folderPath);
                return true;
            } else {
                System.err.println("Failed to delete folder contents. Status: " + deleteResponse.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("An error occurred during folder deletion: " + e.getMessage());
            return false;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.set("apikey", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String toJsonArrayString(List<String> list) {
        return "[" + list.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]";
    }
}