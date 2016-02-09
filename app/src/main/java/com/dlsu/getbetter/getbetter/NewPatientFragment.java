package com.dlsu.getbetter.getbetter;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.NewPatientSessionManager;


import java.io.ByteArrayOutputStream;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewPatientFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText firstNameInput;
    private EditText middleNameInput;
    private EditText lastNameInput;
    private TextView displayBirthday;
    private Spinner genderSpinner;
    private Spinner civilStatusSpinner;
    private ImageView setProfilePicBtn;
    private TextView profilePicPlaceholder;

    private int year, month, day;
    private String birthDate;
    private String genderSelected;
    private String civilStatusSelected;
    private String encoded;

    private ArrayAdapter<CharSequence> genderAdapter;
    private ArrayAdapter<CharSequence> civilStatusAdapter;

    private static final int REQUEST_IMAGE1 = 100;

    NewPatientSessionManager newPatientSessionManager;



    public NewPatientFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        birthDate = year + "-" + month + "-" + day;

        newPatientSessionManager = new NewPatientSessionManager(this.getContext());

        genderAdapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.genders, android.R.layout.simple_spinner_item);

        civilStatusAdapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.civil_statuses, android.R.layout.simple_spinner_item);

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        civilStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView =  inflater.inflate(R.layout.fragment_new_patient, container, false);

        Button setBirthday = (Button)rootView.findViewById(R.id.new_patient_set_birthday_btn);
        Button nextButton = (Button)rootView.findViewById(R.id.new_patient_next_btn);

        setProfilePicBtn = (ImageView)rootView.findViewById(R.id.profile_picture_select);

        firstNameInput = (EditText)rootView.findViewById(R.id.first_name_input);
        middleNameInput = (EditText)rootView.findViewById(R.id.middle_name_input);
        lastNameInput = (EditText)rootView.findViewById(R.id.last_name_input);
        profilePicPlaceholder = (TextView)rootView.findViewById(R.id.profile_picture_select_placeholder);


        genderSpinner = (Spinner)rootView.findViewById(R.id.gender_spinner);
        civilStatusSpinner = (Spinner)rootView.findViewById(R.id.civil_status_spinner);

        displayBirthday = (TextView)rootView.findViewById(R.id.display_birthday);

        genderSpinner.setAdapter(genderAdapter);
        civilStatusSpinner.setAdapter(civilStatusAdapter);

        nextButton.setOnClickListener(this);
        genderSpinner.setOnItemSelectedListener(this);
        civilStatusSpinner.setOnItemSelectedListener(this);
        setBirthday.setOnClickListener(this);
        setProfilePicBtn.setOnClickListener(this);

        showDate(year, month, day);


        return rootView;
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.new_patient_next_btn) {

            savePatientInfo();

            CaptureDocumentsFragment captureDocumentsFragment = new CaptureDocumentsFragment();
            getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).commit();
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, captureDocumentsFragment).commit();

        } else if(id == R.id.new_patient_set_birthday_btn) {
            showPicker();
            Log.d("set", "Working");

        } else if (id == R.id.profile_picture_select) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE1);

        }
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

        this.displayBirthday.setText(new StringBuilder().append(year).append("-")
                .append(sMonth).append("-").append(sDay));
    }

    public void savePatientInfo() {

        String firstName = this.firstNameInput.getText().toString();
        String middleName = this.middleNameInput.getText().toString();
        String lastName = this.lastNameInput.getText().toString();

        Patient newPatient = new Patient(firstName, middleName, lastName,
                birthDate, genderSelected, civilStatusSelected);

        newPatientSessionManager.createNewPatientSession(firstName, middleName, lastName,
                birthDate, genderSelected, civilStatusSelected, encoded);

        Log.d("First Name", newPatient.getFirstName());
        Log.d("Middle Name", newPatient.getMiddleName());
        Log.d("Last Name", newPatient.getLastName());
        Log.d("Gender", newPatient.getGender());
        Log.d("Civil Status", newPatient.getCivilStatus());


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

    private void showPicker () {

        DatePickerFragment date = new DatePickerFragment();
        /**
         * Set Up Current Date Into dialog
         */
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        /**
         * Set Call back to capture selected date
         */
        date.setCallBack(ondate);
        date.show(getFragmentManager(), "Date Picker");
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int arg1, int arg2,
                              int arg3) {

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
            Log.d("date", birthDate + "");
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_IMAGE1 && resultCode == Activity.RESULT_OK) {

            Bitmap photo = (Bitmap)data.getExtras().get("data");
            setProfilePicBtn.setImageBitmap(photo);
            profilePicPlaceholder.setText("");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (photo != null) {
                photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
            }
            byte[] b = baos.toByteArray();

            encoded = Base64.encodeToString(b, Base64.DEFAULT);


            Log.d("image byte", encoded + "");

        }
    }
}
