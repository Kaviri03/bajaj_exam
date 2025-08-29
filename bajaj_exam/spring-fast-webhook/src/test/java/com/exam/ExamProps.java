package com.exam;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exam")
public class ExamProps {
    private String name;
    private String regNo;
    private String email;
    private String baseUrl;
    private String outputFile;

    // getters & setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getOutputFile() { return outputFile; }
    public void setOutputFile(String outputFile) { this.outputFile = outputFile; }
}
