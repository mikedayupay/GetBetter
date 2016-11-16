package com.dlsu.getbetter.getbetter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.sessionmanagers.NewPatientSessionManager;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CaptureDocumentsActivity extends AppCompatActivity implements View.OnClickListener {

    private static CaptureDocumentsActivity captureDocumentsActivity;

    private ImageView capturePatientInfo;
    private ImageView captureChiefComplaint;
    private ImageView captureSocialFamily;
    private Button viewPatientInfoImage;
    private Button removePatientInfoImage;
    private Button viewChiefComplaintImage;
    private Button removeChiefComplaintImage;
    private Button viewSocialFamilyImage;
    private Button removeSocialFamilyImage;
    private Button backButton;
    private Button nextButton;
    private LinearLayout patientInfoActionButtons;
    private LinearLayout chiefComplaintActionButtons;
    private LinearLayout socialFamilyActionButtons;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private String patientInfoImagePath = "null";
    private String familySocialHistoryImagePath = "null";
    private String chiefComplaintImagePath = "null";
    private NewPatientSessionManager newPatientSessionManager;

    private static final String PATIENT_INFO_FORM_TITLE = "Patient Information Form";
    private static final String FAMILY_SOCIAL_HISTORY_FORM_TITLE = "Family and Social History Form";
    private static final String CHIEF_COMPLAINT_FORM_TITLE = "Chief Complaint Form";

    private static final String PATIENT_INFO_FORM_FILENAME = "patientinfo";
    private static final String FAMILY_SOCIAL_HISTORY_FORM_FILENAME = "familysocialhistory";
    private static final String CHIEF_COMPLAINT_FORM_FILENAME = "chiefcomplaint";

    private static final int REQUEST_PATIENT_INFO_IMAGE = 100;
    private static final int REQUEST_CHIEF_COMPLAINT_IMAGE = 200;
    private static final int REQUEST_FAMILY_SOCIAL_IMAGE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_documents);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        captureDocumentsActivity = this;

        newPatientSessionManager = new NewPatientSessionManager(this);

        bindViews(this);
        bindListeners(this);

    }

    private void bindViews(CaptureDocumentsActivity activity) {
        activity.capturePatientInfo = (ImageView)activity.findViewById(R.id.capture_docu_patient_info_image);
        activity.captureChiefComplaint = (ImageView)activity.findViewById(R.id.capture_docu_chief_complaint_image);
        activity.captureSocialFamily = (ImageView)activity.findViewById(R.id.capture_docu_family_social_history_image);
        activity.viewPatientInfoImage = (Button)activity.findViewById(R.id.capture_docu_view_patient_info_image);
        activity.viewChiefComplaintImage = (Button)activity.findViewById(R.id.capture_docu_view_chief_complaint_image);
        activity.viewSocialFamilyImage = (Button)activity.findViewById(R.id.capture_docu_view_family_social_image);
        activity.removePatientInfoImage = (Button)activity.findViewById(R.id.capture_docu_remove_patient_info_image);
        activity.removeChiefComplaintImage = (Button)activity.findViewById(R.id.capture_docu_remove_chief_complaint_image);
        activity.removeSocialFamilyImage = (Button)activity.findViewById(R.id.capture_docu_remove_family_social_image);
        activity.backButton = (Button)activity.findViewById(R.id.capture_document_back_btn);
        activity.nextButton = (Button)activity.findViewById(R.id.capture_document_next_btn);
        activity.patientInfoActionButtons = (LinearLayout)findViewById(R.id.patient_info_image_action_buttons);
        activity.chiefComplaintActionButtons = (LinearLayout)findViewById(R.id.chief_complaint_image_action_buttons);
        activity.socialFamilyActionButtons = (LinearLayout)findViewById(R.id.family_social_image_action_buttons);
        activity.patientInfoActionButtons.setVisibility(View.INVISIBLE);
        activity.chiefComplaintActionButtons.setVisibility(View.INVISIBLE);
        activity.socialFamilyActionButtons.setVisibility(View.INVISIBLE);
    }

    private void bindListeners(CaptureDocumentsActivity activity) {
        activity.capturePatientInfo.setOnClickListener(activity);
        activity.captureChiefComplaint.setOnClickListener(activity);
        activity.captureSocialFamily.setOnClickListener(activity);
        activity.viewPatientInfoImage.setOnClickListener(activity);
        activity.viewChiefComplaintImage.setOnClickListener(activity);
        activity.viewSocialFamilyImage.setOnClickListener(activity);
        activity.removePatientInfoImage.setOnClickListener(activity);
        activity.removeChiefComplaintImage.setOnClickListener(activity);
        activity.removeSocialFamilyImage.setOnClickListener(activity);
        activity.backButton.setOnClickListener(activity);
        activity.nextButton.setOnClickListener(activity);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if(id == R.id.capture_docu_patient_info_image) {

            patientInfoImagePath = captureDocument(PATIENT_INFO_FORM_FILENAME,
                    REQUEST_PATIENT_INFO_IMAGE);

        } else if(id == R.id.capture_docu_chief_complaint_image) {

            chiefComplaintImagePath = captureDocument(CHIEF_COMPLAINT_FORM_FILENAME,
                    REQUEST_CHIEF_COMPLAINT_IMAGE);

        } else if(id == R.id.capture_docu_family_social_history_image) {

           familySocialHistoryImagePath = captureDocument(FAMILY_SOCIAL_HISTORY_FORM_FILENAME,
                   REQUEST_FAMILY_SOCIAL_IMAGE);

        } else if(id == R.id.capture_docu_view_patient_info_image) {

//            zoomImageFromThumb(viewPatientInfoImage, patientInfoImagePath);
            Intent intent = new Intent(this, ViewImageActivity.class);
            intent.putExtra("imageUrl", patientInfoImagePath);
            intent.putExtra("imageTitle", PATIENT_INFO_FORM_TITLE);
            startActivity(intent);


        } else if(id == R.id.capture_docu_view_chief_complaint_image) {

//            zoomImageFromThumb(viewChiefComplaintImage, chiefComplaintImagePath);
            Intent intent = new Intent(this, ViewImageActivity.class);
            intent.putExtra("imageUrl", chiefComplaintImagePath);
            intent.putExtra("imageTitle", CHIEF_COMPLAINT_FORM_TITLE);
            startActivity(intent);

        } else if(id == R.id.capture_docu_view_family_social_image) {

//            zoomImageFromThumb(viewSocialFamilyImage, familySocialHistoryImagePath);
            Intent intent = new Intent(this, ViewImageActivity.class);
            intent.putExtra("imageUrl", familySocialHistoryImagePath);
            intent.putExtra("imageTitle", FAMILY_SOCIAL_HISTORY_FORM_TITLE);
            startActivity(intent);

        } else if(id == R.id.capture_docu_remove_patient_info_image) {

        } else if(id == R.id.capture_docu_remove_chief_complaint_image) {

        } else if(id == R.id.capture_docu_remove_family_social_image) {

        } else if(id == R.id.capture_document_back_btn) {

            finish();

        } else if(id == R.id.capture_document_next_btn) {

            if(patientInfoImagePath.equals("null") || chiefComplaintImagePath.equals("null") ||
                    familySocialHistoryImagePath.equals("null")) {

                //todo show snackbar alert missing documents

            } else {

                newPatientSessionManager.setDocImages(patientInfoImagePath, familySocialHistoryImagePath, chiefComplaintImagePath,
                        PATIENT_INFO_FORM_TITLE, FAMILY_SOCIAL_HISTORY_FORM_TITLE, CHIEF_COMPLAINT_FORM_TITLE);
                Intent intent = new Intent(this, RecordHpiActivity.class);
                startActivity(intent);

            }

        }

    }

    private String captureDocument(String fileName, int requestId) {

        File imageFile = null;
        String imagePath = "null";

        try {
            try {
                imageFile = createImageFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(imageFile != null) {
                takePicture(requestId, imageFile);
                imagePath = imageFile.getAbsolutePath();
            }

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

        return imagePath;
    }

    private File createImageFile(String imageTitle) throws IOException {


        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        String imageFileName = imageTitle + "_" + timeStamp;

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);


        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void takePicture(int requestImage, File imageFile) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageFile != null) {

            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(intent, requestImage);
            Log.e("image path", imageFile.getAbsolutePath());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK) {

            switch (requestCode) {

                case REQUEST_PATIENT_INFO_IMAGE:

                    setPic(capturePatientInfo, patientInfoImagePath);
                    patientInfoActionButtons.setVisibility(View.VISIBLE);
                    break;

                case REQUEST_CHIEF_COMPLAINT_IMAGE:

                    setPic(captureChiefComplaint, chiefComplaintImagePath);
                    chiefComplaintActionButtons.setVisibility(View.VISIBLE);
                    break;

                case REQUEST_FAMILY_SOCIAL_IMAGE:

                    setPic(captureSocialFamily, familySocialHistoryImagePath);
                    socialFamilyActionButtons.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

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

    private void zoomImageFromThumb(final View thumbView, String photoPath) {

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView)findViewById(
                R.id.expanded_image);

//        int targetW = expandedImageView.getWidth();
//        int targetH = expandedImageView.getHeight();
//
//        // Get the dimensions of the bitmap
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(photoPath, bmOptions);
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//
//        // Determine how much to scale down the image
//        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
//
//        // Decode the image file into a Bitmap sized to fill the View
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

        expandedImageView.setImageBitmap(bitmap);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);

        thumbView.findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    public static CaptureDocumentsActivity getInstance() {
        return captureDocumentsActivity;
    }
}
