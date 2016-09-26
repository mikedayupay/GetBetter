package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.UpdatePatientRecordActivity;
import com.dlsu.getbetter.getbetter.UploadPatientToServerActivity;
import com.dlsu.getbetter.getbetter.adapters.ExistingPatientAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


public class ExistingPatientActivity extends AppCompatActivity implements View.OnClickListener {

    private DataAdapter getBetterDb;

    private Long selectedPatientId;
    private String patientFirstName;
    private String patientLastName;

    private ArrayList<Patient> existingPatients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_patient);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        int healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        Button newPatientRecBtn = (Button) findViewById(R.id.create_new_patient_btn);
        Button uploadPatientRecBtn = (Button) findViewById(R.id.upload_patient_record);
        Button backBtn = (Button)findViewById(R.id.existing_patient_back_btn);

        RecyclerView existingPatientListView = (RecyclerView) findViewById(R.id.existing_patient_list);
        RecyclerView.LayoutManager existingPatientLayoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this);

        existingPatients = new ArrayList<>();
        initializeDatabase();
        new GetPatientListTask().execute(healthCenterId);
        Log.e("patient size", existingPatients.size() + "");
        ExistingPatientAdapter existingPatientsAdapter = new ExistingPatientAdapter(existingPatients);

        existingPatientListView.setHasFixedSize(true);
        existingPatientListView.setLayoutManager(existingPatientLayoutManager);
        existingPatientListView.setAdapter(existingPatientsAdapter);
        existingPatientListView.addItemDecoration(dividerItemDecoration);
        existingPatientsAdapter.SetOnItemClickListener(new ExistingPatientAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                selectedPatientId = existingPatients.get(position).getId();
                Intent intent = new Intent(ExistingPatientActivity.this, ViewPatientActivity.class);
                intent.putExtra("patientId", selectedPatientId);
                startActivity(intent);
                finish();

            }
        });


        newPatientRecBtn.setOnClickListener(this);
        uploadPatientRecBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void getExistingPatients(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        existingPatients.addAll(getBetterDb.getPatients(healthCenterId));

        getBetterDb.closeDatabase();

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.create_new_patient_btn) {

            Intent intent = new Intent(this, NewPatientRecordActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.upload_patient_record) {

            Intent intent = new Intent(this, UploadPatientToServerActivity.class);
            startActivity(intent);

        } else if (id == R.id.existing_patient_back_btn) {
            finish();
        }
    }

    private class GetPatientListTask extends AsyncTask<Integer, Void, String> {

        private ProgressDialog progressDialog = new ProgressDialog(ExistingPatientActivity.this);

        @Override
        protected void onPreExecute () {
            super.onPreExecute();
            progressDialog.setMessage("Populating Patient List...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {

            getExistingPatients(params[0]);

            return "Done!";
        }

        @Override
        protected void onPostExecute (String results) {

            super.onPostExecute(results);
            progressDialog.hide();
            progressDialog.dismiss();
        }
    }


}
