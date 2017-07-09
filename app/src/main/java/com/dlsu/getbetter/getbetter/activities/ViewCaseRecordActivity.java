package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.ListenAudioFragment;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.ViewVideoFragment;
import com.dlsu.getbetter.getbetter.adapters.FileAttachmentsAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.interfaces.GetBetterClient;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.Connectivity;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.services.ServiceGenerator;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewCaseRecordActivity extends AppCompatActivity implements MediaController.MediaPlayerControl, View.OnClickListener {

    private static final String TAG = "ViewCaseRecordActivity";

    private TextView patientName;
    private TextView healthCenterName;
    private TextView ageGender;
    private TextView chiefComplaint;
    private TextView controlNumber;
    private TextView additionalNotes;
    private CircleImageView profilePic;
    private Button backBtn;
    private Button updateCaseBtn;
    private RecyclerView attachmentList;

    private int healthCenterId;
    private CaseRecord caseRecord;
    private Patient patientInfo;
    private ArrayList<Attachment> caseAttachments;
    private int attachmentType;
    private Uri fileUri;

    private DataAdapter getBetterDb;
    private MediaPlayer nMediaPlayer;
    private MediaController nMediaController;
    private Handler nHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_case_record);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        Bundle extras = getIntent().getExtras();
        int caseRecordId = extras.getInt("caseRecordId");
        long patientId = extras.getLong("patientId");

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        killMediaPlayer();
        nMediaPlayer = new MediaPlayer();
        nMediaController = new MediaController(ViewCaseRecordActivity.this) {
            @Override
            public void hide() {

            }
        };

        nMediaController.setMediaPlayer(ViewCaseRecordActivity.this);
        nMediaController.setAnchorView(findViewById(R.id.hpi_media_player));

        initializeDatabase();
        getCaseRecord(caseRecordId);
        getCaseAttachments(caseRecordId);
        getPatientInfo(patientId);
        bindViews(this);
        bindListeners(this);
        initFileList(this);

        String recordedHpiOutputFile = getHpiOutputFile(caseRecordId);
        Log.d(TAG, "onCreate: " + recordedHpiOutputFile);
        nMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            nMediaPlayer.setDataSource(recordedHpiOutputFile);
            nMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: " + e.toString());
        }

        nMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                nHandler.post(new Runnable() {
                    public void run() {
                        nMediaController.show(0);
                        nMediaController.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                nMediaPlayer.start();
                            }
                        });

                    }
                });
            }
        });
    }

    private void bindViews(ViewCaseRecordActivity activity) {

        activity.patientName = (TextView)activity.findViewById(R.id.view_case_patient_name);
        activity.healthCenterName = (TextView)activity.findViewById(R.id.view_case_health_center);
        activity.ageGender = (TextView)activity.findViewById(R.id.view_case_age_gender);
        activity.chiefComplaint = (TextView)activity.findViewById(R.id.view_case_chief_complaint);
        activity.controlNumber = (TextView)activity.findViewById(R.id.view_case_control_number);
        activity.additionalNotes = (TextView)activity.findViewById(R.id.view_case_additional_notes);
        activity.attachmentList = (RecyclerView)activity.findViewById(R.id.view_case_files_list);
        activity.profilePic = (CircleImageView) activity.findViewById(R.id.profile_picture_display);
        activity.backBtn = (Button)activity.findViewById(R.id.view_case_back_btn);
        activity.updateCaseBtn = (Button)activity.findViewById(R.id.update_case_record_btn);

        String fullName = patientInfo.getFirstName() + " " + patientInfo.getMiddleName() + " " + patientInfo.getLastName();
        String gender = patientInfo.getGender();
        String patientAgeGender = patientInfo.getAge() + " yrs. old, " + gender;

        if(!patientInfo.getProfileImageBytes().equals("")) {
            setPic(profilePic, patientInfo.getProfileImageBytes());
        }


        ageGender.setText(patientAgeGender);
        chiefComplaint.setText(caseRecord.getCaseRecordComplaint());

        if (caseRecord.getCaseRecordControlNumber() != null ) {
            controlNumber.setText(caseRecord.getCaseRecordControlNumber());
        }

        additionalNotes.setText(caseRecord.getCaseRecordAdditionalNotes());

        patientName.setText(fullName);
        healthCenterName.setText(getHealthCenterString(healthCenterId));
        activity.updateCaseBtn.setVisibility(View.INVISIBLE);

    }

    private void bindListeners(ViewCaseRecordActivity activity) {

        activity.backBtn.setOnClickListener(activity);
        activity.updateCaseBtn.setOnClickListener(activity);
    }

    private void initFileList(ViewCaseRecordActivity activity) {

        FileAttachmentsAdapter fileAdapter = new FileAttachmentsAdapter(caseAttachments);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(activity);

        activity.attachmentList.setHasFixedSize(true);
        activity.attachmentList.setLayoutManager(layoutManager);
        activity.attachmentList.setAdapter(fileAdapter);
        activity.attachmentList.addItemDecoration(dividerItemDecoration);
        fileAdapter.SetOnItemClickListener(new FileAttachmentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (caseAttachments.get(position).getIsNew() == 1) {

                    if (Connectivity.isConnectedFast(ViewCaseRecordActivity.this)) {

                        downloadAttachment(caseAttachments.get(position));
//                        fileAdapter.notifyItemChanged(position);

                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewCaseRecordActivity.this);
                        builder.setTitle("Internet Connectivity");
                        builder.setMessage("Could not download file due to no internet connection or slow internet.");

                        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder.show();

                    }

                } else {

                    String path = caseAttachments.get(position).getAttachmentPath();
                    String title = caseAttachments.get(position).getAttachmentDescription();

                    Log.d(TAG, "path: " + path);
                    Log.d(TAG, "title: " + title);

                    if(caseAttachments.get(position).getAttachmentType() == 1) {

                        Intent intent = new Intent(ViewCaseRecordActivity.this, ViewImageActivity.class);
                        intent.putExtra("imageUrl", path);
                        intent.putExtra("imageTitle", title);
                        startActivity(intent);

                    } else if (caseAttachments.get(position).getAttachmentType() == 2) {

                        FragmentManager fm = getSupportFragmentManager();
                        ViewVideoFragment viewVideoFragment = ViewVideoFragment.newInstance(path, title);
                        viewVideoFragment.show(fm , "fragment_video");


                    } else if (caseAttachments.get(position).getAttachmentType() == 3 || caseAttachments.get(position).getAttachmentType() == 5) {


                        FragmentManager fm = getSupportFragmentManager();
                        ListenAudioFragment listenAudioFragment = ListenAudioFragment.newInstance(path, title);
                        listenAudioFragment.show(fm, "fragment_listen");
                    }
                }
            }
        });
    }

    private void initializeDatabase() {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch(SQLException e ){
            e.printStackTrace();
        }

    }

    private void getCaseRecord(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseRecord = getBetterDb.getCaseRecord(caseRecordId);

        getBetterDb.closeDatabase();

    }

    private void getCaseAttachments(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseAttachments = new ArrayList<>();
        caseAttachments.addAll(getBetterDb.getCaseRecordAttachments(caseRecordId));

        getBetterDb.closeDatabase();

    }

    private void getPatientInfo(long patientId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        patientInfo = getBetterDb.getPatient(patientId);

        getBetterDb.closeDatabase();
    }

    private String getHealthCenterString(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String healthCenterName = getBetterDb.getHealthCenterString(healthCenterId);

        getBetterDb.closeDatabase();

        return healthCenterName;

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.view_case_back_btn) {

            finish();

        } else if (id == R.id.update_case_record_btn) {

        }
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 255;//mImageView.getWidth();
        int targetH = 200;// mImageView.getHeight();

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

    private String getHpiOutputFile(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result;

        result = getBetterDb.getHPI(caseRecordId);

        getBetterDb.closeDatabase();

        return result;
    }

    private void downloadAttachment (final Attachment attachment) {

        GetBetterClient getBetterClient = ServiceGenerator.createService(GetBetterClient.class);

//        String filePath = DirectoryConstants.SERVER_UPLOAD_URL + attachment.getAttachmentPath();
//        Log.d(TAG, "downloadAttachment: " + filePath);

        Call<ResponseBody> call = getBetterClient.downloadAttachmentFile(attachment.getAttachmentPath());

        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(ViewCaseRecordActivity.this);
//        progressDoalog.setMax(100);
        progressDoalog.setMessage("Downloading attachment file...");
        progressDoalog.setTitle("GetBetter Server");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // show it
        progressDoalog.show();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                if (response.isSuccessful()) {

                    Log.d(TAG, "server contacted and has file");

                    new AsyncTask<Void, Void, Void>() {


                        @Override
                        protected Void doInBackground(Void... voids) {

                            boolean writtenToDisk = writeResponseBodyToDisk(response.body(), attachment.getAttachmentDescription(), attachment.getAttachmentPath());

                            Log.d(TAG, "file download was a success? " + writtenToDisk);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);

                            updateAttachmentStatus(attachment.getAttachmentId());
                            Toast.makeText(ViewCaseRecordActivity.this, "Download Complete!", Toast.LENGTH_LONG).show();

                        }
                    }.execute();

                    progressDoalog.dismiss();

                } else {

                    Log.d(TAG, "server contact failed");
                    progressDoalog.dismiss();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.d(TAG, "server contact failed: " + t.toString());
                progressDoalog.dismiss();
            }
        });

    }

    private boolean writeResponseBodyToDisk (ResponseBody body, String description, String url) {

        try {
            File attachmentFile = createAttachmentFile(description, url);

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

    private void updateAttachmentStatus (int attachmentId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.updateAttachmentStatus(attachmentId, attachmentType, fileUri.getPath());

        getBetterDb.closeDatabase();

    }

    private File createAttachmentFile(String description, String url) {

        File attachmentFile = null;

//        Log.d(TAG, "createAttachmentFile: " + description + attachmentType);
        String extension = url.substring(url.length() - 3);
        Log.d(TAG, "createAttachmentFile: " + extension);
        File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Debug", "Oops! Failed create "
                        + DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        if(extension.contains("jpg")) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + ".jpg");

            attachmentType = 1;

        } else if (extension.contains("3gp")) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + ".3gp");

            attachmentType = 3;

        } else if (extension.contains("mp4")) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + ".mp4");

            attachmentType = 2;

        } else if (extension.contains("png")) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + ".png");

            attachmentType = 1;

        } else if (extension.contains("avi")) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + ".avi");

            attachmentType = 2;

        } else if (extension.contains("gif")) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + ".gif");

            attachmentType = 1;

        } else if (extension.contains("wmv")) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + ".wmv");

            attachmentType = 2;
        }

        fileUri = Uri.fromFile(attachmentFile);

        return attachmentFile;
    }

    @Override
    public void onPause() {
        super.onPause();
        killMediaPlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        killMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
    }

    private void killMediaPlayer() {
        if(nMediaPlayer != null) {
            try{
                if(nMediaController.isShowing() || nMediaController != null) {
                    nMediaController.hide();
                }
                nMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {

        return (nMediaPlayer.getCurrentPosition() * 100) / nMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return nMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return nMediaPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return nMediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if(nMediaPlayer.isPlaying())
            nMediaPlayer.pause();
    }

    @Override
    public void seekTo(int pos) {
        nMediaPlayer.seekTo(pos);
    }

    @Override
    public void start() {
        nMediaPlayer.start();
    }

}
