package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.CaseRecordUploadAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class UploadCaseRecordToServerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UPLOADCASERECORD";

    private static final String CASE_RECORD_ID_KEY = "case_record_id";
    private static final String CASE_TYPE_ID_KEY = "case_id";
    private static final String USER_ID_KEY = "user_id";
    private static final String HEALTH_CENTER_ID_KEY = "health_center_id";
    private static final String COMPLAINT_KEY = "complaint";
    private static final String CONTROL_NUMBER_KEY = "control_number";
    private static final String ADDITIONAL_NOTES_KEY = "additional_notes";
    private static final String UPDATED_BY_KEY = "updated_by";
    private static final String UPDATED_ON_KEY = "updated_on";

    private static final String ATTACHMENT_DESCRIPTION_KEY = "description";
    private static final String ATTACHMENT_NAME_KEY = "attachment_name";
    private static final String ATTACHMENT_TYPE_ID_KEY = "case_record_attachment_type_id";
    private static final String UPLOADED_ON_KEY = "uploaded_on";

    private static final int TIMEOUT_VALUE = 60 * 1000;
    private int index = 0;
    private int numberOfAttachments = 0;
    private int newCaseRecordId;

    private ArrayList<CaseRecord> caseRecordsUpload = null;
    private ArrayList<Attachment> caseRecordAttachments = null;
    private CaseRecord selectedCaseRecord;
    private int userId;
    private int healthCenterId;
    private ProgressDialog pDialog = null;
    private ProgressDialog uDialog = null;

    private DataAdapter getBetterDb;
    private Handler handler;

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

        caseRecordsUpload = new ArrayList<>();

        Button backBtn = (Button)findViewById(R.id.upload_caserecord_back_btn);
        Button uploadBtn = (Button)findViewById(R.id.upload_caserecord_upload_btn);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        RecyclerView.LayoutManager fileListLayoutManager = new LinearLayoutManager(this);
        RecyclerView caseRecordList = (RecyclerView)findViewById(R.id.upload_page_case_record_list);
        caseRecordList.setHasFixedSize(true);
        caseRecordList.setLayoutManager(fileListLayoutManager);
        caseRecordList.addItemDecoration(dividerItemDecoration);

        uploadBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        initializeDatabase();
        userId = getUserId(user.get(SystemSessionManager.LOGIN_USER_NAME));
        Log.d(TAG, "onCreate: " + userId);
        new PopulateCaseRecordListTask().execute();
        CaseRecordUploadAdapter caseRecordUploadAdapter = new CaseRecordUploadAdapter(caseRecordsUpload);
        caseRecordList.setAdapter(caseRecordUploadAdapter);
        caseRecordUploadAdapter.SetOnItemClickListener(new CaseRecordUploadAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectedCaseRecord = caseRecordsUpload.get(position);
            }
        });
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

        caseRecordsUpload.addAll(getBetterDb.getCaseRecordsUpload());
        for(int i = 0; i < caseRecordsUpload.size(); i++) {
            String patientName = getPatientName(caseRecordsUpload.get(i).getUserId());
            caseRecordsUpload.get(i).setPatientName(patientName);
        }

        getBetterDb.closeDatabase();

    }

    private String getPatientName (int userId) {

        String patientName;

        patientName = getBetterDb.getPatientName(userId);

        return patientName;

    }

    private int getUserId (String username) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int id = getBetterDb.getUserId(username);

        getBetterDb.closeDatabase();

        return id;

    }

    private ArrayList<Attachment> getCaseRecordAttachments(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<Attachment> caseAttachments = new ArrayList<>();
        caseAttachments.addAll(getBetterDb.getAttachments(caseRecordId));

        getBetterDb.closeDatabase();

        return caseAttachments;
    }

    private void updateCaseRecordId(int newCaseRecordId, int oldCaseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.updateCaseRecordId(newCaseRecordId, oldCaseRecordId);
        getBetterDb.closeDatabase();
    }

    private void updateCaseRecord(int updatedCaseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.updateCaseRecordUploaded(updatedCaseRecordId);
        getBetterDb.closeDatabase();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.upload_caserecord_back_btn) {

            Intent intent = new Intent(this, ExistingPatientActivity.class);
            startActivity(intent);
            finish();

        } else if(id == R.id.upload_caserecord_upload_btn) {

            if(selectedCaseRecord != null){
                caseRecordAttachments = getCaseRecordAttachments(selectedCaseRecord.getCaseRecordId());
                uploadCaseRecord(selectedCaseRecord);
//                uploadAttachment(getCaseRecordAttachments(selectedCaseRecord.getCaseRecordId()));
            } else {
                Toast.makeText(this, "Please select a case record to upload.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PopulateCaseRecordListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            showPopulateProgressDialog();

        }

        @Override
        protected Void doInBackground(Void... params) {
            getCaseRecordsUpload();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onCreate: " + caseRecordsUpload.size());
            dismissProgressDialog();
        }
    }

    private void uploadCaseRecord(CaseRecord caseRecordsUpload) {

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setTimeout(TIMEOUT_VALUE);
        RequestParams params = new RequestParams();

        String caseRecordId = String.valueOf(caseRecordsUpload.getCaseRecordId());

        Map<String, String> record = new HashMap<>();
//        record.put(CASE_RECORD_ID_KEY, caseRecordId);
        record.put(CASE_TYPE_ID_KEY, String.valueOf(caseRecordsUpload.getCaseId()));
        record.put(USER_ID_KEY, String.valueOf(caseRecordsUpload.getUserId()));
        record.put(HEALTH_CENTER_ID_KEY, String.valueOf(healthCenterId));
        record.put(COMPLAINT_KEY, caseRecordsUpload.getCaseRecordComplaint());
        record.put(CONTROL_NUMBER_KEY, caseRecordsUpload.getCaseRecordControlNumber());
        record.put(ADDITIONAL_NOTES_KEY, caseRecordsUpload.getCaseRecordAdditionalNotes());
        record.put(UPDATED_BY_KEY, String.valueOf(userId));
        // TODO: 09/03/2017 fix updated_by error

        params.put("case_record", record);
        params.setUseJsonStreamer(false);

        asyncHttpClient.post(UploadCaseRecordToServerActivity.this,
                DirectoryConstants.TEST_URL_POST, params, new TextHttpResponseHandler() {

                    @Override
                    public void onStart() {

                        super.onStart();
                        showProgressDialog();
                        uDialog.setMessage("Uploading Case Record...");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.d(TAG, "onFailure: " + responseString);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {

                        Log.d(TAG, responseString);
                        newCaseRecordId = Integer.parseInt(responseString);
                        updateCaseRecord(selectedCaseRecord.getCaseRecordId());
                        updateCaseRecordId(newCaseRecordId, selectedCaseRecord.getCaseRecordId());
//                        removeCaseRecordsUpload(caseRecordsUpload.getCaseRecordId());
                    }

                    @Override
                    public void onFinish() {

                        super.onFinish();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                caseRecordAttachments = getCaseRecordAttachments(newCaseRecordId);
                                numberOfAttachments = caseRecordAttachments.size();
                                uploadAttachment();
                            }
                        }, 10000);
                    }
                });

    }

    private void uploadAttachment() {

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setTimeout(TIMEOUT_VALUE);
        RequestParams params = new RequestParams();
        final String contentType = RequestParams.APPLICATION_OCTET_STREAM;

        Map<String, String> attachment = new HashMap<>();
        attachment.put(CASE_RECORD_ID_KEY, String.valueOf(newCaseRecordId));
        attachment.put(USER_ID_KEY, String.valueOf(selectedCaseRecord.getUserId()));
        attachment.put(ATTACHMENT_DESCRIPTION_KEY, caseRecordAttachments.get(index).getAttachmentDescription());
        attachment.put(ATTACHMENT_TYPE_ID_KEY, String.valueOf(caseRecordAttachments.get(index).getAttachmentType()));
        params.put("attachment", attachment);
        File attachmentFile = new File(caseRecordAttachments.get(index).getAttachmentPath());

        try {
            params.put("attachmentFile", attachmentFile, contentType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        asyncHttpClient.post(this, DirectoryConstants.ATTACHMENT_POST_URL, params, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                uDialog.setMessage("Uploading " + caseRecordAttachments.get(index).getAttachmentDescription() + "...");
                showProgressDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "onFailure: " + responseString + " " + statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {


                featureAlertMessage("UPLOAD SUCCESS");
                Log.d(TAG, "onSuccess");
                Log.d(TAG, "onSuccess: " + responseString);

                index++;
                if(index < numberOfAttachments) {
                    uploadAttachment();
                }
            }

            @Override
            public void onFinish() {
                dismissProgressDialog();
            }
        });
    }

    private void featureAlertMessage(String result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("STATUS");
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

    Runnable UpdateRecordTime = new Runnable() {
        @Override
        public void run() {

            handler.postDelayed(this, 1000);
        }
    };

    private void showPopulateProgressDialog() {
        if(pDialog == null) {
            pDialog = new ProgressDialog(UploadCaseRecordToServerActivity.this);
            pDialog.setTitle("Populating Case Record List");
            pDialog.setMessage("Please wait a moment...");
            pDialog.setIndeterminate(true);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        pDialog.show();
    }

    private void showProgressDialog() {

        if(uDialog == null) {
            uDialog = new ProgressDialog(UploadCaseRecordToServerActivity.this);
            uDialog.setTitle("GetBetter Server");
//            uDialog.setProgress(0);
//            uDialog.incrementProgressBy(1);
            uDialog.setIndeterminate(true);
            uDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        uDialog.show();
    }

    private void dismissProgressDialog() {

        if(pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }

        if (uDialog != null && uDialog.isShowing()) {
            uDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

}