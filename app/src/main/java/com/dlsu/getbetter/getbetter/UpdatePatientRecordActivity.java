package com.dlsu.getbetter.getbetter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.activities.ExistingPatientActivity;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Patient;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class UpdatePatientRecordActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private DataAdapter getBetterDb;
    private EditText firstNameInput;
    private EditText middleNameInput;
    private EditText lastNameInput;
    private TextView displayBirthday;
    private ImageView setProfilePicBtn;
    private int year, month, day;
    private long patientId;
    private String birthDate;
    private String genderSelected;
    private String civilStatusSelected;
    private String profilePicTitle;
    private String profilePicPath;

    private static final int REQUEST_IMAGE1 = 100;

    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_new_patient);

        Bundle extras = getIntent().getExtras();
        patientId = 0;

        if(extras != null) {
            patientId = extras.getLong("selectedPatient");
        }

        Log.e("patient id", patientId + "");

        initializeDatabase();
        Patient patient = getPatientInfo(patientId);

        Button submitBtn = (Button)findViewById(R.id.new_patient_next_btn);
        Button setBirthday = (Button)findViewById(R.id.new_patient_set_birthday_btn);
        Spinner genderSpinner = (Spinner)findViewById(R.id.gender_spinner);
        Spinner civilStatusSpinner = (Spinner)findViewById(R.id.civil_status_spinner);
        firstNameInput = (EditText)findViewById(R.id.first_name_input);
        middleNameInput = (EditText)findViewById(R.id.middle_name_input);
        lastNameInput = (EditText)findViewById(R.id.last_name_input);
        setProfilePicBtn = (ImageView)findViewById(R.id.profile_picture_select);
        displayBirthday = (TextView)findViewById(R.id.display_birthday);
        TextView profilePicPlaceHolder = (TextView) findViewById(R.id.profile_picture_select_placeholder);

        firstNameInput.setText(patient.getFirstName());
        middleNameInput.setText(patient.getMiddleName());
        lastNameInput.setText(patient.getLastName());
        profilePicPath = patient.getProfileImageBytes();
        setPic(setProfilePicBtn, patient.getProfileImageBytes());

        if (profilePicPlaceHolder != null) {
            profilePicPlaceHolder.setText("");
        }

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> civilStatusAdapter = ArrayAdapter.createFromResource(this,
                R.array.civil_statuses, android.R.layout.simple_spinner_item);

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        civilStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (genderSpinner != null) {
            genderSpinner.setAdapter(genderAdapter);
        }
        if (civilStatusSpinner != null) {
            civilStatusSpinner.setAdapter(civilStatusAdapter);
        }

        if (submitBtn != null) {
            submitBtn.setText(R.string.submit_button_text);
        }

        if (submitBtn != null) {
            submitBtn.setOnClickListener(this);
        }

        if (setBirthday != null) {
            setBirthday.setOnClickListener(this);
        }

        if (genderSpinner != null) {
            genderSpinner.setOnItemSelectedListener(this);
        }

        if (civilStatusSpinner != null) {
            civilStatusSpinner.setOnItemSelectedListener(this);
        }

        setProfilePicBtn.setOnClickListener(this);

        if(patient.getBirthdate() != null) {

            StringTokenizer token = new StringTokenizer(patient.getBirthdate(), "-");
            year = Integer.parseInt(token.nextElement().toString());
            month = Integer.parseInt(token.nextElement().toString());
            day = Integer.parseInt(token.nextElement().toString());
        }

        showDate(year, month, day);


    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.new_patient_next_btn) {

            savePatientInfo();
            Intent intent = new Intent(this, ExistingPatientActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.profile_picture_select) {

            takePicture();

        } else if (id == R.id.new_patient_set_birthday_btn) {

            showDialog(999);
        } else if (id == R.id.newpatient_fragment_back_btn) {

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

    private Patient getPatientInfo(long patientId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Patient patient = getBetterDb.getPatient(patientId);

        getBetterDb.closeDatabase();

        return patient;

    }
    private void showDate (int year, int month, int day) {
        String sMonth = month + "";
        String sDay = day + "";

        if(month < 10) {
            sMonth = "0" + sMonth;
        }

        if(day < 10) {
            sDay = "0" + sDay;
        }

        displayBirthday.setText(new StringBuilder().append(year).append("-")
                .append(sMonth).append("-").append(sDay));
    }

    private void savePatientInfo() {

        String firstName = firstNameInput.getText().toString();
        String middleName = middleNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();

        Patient newPatient = new Patient(firstName, middleName, lastName,
                birthDate, genderSelected, civilStatusSelected, profilePicPath);

        Log.d("First Name", newPatient.getFirstName());
        Log.d("Middle Name", newPatient.getMiddleName());
        Log.d("Last Name", newPatient.getLastName());
        Log.d("Gender", newPatient.getGender());
        Log.d("Civil Status", newPatient.getCivilStatus());

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.updatePatientInfo(newPatient, patientId);
        getBetterDb.closeDatabase();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch(parent.getId()) {
            case R.id.gender_spinner:
                genderSelected = (parent.getItemAtPosition(position)).toString();
                break;

            case R.id.civil_status_spinner:
                civilStatusSelected = (parent.getItemAtPosition(position)).toString();
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        switch(parent.getId()) {
            case R.id.gender_spinner:
                genderSelected = (parent.getSelectedItem()).toString();
                break;

            case R.id.civil_status_spinner:
                civilStatusSelected = (parent.getSelectedItem()).toString();
                break;

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // arg1 = year
            // arg2 = month
            // arg3 = day
            showDate(arg1, arg2+1, arg3);
            arg2 += 1;
            String month = arg2 + "";
            String day = arg3 + "";

            if(arg2 < 10) {
                month = "0" + arg2;
            }

            if(arg3 < 10) {
                day = "0" + arg3;
            }

            birthDate = arg1 + "-" + month + "-" + day;
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_IMAGE1 && resultCode == Activity.RESULT_OK) {

            setPic(setProfilePicBtn, fileUri.getPath());

//            Bitmap photo = (Bitmap)data.getExtras().get("data");
//            setProfilePicBtn.setImageBitmap(photo);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            if (photo != null) {
//                photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
//            }
//            byte[] b = baos.toByteArray();
//
//            encoded = Base64.encodeToString(b, Base64.DEFAULT);
//
//
//            Log.d("image byte", encoded + "");

        }
    }

    private File createImageFile() {

        File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                DirectoryConstants.PROFILE_IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Debug", "Oops! Failed create "
                        + DirectoryConstants.PROFILE_IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }


        return new File (mediaStorageDir.getPath() + File.pathSeparator + "ProfileIMG_" + getTimeStamp() + ".jpg");
    }

    private void takePicture() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = Uri.fromFile(createImageFile());

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_IMAGE1);

    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 255; //mImageView.getWidth();
        int targetH = 138; //mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
