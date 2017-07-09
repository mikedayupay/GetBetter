package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.AddInstructionsCaseFragment;
import com.dlsu.getbetter.getbetter.ClosedCaseFragment;
import com.dlsu.getbetter.getbetter.DetailsFragment;
import com.dlsu.getbetter.getbetter.DiagnosedCaseFragment;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.UrgentCaseFragment;
import com.dlsu.getbetter.getbetter.adapters.CaseRecordDownloadAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.interfaces.GetBetterClient;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.AttachmentList;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.CaseRecordList;
import com.dlsu.getbetter.getbetter.objects.Connectivity;
import com.dlsu.getbetter.getbetter.services.ServiceGenerator;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        DiagnosedCaseFragment.OnCaseRecordSelected, UrgentCaseFragment.OnCaseRecordSelected,
        ClosedCaseFragment.OnCaseRecordSelected, AddInstructionsCaseFragment.OnCaseRecordSelected {

    private SystemSessionManager systemSessionManager;
    private DataAdapter getBetterDb;
    private DetailsFragment fragment;
    private ArrayList<CaseRecord> caseRecordsData;

    private FragmentTabHost fragmentTabHost;
    private GetBetterClient getBetterClient;
    private static final String TAG = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();

        String userNameLabel = user.get(SystemSessionManager.LOGIN_USER_NAME);
        String currentHealthCenter = hc.get(SystemSessionManager.HEALTH_CENTER_NAME);
        initializeDatabase();

        TextView welcomeText = (TextView)findViewById(R.id.home_welcome_text);
        Button viewCreatePatientBtn = (Button)findViewById(R.id.view_create_patient_records_btn);
//        Button downloadAdditionalContentBtn = (Button)findViewById(R.id.download_content_btn);
        Button logoutBtn = (Button)findViewById(R.id.logout_btn);
        Button changeHealthCenterBtn = (Button)findViewById(R.id.change_health_center_btn);
        TextView healthCenter = (TextView)findViewById(R.id.home_current_health_center);

        welcomeText.append(" " + getUserName(userNameLabel) + "!");

        if (Connectivity.isConnectedFast(this)) {

            getBetterClient = ServiceGenerator.createService(GetBetterClient.class);
            getUpdatedCaseRecords();

        }

        updateFragmentTabs();

        fragmentTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
//        FragmentTabHost fragmentTabHost = new FragmentTabHost(this);
        fragmentTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);


        if (healthCenter != null) {
            healthCenter.setText(currentHealthCenter);
        }

//        TextView userLabel = (TextView)findViewById(R.id.user_label);
//        if (userLabel != null) {
//            userLabel.setText(userNameLabel);
//        }

        if (viewCreatePatientBtn != null) {
            viewCreatePatientBtn.setOnClickListener(this);
        }

//        if (downloadAdditionalContentBtn != null) {
//            downloadAdditionalContentBtn.setOnClickListener(this);
//        }

        if (changeHealthCenterBtn != null) {
            changeHealthCenterBtn.setOnClickListener(this);
        }

        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(this);
        }



    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.view_create_patient_records_btn) {

//            if (!fragment.isDetached()) {
//                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//            }
//            systemSessionManager.setHealthCenter(healthCenterName, String.valueOf(healthCenterId));
            Intent intent = new Intent(this, ExistingPatientActivity.class);
            startActivity(intent);


        }
// else if (id == R.id.download_content_btn) {
//
//            if(Connectivity.isConnectedFast(this)) {
//
////                if (!fragment.isDetached()) {
////                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
////                }
//                //            systemSessionManager.setHealthCenter(healthCenterName, String.valueOf(healthCenterId));
//                Intent intent = new Intent(this, DownloadContentActivity.class);
//                startActivity(intent);
//
//            } else {
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Feature under development.");
//                builder.setMessage("Sorry. This feature is still being fixed.");
//
//                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//
//                builder.show();
//            }
//
//        }
        else if (id == R.id.logout_btn) {

            systemSessionManager.logoutUser();

        } else if (id == R.id.change_health_center_btn) {

            Intent intent = new Intent(this, HealthCenterActivity.class);
            startActivity(intent);
            finish();

        }
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getUserName(String userEmail) {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        String username = getBetterDb.getUserName(userEmail);

        getBetterDb.closeDatabase();

        return username;
    }

    @Override
    public void onCaseRecordSelected(int caseRecordId) {

        if (findViewById(R.id.case_detail) != null) {
            Log.d("choice", caseRecordId + "");
            Bundle arguments = new Bundle();
            arguments.putInt("case record id", caseRecordId);
            fragment = new DetailsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.case_detail, fragment).commit();
        }
    }

    private void getUpdatedUserList () {

        // TODO: 06/07/2017 finish method
    }

    private void getUpdatedCaseRecords () {

        Call<CaseRecordList> call = getBetterClient.getCaseRecords();

        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(HomeActivity.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Getting data from GetBetter server...");
        progressDoalog.setTitle("GetBetter Server");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // show it
        progressDoalog.show();

        call.enqueue(new Callback<CaseRecordList>() {

            @Override
            public void onResponse(Call<CaseRecordList> call, Response<CaseRecordList> response) {
                Log.d(TAG, "contact to server successful");

                caseRecordsData = response.body().getCaseRecords();

                for (int i = 0; i < caseRecordsData.size(); i++) {

                    if (!checkIfCaseRecordExists(caseRecordsData.get(i).getCaseRecordId())) {
                        caseRecordsData.remove(i);
                    }
                }


                progressDoalog.dismiss();
                Log.d(TAG, "onResponse: " + caseRecordsData.size());

                new AsyncTask<ArrayList<CaseRecord>, Void, Void>() {

                    @Override
                    protected Void doInBackground(ArrayList<CaseRecord>... arrayLists) {

                        updateCaseRecordAdditionalNotes(arrayLists[0]);
                        updateLocalCaseRecordHistory(arrayLists[0]);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        updateFragmentTabs();
                    }
                }.execute(caseRecordsData);

                for (int i = 0; i < caseRecordsData.size(); i++) {
                    Log.d(TAG, "id: " + caseRecordsData.get(i).getCaseRecordId());
                    getNewAttachments(caseRecordsData.get(i).getCaseRecordId());
                }
            }

            @Override
            public void onFailure(Call<CaseRecordList> call, Throwable t) {
                progressDoalog.dismiss();
                Log.i(TAG, "onFailure: " + t.toString());
            }
        });
    }

    private void getNewAttachments (long caseRecordId) {

        Call<AttachmentList> call = getBetterClient.getAttachmentList(caseRecordId);


        call.enqueue(new Callback<AttachmentList>() {
            @Override
            public void onResponse(Call<AttachmentList> call, Response<AttachmentList> response) {

                Log.d(TAG, "contact to server successful");

                ArrayList<Attachment> attachments = new ArrayList<Attachment>();
                attachments = response.body().getAttachments();
                Log.d(TAG, "onResponse: " + attachments.size());

                for (int i = 0; i < attachments.size(); i++) {

                    if (!checkIfAttachmentExists(attachments.get(i).getCaseRecordId(),
                            attachments.get(i).getAttachmentDescription(), attachments.get(i).getUploadedDate())) {

                        insertNewAttachments(attachments.get(i));

                    }

                }
            }

            @Override
            public void onFailure(Call<AttachmentList> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.toString());
            }
        });
    }

    private boolean checkIfCaseRecordExists (int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        boolean result = getBetterDb.checkIfCaseRecordExists(caseRecordId);

        getBetterDb.closeDatabase();

        return result;
    }

    private boolean checkIfAttachmentExists (int caseRecordId, String description, String uploadedOn) {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        boolean result = getBetterDb.checkIfAttachmentExists(caseRecordId, description, uploadedOn);

        getBetterDb.closeDatabase();

        return result;
    }

    private void updateCaseRecordAdditionalNotes(ArrayList<CaseRecord> caseRecords) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.insertAdditionalNotes(caseRecords);

        getBetterDb.closeDatabase();
    }

    private void updateLocalCaseRecordHistory(ArrayList<CaseRecord> caseRecords) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.updateCaseRecordHistory(caseRecords);

        getBetterDb.closeDatabase();
    }

    private void insertNewAttachments (Attachment attachment) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long id;
        id = getBetterDb.insertNewCaseRecordAttachments(attachment);

        Log.d(TAG, "insertNewAttachments: " + id);

        getBetterDb.closeDatabase();
    }

    private void updateFragmentTabs() {

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("urgent").setIndicator("Urgent Cases"),
                UrgentCaseFragment.class, null);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("additional instructions").setIndicator("Cases w/ Additional Instructions"),
                AddInstructionsCaseFragment.class, null);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("diagnosed").setIndicator("Diagnosed Cases"),
                DiagnosedCaseFragment.class, null);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("closed").setIndicator("Closed Cases"),
                ClosedCaseFragment.class, null);

    }


}
