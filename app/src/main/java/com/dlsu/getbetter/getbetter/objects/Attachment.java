package com.dlsu.getbetter.getbetter.objects;

import com.google.gson.annotations.SerializedName;

import java.io.File;

/**
 * Created by mikedayupay on 21/02/2016.
 * GetBetter 2016
 */
public class Attachment {

    @SerializedName("case_record_id")
    private int caseRecordId;

    @SerializedName("file_path")
    private String attachmentPath;

    @SerializedName("description")
    private String attachmentDescription;

    @SerializedName("case_attachment_type")
    private int attachmentType;

    @SerializedName("uploaded_on")
    private String uploadedDate;

    private int attachmentId;
    private int uploadedBy;
    private File fileName;
    private int isNew;

    public Attachment(String attachmentPath, String attachmentDescription, int attachmentType, String uploadedDate) {
        this.attachmentPath = attachmentPath;
        this.attachmentDescription = attachmentDescription;
        this.attachmentType = attachmentType;
        this.uploadedDate = uploadedDate;
    }

    public Attachment(String attachmentPath, String attachmentDescription) {
        this.attachmentPath = attachmentPath;
        this.attachmentDescription = attachmentDescription;
    }

    public Attachment(int caseRecordId, String attachmentPath, String attachmentDescription,
                      int attachmentType, String uploadedDate, int uploadedBy) {
        this.caseRecordId = caseRecordId;
        this.attachmentPath = attachmentPath;
        this.attachmentDescription = attachmentDescription;
        this.attachmentType = attachmentType;
        this.uploadedDate = uploadedDate;
        this.uploadedBy = uploadedBy;
    }

    public Attachment(int caseRecordId, String attachmentPath, String attachmentDescription,
                      int attachmentType, String uploadedDate, int uploadedBy, int isNew, int attachmentId) {
        this.caseRecordId = caseRecordId;
        this.attachmentPath = attachmentPath;
        this.attachmentDescription = attachmentDescription;
        this.attachmentType = attachmentType;
        this.uploadedDate = uploadedDate;
        this.uploadedBy = uploadedBy;
        this.isNew = isNew;
        this.attachmentId = attachmentId;
    }

    public Attachment(int caseRecordId, String attachmentPath, String attachmentDescription,
                      int attachmentType, String uploadedDate) {
        this.caseRecordId = caseRecordId;
        this.attachmentPath = attachmentPath;
        this.attachmentDescription = attachmentDescription;
        this.attachmentType = attachmentType;
        this.uploadedDate = uploadedDate;
    }

    public Attachment(String attachmentPath, File fileName) {
        this.attachmentPath = attachmentPath;
        this.fileName = fileName;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getAttachmentDescription() {
        return attachmentDescription;
    }

    public void setAttachmentDescription(String attachmentDescription) {
        this.attachmentDescription = attachmentDescription;
    }

    public int getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(int attachmentType) {
        this.attachmentType = attachmentType;
    }

    public int getCaseRecordId() {
        return caseRecordId;
    }

    public void setCaseRecordId(int caseRecordId) {
        this.caseRecordId = caseRecordId;
    }

    public String getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(String uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public File getFileName() {
        return fileName;
    }

    public void setFileName(File fileName) {
        this.fileName = fileName;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
    }
}
