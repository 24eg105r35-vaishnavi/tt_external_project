package com.portal.dto;

// Simple holder for saved file details
public class StoredFileInfo {

    private String originalName;
    private String storedName;
    private String filePath;
    private String fileType;
    private long fileSize;
    private String checksum;

    public StoredFileInfo() {
    }

    public StoredFileInfo(String originalName, String storedName, String filePath, String fileType, long fileSize, String checksum) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.checksum = checksum;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
