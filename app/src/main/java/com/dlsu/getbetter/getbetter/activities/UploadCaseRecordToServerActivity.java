package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.RequestHandler;
import com.dlsu.getbetter.getbetter.adapters.CaseRecordUploadAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


public class UploadCaseRecordToServerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CASE_RECORD_ID_KEY = "caseRecordId";
    private static final String USER_ID_KEY = "userId";
    private static final String HEALTH_CENTER_ID_KEY = "healthCenterId";
    private static final String COMPLAINT_KEY = "complaint";
    private static final String CONTROL_NUMBER_KEY = "controlNumber";
    private static final String CASE_RECORD_STATUS_ID_KEY = "caseRecordStatusId";
    private static final String UPDATED_BY_KEY = "updatedBy";
    private static final String UPDATED_ON_KEY = "updatedOn";

    private static final String ATTACHMENT_DESCRIPTION_KEY = "description";
    private static final String ATTACHMENT_NAME_KEY = "attachment_name";
    private static final String ENCODED_IMAGE_KEY = "encoded_image";
    private static final String ATTACHMENT_TYPE_ID_KEY = "attachment_type";
    private static final String UPLOADED_ON_KEY = "uploaded_on";
    private static final String TAG_JSON_RESULT = "result";
    private static final String RESULT_MESSAGE = "SUCCESS";

    private ArrayList<CaseRecord> caseRecordsUpload;
    private ArrayList<Attachment> caseRecordAttachmentsUpload;
    private ArrayList<CaseRecord> caseRecordHistoryUpload;
    private ArrayList<Integer> caseRecordId;
    private long userId;
    private int healthCenterId;
    private ProgressDialog cDialog = null;

    private DataAdapter getBetterDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_case_record_to_server);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);

        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));
        String midwifeName = user.get(SystemSessionManager.LOGIN_USER_NAME);

        Bundle extras = getIntent().getExtras();
        caseRecordsUpload = new ArrayList<>();
        caseRecordAttachmentsUpload = new ArrayList<>();
        caseRecordHistoryUpload = new ArrayList<>();
        caseRecordId = new ArrayList<>();

        userId = extras.getLong("patientId");

        Button backBtn = (Button)findViewById(R.id.upload_caserecord_back_btn);
        Button uploadBtn = (Button)findViewById(R.id.upload_caserecord_upload_btn);
        ListView caseRecordList = (ListView)findViewById(R.id.upload_page_case_record_list);
        TextView patientNameLabel = (TextView)findViewById(R.id.patient_label);

        uploadBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        initializeDatabase();
        if (patientNameLabel != null) {
            patientNameLabel.setText(getPatientName((int)userId));
        }
        new PopulateCaseRecordListTask().execute();

        CaseRecordUploadAdapter caseRecordUploadAdapter = new CaseRecordUploadAdapter(this, R.layout.case_record_item_checkbox, caseRecordsUpload);
        caseRecordList.setAdapter(caseRecordUploadAdapter);
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getCaseRecordsUpload () {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseRecordsUpload.addAll(getBetterDb.getCaseRecordsUpload(userId));
        Log.d("case upload size", caseRecordsUpload.size() + "");
        getBetterDb.closeDatabase();

    }

    private String getPatientName (int userId) {

        String patientName;

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        patientName = getBetterDb.getPatientName(userId);

        getBetterDb.closeDatabase();

        return patientName;

    }

    private void getCaseRecordAttachments(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseRecordAttachmentsUpload.addAll(getBetterDb.getCaseRecordAttachments(caseRecordId));
        getBetterDb.closeDatabase();

    }

    private CaseRecord getCaseRecordHistory(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        CaseRecord caseRecordHistory = getBetterDb.getCaseRecordHistoryUpload(caseRecordId);
        getBetterDb.closeDatabase();
        return caseRecordHistory;

    }

    private void removeCaseRecordsUpload (int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.removeCaseRecordUpload(caseRecordId);
        getBetterDb.closeDatabase();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.upload_caserecord_back_btn) {

            finish();
        } else if(id == R.id.upload_caserecord_upload_btn) {

            ArrayList<CaseRecord> selectedCaseRecordsList = new ArrayList<>();

            for (int i = 0; i < caseRecordsUpload.size(); i++) {

                CaseRecord selectedCaseRecord = caseRecordsUpload.get(i);

                if (selectedCaseRecord.isChecked()) {
                    selectedCaseRecordsList.add(selectedCaseRecord);
                    caseRecordId.add(selectedCaseRecord.getCaseRecordId());
                }
            }

            Log.e("size", selectedCaseRecordsList.size() + "");
            UploadCaseRecordsTask uploadCaseRecordsTask = new UploadCaseRecordsTask();
            uploadCaseRecordsTask.execute(selectedCaseRecordsList);

        }
    }

    private class PopulateCaseRecordListTask extends AsyncTask<Void, Void, Void> {

//        ProgressDialog progressDialog = new ProgressDialog(UploadCaseRecordToServerActivity.this);

        @Override
        protected void onPreExecute() {
//            progressDialog.setMessage("Populating Case Record List");
//            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            getCaseRecordsUpload();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
//            if(progressDialog.isShowing()) {
//                progressDialog.hide();
//                progressDialog.dismiss();

//            }

        }

    }

    private class UploadCaseRecordsTask extends AsyncTask<ArrayList<CaseRecord>, Void, String > {

        //ProgressDialog progressDialog = new ProgressDialog(UploadCaseRecordToServerActivity.this);
        RequestHandler rh = new RequestHandler();

        @Override
        protected void onPreExecute() {
            showProgressDialog("Uploading Case Record/s...");
        }

        @Override
        protected String doInBackground(ArrayList<CaseRecord>... params) {

            ArrayList<CaseRecord> caseRecordUpload = params[0];
            String result = "";
            String user = String.valueOf(userId);

            for(int i = 0; i < caseRecordUpload.size(); i++) {

                Log.d("id", caseRecordUpload.get(i).getCaseRecordId() + "");
                Log.d("user", user);
                Log.d("hc", healthCenterId + "");
                Log.d("complaint", caseRecordUpload.get(i).getCaseRecordComplaint());
                Log.d("cn", caseRecordUpload.get(i).getCaseRecordControlNumber());

                HashMap<String, String> data = new HashMap<>();
                data.put(CASE_RECORD_ID_KEY, String.valueOf(caseRecordUpload.get(i).getCaseRecordId()));
                data.put(USER_ID_KEY, user);
                data.put(HEALTH_CENTER_ID_KEY, String.valueOf(healthCenterId));
                data.put(COMPLAINT_KEY, caseRecordUpload.get(i).getCaseRecordComplaint());
                data.put(CONTROL_NUMBER_KEY, caseRecordUpload.get(i).getCaseRecordControlNumber());
                result = rh.sendPostRequest(DirectoryConstants.UPLOAD_CASE_RECORD_SERVER_SCRIPT_URL, data);

                CaseRecord history = getCaseRecordHistory(caseRecordUpload.get(i).getCaseRecordId());
                caseRecordHistoryUpload.add(history);
                getCaseRecordAttachments(caseRecordsUpload.get(i).getCaseRecordId());
            }
            Log.d("Attachments", caseRecordAttachmentsUpload.size() + "");
            return result;
        }

        @Override
        protected void onPostExecute(String s) {

            dismissProgressDialog();

            String message = getServerMessage(s);

            if(RESULT_MESSAGE.contentEquals(message)) {
                uploadCaseRecordHistory();
            } else {
                featureAlertMessage("Failed to upload Case Record.");
            }
        }

    }

    private void uploadCaseRecordAttachments() {

        class UploadCaseRecordAttachments extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                showProgressDialog("Uploading Case Record Attachments...");
            }

            @Override
            protected String doInBackground(String... params) {

                String result = "";
                for(int i = 0; i < caseRecordAttachmentsUpload.size(); i++) {

                    Log.e("type id", caseRecordAttachmentsUpload.get(i).getAttachmentType() + "");

                    if(caseRecordAttachmentsUpload.get(i).getAttachmentType() == 1) {
                        result = uploadImageAttachment(caseRecordAttachmentsUpload.get(i));
                    } else if (caseRecordAttachmentsUpload.get(i).getAttachmentType() == 2) {
                        result = uploadVideoAttachment(caseRecordAttachmentsUpload.get(i)) ;
                    } else if (caseRecordAttachmentsUpload.get(i).getAttachmentType() == 3 ||
                            caseRecordAttachmentsUpload.get(i).getAttachmentType() == 5) {
                        result = uploadAudioAttachment(caseRecordAttachmentsUpload.get(i));
                    }

                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {

                dismissProgressDialog();

                String message = getServerMessage(s);

                if(RESULT_MESSAGE.contentEquals(message)) {

                    for(int i = 0; i < caseRecordId.size(); i++) {
                        removeCaseRecordsUpload(caseRecordId.get(i));
                    }

                    featureAlertMessage("Successfully Uploaded Case Record/s");

                } else {
                    featureAlertMessage("Failed to upload Case Record Attachments.");
                }
//                featureAlertMessage("Successfully Uploaded Case Record/s");
            }
        }

        UploadCaseRecordAttachments uploadCaseRecordAttachments = new UploadCaseRecordAttachments();
        uploadCaseRecordAttachments.execute();
    }

    private void uploadCaseRecordHistory() {

        class UploadCaseRecordHistory extends AsyncTask<String, Void, String> {

            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                showProgressDialog("Uploading Case Record History...");
            }

            @Override
            protected String doInBackground(String... params) {

                String result = "";

                for(int i = 0; i < caseRecordHistoryUpload.size(); i++) {

                    Log.d("cid", caseRecordHistoryUpload.get(i).getCaseRecordId() + "");
                    Log.d("stat", caseRecordHistoryUpload.get(i).getCaseRecordStatusId() + "");
                    Log.d("by", caseRecordHistoryUpload.get(i).getCaseRecordUpdatedBy() + "");
                    Log.d("on", caseRecordHistoryUpload.get(i).getCaseRecordUpdatedOn());

                    HashMap<String, String> data = new HashMap<>();
                    data.put(CASE_RECORD_ID_KEY, String.valueOf(caseRecordHistoryUpload.get(i).getCaseRecordId()));
                    data.put(CASE_RECORD_STATUS_ID_KEY, String.valueOf(caseRecordHistoryUpload.get(i).getCaseRecordStatusId()));
                    data.put(UPDATED_BY_KEY, String.valueOf(caseRecordHistoryUpload.get(i).getCaseRecordUpdatedBy()));
                    data.put(UPDATED_ON_KEY, caseRecordHistoryUpload.get(i).getCaseRecordUpdatedOn());
                    result = rh.sendPostRequest(DirectoryConstants.UPLOAD_CASE_RECORD_HISTORY_SERVER_SCRIPT_URL, data);

                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {

                dismissProgressDialog();

                String message = getServerMessage(s);

                if(RESULT_MESSAGE.contentEquals(message)) {
                    uploadCaseRecordAttachments();
                } else {
                    featureAlertMessage("Failed to upload Case Record History.");
                }

            }
        }

        UploadCaseRecordHistory uploadCaseRecordHistory = new UploadCaseRecordHistory();
        uploadCaseRecordHistory.execute();

    }

    private String getServerMessage(String s) {

        String result = null;

        try{

            JSONObject jsonObject = new JSONObject(s);
            result = jsonObject.getString(TAG_JSON_RESULT);

            Log.d("UploadCaseActivity", result);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String uploadImageAttachment(Attachment attachment) {

//        Log.d("upload activity", "case record id " + attachment.getCaseRecordId());

        RequestHandler rh = new RequestHandler();
        String result;

        String attachmentName = attachment.getCaseRecordId() + "_" +
                attachment.getAttachmentDescription() + ".jpg";

        String image = null;

        try {
            Bitmap bmp = ImageLoader.init().from(attachment.getAttachmentPath()).requestSize(512, 512).getBitmap();
            image = ImageBase64.encode(bmp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        HashMap<String, String> data = new HashMap<>();
        data.put(CASE_RECORD_ID_KEY, String.valueOf(attachment.getCaseRecordId()));
        data.put(ATTACHMENT_DESCRIPTION_KEY, attachment.getAttachmentDescription());
        data.put(ATTACHMENT_TYPE_ID_KEY, String.valueOf(attachment.getAttachmentType()));
        data.put(ATTACHMENT_NAME_KEY, attachmentName);
        data.put(ENCODED_IMAGE_KEY, image);
        data.put(UPLOADED_ON_KEY, attachment.getUploadedDate());
        result = rh.sendPostRequest(DirectoryConstants.UPLOAD_CASE_RECORD_IMAGE_ATTACHMENTS_SERVER_SCRIPT_URL, data);

        return result;
    }

    private String uploadVideoAttachment(Attachment attachment) {

        RequestHandler rh = new RequestHandler();
        String result;

        String attachmentName = attachment.getCaseRecordId() + "_" +
                attachment.getAttachmentDescription() + ".mp4";


        HashMap<String, String> data = new HashMap<>();
        data.put(CASE_RECORD_ID_KEY, String.valueOf(attachment.getCaseRecordId()));
        data.put(ATTACHMENT_DESCRIPTION_KEY, attachment.getAttachmentDescription());
        data.put(ATTACHMENT_TYPE_ID_KEY, String.valueOf(attachment.getAttachmentType()));
        data.put(ATTACHMENT_NAME_KEY, attachmentName);
        data.put(UPLOADED_ON_KEY, attachment.getUploadedDate());
        rh.sendPostRequest(DirectoryConstants.UPLOAD_CASE_RECORD_VIDEO_ATTACHMENTS_SERVER_SCRIPT_URL, data);

        result = rh.sendFileRequest(DirectoryConstants.UPLOAD_CASE_RECORD_VIDEO_ATTACHMENTS_SERVER_SCRIPT_URL, attachment.getAttachmentPath());

        return result;
    }

    private String uploadAudioAttachment(Attachment attachment) {

        RequestHandler rh = new RequestHandler();
        String result;

        String attachmentName = attachment.getCaseRecordId() + "_" +
                attachment.getAttachmentDescription() + ".3gp";

        HashMap<String, String> data = new HashMap<>();
        data.put(CASE_RECORD_ID_KEY, String.valueOf(attachment.getCaseRecordId()));
        data.put(ATTACHMENT_DESCRIPTION_KEY, attachment.getAttachmentDescription());
        data.put(ATTACHMENT_TYPE_ID_KEY, String.valueOf(attachment.getAttachmentType()));
        data.put(ATTACHMENT_NAME_KEY, attachmentName);
        data.put(UPLOADED_ON_KEY, attachment.getUploadedDate());
        rh.sendPostRequest(DirectoryConstants.UPLOAD_CASE_RECORD_VIDEO_ATTACHMENTS_SERVER_SCRIPT_URL, data);

        result = rh.sendFileRequest(DirectoryConstants.UPLOAD_CASE_RECORD_VIDEO_ATTACHMENTS_SERVER_SCRIPT_URL, attachment.getAttachmentPath());

        return result;
    }

    private void featureAlertMessage(String result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Status");
        builder.setMessage(result);

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }

    private void showProgressDialog(String message) {
        if(cDialog == null) {
            cDialog = new ProgressDialog(UploadCaseRecordToServerActivity.this);
            cDialog.setMessage(message);
            cDialog.setIndeterminate(true);
            cDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        cDialog.show();
    }

    private void dismissProgressDialog() {

        if(cDialog != null && cDialog.isShowing()) {
            cDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }


}
