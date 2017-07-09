package com.dlsu.getbetter.getbetter.objects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 25/02/2016.
 * GetBetter 2016
 */
public class CaseRecord {

    @SerializedName("record_status_id")
    private int caseRecordStatusId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("health_center_id")
    private int healthCenterId;

    @SerializedName("complaint")
    private String caseRecordComplaint;

    @SerializedName("additional_notes")
    private String caseRecordAdditionalNotes;

    @SerializedName("control_number")
    private String caseRecordControlNumber;

    @SerializedName("updated_on")
    private String caseRecordUpdatedOn;

    @SerializedName("updated_by")
    private int caseRecordUpdatedBy;

    @SerializedName("case_record_id")
    private int caseRecordId;

    private int caseId;
    private String caseRecordStatus;
    private String healthCenter;
    private String patientName;
    private String profilePic;
    private ArrayList<Attachment> attachments;
    private boolean checked = false;

    public CaseRecord() {

    }

    //constructor for getCaseRecordHistory db query function
    public CaseRecord(int caseRecordId, int caseRecordStatusId, int caseRecordUpdatedBy, String caseRecordUpdatedOn) {
        this.caseRecordId = caseRecordId;
        this.caseRecordStatusId = caseRecordStatusId;
        this.caseRecordUpdatedBy = caseRecordUpdatedBy;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public CaseRecord(int caseRecordId, String caseRecordComplaint, String caseRecordUpdatedOn, int caseRecordStatusId) {
        this.caseRecordId = caseRecordId;
        this.caseRecordComplaint = caseRecordComplaint;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
        this.caseRecordStatusId = caseRecordStatusId;
    }

    public CaseRecord(int caseRecordId, String patientName, String caseRecordControlNumber,
                      String caseRecordComplaint, String caseRecordAdditionalNotes,
                      String healthCenter, String caseRecordStatus, String caseRecordUpdatedOn) {
        this.caseRecordId = caseRecordId;
        this.patientName = patientName;
        this.caseRecordControlNumber = caseRecordControlNumber;
        this.caseRecordComplaint = caseRecordComplaint;
        this.caseRecordAdditionalNotes = caseRecordAdditionalNotes;
        this.healthCenter = healthCenter;
        this.caseRecordStatus = caseRecordStatus;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public CaseRecord(int caseRecordId, int userId, String caseRecordComplaint, String caseRecordUpdatedOn) {
        this.caseRecordId = caseRecordId;
        this.userId = userId;
        this.caseRecordComplaint = caseRecordComplaint;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public CaseRecord(int caseRecordId, String complaint, int userId, String caseRecordControlNumber, String caseRecordAdditionalNotes) {
        this.caseRecordId = caseRecordId;
        this.caseRecordComplaint = complaint;
        this.userId = userId;
        this.caseRecordControlNumber = caseRecordControlNumber;
        this.caseRecordAdditionalNotes = caseRecordAdditionalNotes;
    }

    public CaseRecord(int caseRecordId, int caseId, int userId, int caseRecordStatusId, String caseRecordComplaint,
                      String caseRecordAdditionalNotes, String caseRecordControlNumber, String caseRecordUpdatedOn) {

        this.caseRecordId = caseRecordId;
        this.caseId = caseId;
        this.userId = userId;
        this.caseRecordStatusId = caseRecordStatusId;
        this.caseRecordComplaint = caseRecordComplaint;
        this.caseRecordAdditionalNotes = caseRecordAdditionalNotes;
        this.caseRecordControlNumber = caseRecordControlNumber;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public int getHealthCenterId() {
        return healthCenterId;
    }

    public void setHealthCenterId(int healthCenterId) {
        this.healthCenterId = healthCenterId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCaseRecordStatus(String caseRecordStatus) {
        this.caseRecordStatus = caseRecordStatus;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setCaseRecordId(int caseRecordId) {
        this.caseRecordId = caseRecordId;
    }

    public int getCaseRecordStatusId() {
        return caseRecordStatusId;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public void setCaseRecordStatusId(int caseRecordStatusId) {
        this.caseRecordStatusId = caseRecordStatusId;
    }

    public void setCaseRecordComplaint(String caseRecordComplaint) {
        this.caseRecordComplaint = caseRecordComplaint;
    }

    public void setCaseRecordControlNumber(String caseRecordControlNumber) {
        this.caseRecordControlNumber = caseRecordControlNumber;
    }

    public String getCaseRecordUpdatedOn() {
        return caseRecordUpdatedOn;
    }

    public void setCaseRecordUpdatedOn(String caseRecordUpdatedOn) {
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public int getCaseRecordUpdatedBy() {
        return caseRecordUpdatedBy;
    }

    public void setCaseRecordUpdatedBy(int caseRecordUpdatedBy) {
        this.caseRecordUpdatedBy = caseRecordUpdatedBy;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getHealthCenter() {
        return healthCenter;
    }

    public void setHealthCenter(String healthCenter) {
        this.healthCenter = healthCenter;
    }

    public int getCaseRecordId() {
        return caseRecordId;
    }

    public String getCaseRecordComplaint() {
        return caseRecordComplaint;
    }

    public String getCaseRecordControlNumber() {
        return caseRecordControlNumber;
    }

    public String getCaseRecordStatus() {
        return caseRecordStatus;
    }

    public String getCaseRecordAdditionalNotes() {
        return caseRecordAdditionalNotes;
    }

    public void setCaseRecordAdditionalNotes(String caseRecordAdditionalNotes) {
        this.caseRecordAdditionalNotes = caseRecordAdditionalNotes;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}
