package com.android.espial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FaceDetectActivity extends AppCompatActivity {

    @BindView(R.id.ifSelectedImage)
    ImageView ifSelectedImage;
    @BindView(R.id.right_eye_open_probability)
    TextView rightEyeOpenProbablity;
    @BindView(R.id.left_eye_open_probability)
    TextView leftEyeOpenProbablity;
    @BindView(R.id.smile_probability)
    TextView smileProbability;
    private final static String TAG = MainActivity.class.getName();
    private static final int SELECT_PICTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facedetect);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnFaceSelect)
    void onClickBtnFaceSelect(){
        //firing an intent to open gallery
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //getting result in form of URI of selected image
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedFileURI = data.getData();
                Log.d(TAG, "onActivityResult: " + selectedFileURI);
                String filepath = getRealPathFromURI(this, selectedFileURI);
                Log.d(TAG, "onActivityResult: " + filepath);
                //loading img view using bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(filepath);
                runFaceRecognition(bitmap);
                Drawable drawable = new BitmapDrawable(bitmap);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ifSelectedImage.setBackground(drawable);
                }
                Log.d(TAG, "onActivityResult: " + filepath);
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
            } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    private void runFaceRecognition(Bitmap mBitMap) {
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitMap);
//        Log.d(image.toString(), "Suvesh");
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.FAST_MODE)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setMinFaceSize(0.1f)
                .setTrackingEnabled(false)
                .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitMap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> faces) {
                for(FirebaseVisionFace face: faces){
                    smileProbability.setText(String.valueOf(face.getSmilingProbability()));
                    rightEyeOpenProbablity.setText(String.valueOf(face.getRightEyeOpenProbability()));
                    leftEyeOpenProbablity.setText(String.valueOf(face.getLeftEyeOpenProbability()));
                 }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Log.d(exception.toString(), "Suvesh Agnihotri");
                Toast.makeText(FaceDetectActivity.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });
    }

}


