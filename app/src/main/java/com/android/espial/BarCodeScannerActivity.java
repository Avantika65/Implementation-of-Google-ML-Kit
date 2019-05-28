package com.android.espial;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BarCodeScannerActivity extends AppCompatActivity {


    @BindView(R.id.ifSelectedImage)
    ImageView ifSelectedImage;
    @BindView(R.id.url_link)
    TextView urlLink;

    private final static String TAG = MainActivity.class.getName();
    private static final int SELECT_PICTURE = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcodescan);
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
                getQrCodeData(bitmap);
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

    private void getQrCodeData(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                @Override
                public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                    for(FirebaseVisionBarcode barcode: barcodes){
                        Log.d(barcode.getDisplayValue(), "Suvesh");
                        urlLink.setText(barcode.getDisplayValue());
//                        if(barcode.getEmail().getAddress() != null){
//                            email.setText(barcode.getEmail().getAddress());
//                        }
//                        if(barcode.getContactInfo().getPhones().get(0).getNumber() !=null){
//                            Log.d(barcode.getContactInfo().getPhones().get(0).getNumber(), "Suvesh");
//                            phone.setText(barcode.getContactInfo().getPhones().get(0).getNumber());
//                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Task failed with an exception
                    // ...
                }
            });


    }



}
