package com.exam;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class WorkflowService {

    private final RestTemplate restTemplate;
    private final ExamProps props;

    public WorkflowService(RestTemplate restTemplate, ExamProps props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    public void execute() {
        try {
            
            String genUrl = props.getBaseUrl().replaceAll("/+$", "") + "/generateWebhook/JAVA";
            GenerateRequest gr = new GenerateRequest(props.getName(), props.getRegNo(), props.getEmail());

            HttpHeaders hdr = new HttpHeaders();
            hdr.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GenerateRequest> reqEnt = new HttpEntity<>(gr, hdr);

            System.out.println("Calling generateWebhook -> " + genUrl);
            ResponseEntity<GenerateResponse> genResp = restTemplate.exchange(genUrl, HttpMethod.POST, reqEnt, GenerateResponse.class);

            if (!genResp.getStatusCode().is2xxSuccessful() || genResp.getBody() == null) {
                System.err.println("generateWebhook failed: " + genResp.getStatusCode());
                return;
            }

            String webhook = genResp.getBody().getWebhook();
            String accessToken = genResp.getBody().getAccessToken();

            System.out.println("Received webhook: " + webhook);
            System.out.println("Received accessToken: " + (accessToken == null ? "null" : "***"));

            // 2) Determine question based on last two digits of regNo
            int lastTwo = extractLastTwoDigits(props.getRegNo());
            boolean isOdd = (lastTwo % 2) == 1;
            System.out.println("RegNo last two digits = " + lastTwo + " -> " + (isOdd ? "ODD (Q1)" : "EVEN (Q2)"));

            // 3) Prepare final SQL (the solution you want to submit)
            // This is the SQL for: number of employees younger than each employee within their department.
            String finalQuery = ""
            + "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, "
            + "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT "
            + "FROM EMPLOYEE e1 "
            + "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID "
            + "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB "
            + "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME "
            + "ORDER BY e1.EMP_ID DESC;";

            // 4) Store the finalQuery to a local file for record (optional)
            try {
                String outFile = props.getOutputFile() == null ? "finalQuery.sql" : props.getOutputFile();
                Files.writeString(Path.of(outFile), finalQuery);
                System.out.println("Wrote final query to " + outFile);
            } catch (IOException e) {
                System.err.println("Failed to write finalQuery to file: " + e.getMessage());
            }

            // 5) Submit finalQuery to the webhook using the accessToken as JWT in Authorization header (no Bearer)
            SubmitRequest submit = new SubmitRequest(finalQuery);

            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (accessToken != null) {
                submitHeaders.set("Authorization", accessToken); // spec said plain token
            }

            try {
                HttpEntity<SubmitRequest> submitEnt = new HttpEntity<>(submit, submitHeaders);

                System.out.println("Submitting final query to webhook...");
                ResponseEntity<String> submitResp = restTemplate.exchange(
                    webhook, HttpMethod.POST, submitEnt, String.class
                );

                if (submitResp != null) {
                    HttpStatus status = (HttpStatus) submitResp.getStatusCode();
                    System.out.println("Submission HTTP status: " + status + " (" + status.value() + ")");
                    System.out.println("Submission response body: " +
                        (submitResp.getBody() == null ? "<empty>" : submitResp.getBody()));
                } else {
                    System.out.println("submitResp is null!");
                }

            } catch (Exception ex) {
                System.err.println("Workflow failed: " + ex.getMessage());
                ex.printStackTrace();
            }
            }
        catch (Exception outerEx) {  // <-- closes the outer try
                System.err.println("Workflow execution failed: " + outerEx.getMessage());
                outerEx.printStackTrace();
        }}            

      
    

    private int extractLastTwoDigits(String regNo) {
        if (regNo == null) return 0;
        // remove non-digits, take last two characters
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.length() == 0) return 0;
        if (digits.length() == 1) return Integer.parseInt(digits);
        return Integer.parseInt(digits.substring(digits.length() - 2));
    }
}
