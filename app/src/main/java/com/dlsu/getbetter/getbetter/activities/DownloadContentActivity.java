package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.CaseRecordDownloadAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.interfaces.GetBetterClient;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.AttachmentList;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.CaseRecordList;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.services.ServiceGenerator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadContentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG_CASE_RECORD = "case_records";
    private static final String TAG_CASE_ATTACHMENTS = "case_attachments";
    private static final String TAG_CASE_RECORD_ID = "case_record_id";
    private static final String TAG_USER_ID = "user_id";
    private static final String TAG_CONTROL_NUMBER = "control_number";
    private static final String TAG_COMPLAINT = "complaint";
    private static final String TAG_ADDITIONAL_NOTES = "additional_notes";
    private static final String TAG_HEALTH_CENTER_ID = "health_center_id";
    private static final String TAG_RECORD_STATUS_ID = "record_status_id";
    private static final String TAG_UPDATED_ON = "updated_on";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_FILE_PATH = "file_path";
    private static final String TAG_CASE_ATTACHMENT_TYPE = "case_attachment_type";
    private static final String TAG_UPLOADED_ON = "uploaded_on";
    private static final String TAG_UPLOADED_BY = "uploaded_by";
    private static final String TAG = "downloadactivity";

    private String myJSON;
    private String myJSONAttachments;
    private int index, total;
    private CaseRecord selectedCaseRecord;
    private ArrayList<CaseRecord> caseRecordsData;
    private ArrayList<Long> patientIds;
    private ArrayList<Attachment> attachments;

    private DataAdapter getBetterDb;
    private ProgressDialog dDialog = null;
    private RecyclerView recyclerView;

    private GetBetterClient getBetterClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_content);

        Button backBtn = (Button)findViewById(R.id.download_back_btn);
        Button downloadBtn = (Button)findViewById(R.id.download_selected_btn);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        RecyclerView.LayoutManager fileListLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView)findViewById(R.id.download_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(fileListLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        backBtn.setOnClickListener(this);
        downloadBtn.setOnClickListener(this);


        caseRecordsData = new ArrayList<>();

        ArrayList<Attachment> caseRecordAttachments = new ArrayList<>();
        getBetterClient = ServiceGenerator.createService(GetBetterClient.class);

        initializeDatabase();
        getPatientIds();
        getUpdatedCaseRecordList();
//        getDownloadList();
//        getDownloadableData();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.download_back_btn) {
            finish();
        } else if (id == R.id.download_selected_btn) {

            updateCaseRecord(selectedCaseRecord);
//            updateCaseRecordAdditionalNotes(selectedCaseRecordList);
//            updateLocalCaseRecordHistory(selectedCaseRecordList);
//            downloadSelectedData(selectedCaseRecordList);
        }
    }

    private void getUpdatedCaseRecordList () {

        Call<CaseRecordList> call = getBetterClient.getCaseRecords();

        call.enqueue(new Callback<CaseRecordList>() {

            @Override
            public void onResponse(Call<CaseRecordList> call, Response<CaseRecordList> response) {
                Log.d(TAG, "contact to server successful");

                caseRecordsData = response.body().getCaseRecords();
                CaseRecordDownloadAdapter caseRecordDownloadAdapter = new CaseRecordDownloadAdapter(caseRecordsData);
                recyclerView.setAdapter(caseRecordDownloadAdapter);
                caseRecordDownloadAdapter.SetOnItemClickListener(new CaseRecordDownloadAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        selectedCaseRecord = caseRecordsData.get(position);
                        Log.d(TAG, "onItemClick: " + selectedCaseRecord.getCaseRecordStatus());

                    }
                });
            }
            @Override
            public void onFailure(Call<CaseRecordList> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.toString());
            }
        });
    }

    private void getAttachmentData (long caseRecordId) {

        Call<AttachmentList> call = getBetterClient.getAttachmentList(caseRecordId);

        call.enqueue(new Callback<AttachmentList>() {

            @Override
            public void onResponse(Call<AttachmentList> call, Response<AttachmentList> response) {
                Log.d(TAG, "contact to server successful");
                attachments = response.body().getAttachments();

            }

            @Override
            public void onFailure(Call<AttachmentList> call, Throwable t) {
                Log.d(TAG, "failed to connect to server");

            }
        });

    }

    private void downloadAttachmentFile (String url, final String description, final String uploadedOn, final int attachmentType) {

        Call<ResponseBody> call = getBetterClient.downloadAttachmentFile(url);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d(TAG, "contact to server successful");

                boolean writtenToDisk = writeResponseBodyToDisk(response.body(), description, uploadedOn, attachmentType);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "failed to connect to server");
            }
        });

    }

    private boolean writeResponseBodyToDisk (ResponseBody body, String description, String uploadedOn, int attachmentType) {

        try {
            File attachmentFile = createAttachmentFile(description, uploadedOn, attachmentType);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(attachmentFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if(read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file downloaded: " + fileSizeDownloaded + " of" + fileSize);

                }

                outputStream.flush();

                return true;

            } catch (IOException e) {

                return false;

            } finally {
                if(inputStream != null) {
                    inputStream.close();
                }

                if(outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void getDownloadList() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("patientIds", patientIds);
        client.get(DirectoryConstants.DOWNLOAD_CASE_RECORD_LIST_URL, params, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                myJSON = responseString;
                populateCaseRecordsList();
            }
        });
    }

    private void populateCaseRecordsList() {

        try {
            JSONObject jsonObject = new JSONObject(myJSON);
            JSONArray caseRecords = jsonObject.getJSONArray(TAG_CASE_RECORD);

            for(int i = 0; i < caseRecords.length(); i++) {
                JSONObject c = caseRecords.getJSONObject(i);
                int caseRecordId = Integer.parseInt(c.getString(TAG_CASE_RECORD_ID));
                String controlNumber = c.getString(TAG_CONTROL_NUMBER);
                String patientName = getUserName(Integer.parseInt(c.getString(TAG_USER_ID)));
                String complaint = c.getString(TAG_COMPLAINT);
                String additionalNotes = c.getString(TAG_ADDITIONAL_NOTES);
                String healthCenter = getHealthCenterName(Integer.parseInt(c.getString(TAG_HEALTH_CENTER_ID)));
                String recordStatus = getCaseRecordStatusString(Integer.parseInt(c.getString(TAG_RECORD_STATUS_ID)));
                String updatedOn = c.getString(TAG_UPDATED_ON);
                //// TODO: 03/02/2017 add doctors name

                CaseRecord caseRecord = new CaseRecord(caseRecordId, patientName, controlNumber,
                        complaint, additionalNotes, healthCenter, recordStatus, updatedOn);
                caseRecord.setCaseRecordStatusId(Integer.parseInt(c.getString(TAG_RECORD_STATUS_ID)));
                caseRecordsData.add(caseRecord);

                Log.e("case records data", caseRecordsData.size() + "");
            }

            CaseRecordDownloadAdapter caseRecordDownloadAdapter = new CaseRecordDownloadAdapter(caseRecordsData);
            recyclerView.setAdapter(caseRecordDownloadAdapter);
            caseRecordDownloadAdapter.SetOnItemClickListener(new CaseRecordDownloadAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    selectedCaseRecord = caseRecordsData.get(position);
                    Log.d(TAG, "onItemClick: " + selectedCaseRecord.getCaseRecordStatus());

                }
            });

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateAttachmentList() {

        attachments = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(myJSON);
            JSONArray caseAttachments = jsonObject.getJSONArray(TAG_CASE_ATTACHMENTS);

            for (int i = 0; i < caseAttachments.length(); i++) {
                JSONObject c = caseAttachments.getJSONObject(i);
                int caseRecordId = Integer.parseInt(c.getString(TAG_CASE_RECORD_ID));
                String description = c.getString(TAG_DESCRIPTION);
                String filePath = c.getString(TAG_FILE_PATH);
                int caseAttachmentTypeId = Integer.parseInt(c.getString(TAG_CASE_ATTACHMENT_TYPE));
                String uploadedOn = c.getString(TAG_UPLOADED_ON);

//                Log.d(TAG, "populateAttachmentList: " + caseRecordId);
//                Log.d(TAG, "populateAttachmentList: " + description);
//                Log.d(TAG, "populateAttachmentList: " + filePath);
//                Log.d(TAG, "populateAttachmentList: " + uploadedOn);
//                Log.d(TAG, "populateAttachmentList: " + caseAttachmentTypeId);
                Attachment attachment = new Attachment(caseRecordId, filePath, description, caseAttachmentTypeId, uploadedOn);
                attachments.add(attachment);
//                Log.d(TAG, "populateAttachmentList: " + attachments.size());
            }

            downloadAttachment(attachments);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateCaseRecord(CaseRecord caseRecord) {

        class UpdateCaseRecords extends AsyncTask<CaseRecord, Integer, Integer> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog("Updating Case Record");
            }

            @Override
            protected Integer doInBackground(CaseRecord... caseRecord) {

                try {
                    getBetterDb.openDatabase();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                updateLocalCaseRecordHistory(caseRecord[0]);
                updateCaseRecordAdditionalNotes(caseRecord[0]);


                return null;
            }

            @Override
            protected void onPostExecute(Integer s) {
                super.onPostExecute(s);
                dismissProgressDialog();
                getAttachmentdata(selectedCaseRecord.getCaseRecordId());
            }
        }
        new UpdateCaseRecords().execute(caseRecord);
    }

    private void getAttachmentdata(int caseRecordId) {

        Log.d(TAG, "getAttachmentdata: method fire " + caseRecordId);
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put(TAG_CASE_RECORD_ID, caseRecordId);

        httpClient.post(this, DirectoryConstants.DOWNLOAD_CASE_RECORD_NEW_ATTACHMENTS_SERVER_SCRIPT_URL, params, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "onFailure: failed to get");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, "onSuccess: " + responseString);
                myJSON = responseString;
                populateAttachmentList();
            }
        });
    }

    private void downloadAttachment(ArrayList<Attachment> attachment) {

        for (int i = 0; i < attachment.size(); i++) {
            AsyncHttpClient fileClient = new AsyncHttpClient();
            fileClient.addHeader("Connection", "Keep-Alive");

            String uploadedOn = attachment.get(i).getUploadedDate();
            int attachmentType = attachment.get(i).getAttachmentType();
            String attachmentDescription = attachment.get(i).getAttachmentDescription();

            File destinationFile = createAttachmentFile(attachmentDescription, uploadedOn, attachmentType);


            if (destinationFile != null) {
                fileClient.get(attachment.get(i).getAttachmentPath(), new FileAsyncHttpResponseHandler(destinationFile, false, true) {

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                        Log.d(TAG, "onFailure: " + statusCode + file.getName());
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, File file) {

                    }
                });
            }
        }
    }

    /**
    * ALL METHODS BELOW ARE
    * DATABASE MANIPULATION
    */

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getPatientIds() {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        patientIds = getBetterDb.getPatientIds();
        getBetterDb.closeDatabase();

    }

    private String getHealthCenterName(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result;
        result = getBetterDb.getHealthCenterString(healthCenterId);

        getBetterDb.closeDatabase();

        return result;
    }

    private String getCaseRecordStatusString(int caseRecordStatusId) {

        String result = null;
        Resources res = getResources();
        String[] caseRecordStatuses = res.getStringArray(R.array.record_status);

        switch (caseRecordStatusId) {

            case 1: result = caseRecordStatuses[0];
                break;

            case 2: result = caseRecordStatuses[1];
                break;

            case 3: result = caseRecordStatuses[2];
                break;

            case 4: result = caseRecordStatuses[3];
                break;

            case 5: result = caseRecordStatuses[4];
                break;

            case 6: result = caseRecordStatuses[5];
                break;

            case 7: result = caseRecordStatuses[6];
                break;

        }

        return result;
    }

    private String getUserName(int userId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result;

        try {
            result = getBetterDb.getPatientName(userId);
        } catch (NullPointerException e) {
            e.printStackTrace();
            result = "New Patient";
        }

        getBetterDb.closeDatabase();
        return result;
    }

    private void updateCaseRecordAdditionalNotes(CaseRecord selectedCaseRecord) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.updateCaseRecordAdditionalNotes(selectedCaseRecord.getCaseRecordId(),
                selectedCaseRecord.getCaseRecordAdditionalNotes());

        getBetterDb.closeDatabase();
    }

    private void updateLocalCaseRecordHistory(CaseRecord selectedCaseRecord) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.updateLocalCaseRecordHistory(selectedCaseRecord);


        getBetterDb.closeDatabase();
    }

    private void insertCaseAttachment(Attachment attachment) {

        try{
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.insertCaseRecordAttachments(attachment);

        getBetterDb.closeDatabase();
    }

    private File createAttachmentFile(String description, String uploaded_on, int attachmentType) {

        File attachmentFile = null;

        File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Debug", "Oops! Failed create "
                        + DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        if(attachmentType == 1) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + "_" + uploaded_on + ".jpg");

        } else if (attachmentType == 3 || attachmentType == 5) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + "_" + uploaded_on + ".3gp");

        } else if (attachmentType == 2) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + "_" + uploaded_on + ".mp4");
        }

        return attachmentFile;
    }

    /**
     * BELOW ARE THE
     * DIALOG AND ALERT MESSAGE METHODS
     */

    private void featureAlertMessage(String result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download Status");
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

    private void showDownloadingDialog() {
        if(dDialog == null) {

            dDialog = new ProgressDialog(DownloadContentActivity.this);
            dDialog.setTitle("Downloading Attachment");
            dDialog.setMessage("Downloading " + index + " out of " + total + "...");
            dDialog.setIndeterminate(false);
            dDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        }
        dDialog.show();
    }

    private void showProgressDialog(String message) {

        if(dDialog == null) {

            dDialog = new ProgressDialog(DownloadContentActivity.this);
            dDialog.setTitle("Download Status");
            dDialog.setMessage(message);
            dDialog.setIndeterminate(true);
            dDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        }
        dDialog.show();
    }

    private void dismissProgressDialog() {

        if(dDialog != null && dDialog.isShowing()) {
            dDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }


}
