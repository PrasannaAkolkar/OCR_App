package com.example.textocrapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;

public class MainActivity extends AppCompatActivity {

    EditText mResult_et;
    ImageView mImagePreview;
    Button save_as_pdf;
    TextView temp;


    private static final int CAMERA_REQ_CODE = 200;
    private static final int STORAGE_REQ_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;


    private static final int STORAGE_CODE = 101;

    String[] cameraPermission;
    String[] galleryPermission;

    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Click + Button to Insert an Image");


        mResult_et = findViewById(R.id.result_et);
        mImagePreview = findViewById(R.id.image_preview);
        save_as_pdf = findViewById(R.id.Save_as_pdf_btn);
        temp = findViewById(R.id.temp_tv);



        // camera permission
        cameraPermission = new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // gallery permission
        galleryPermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    // action bar menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //handle item clicks


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id==R.id.addImage){
            showImageDialog();
        }
        if(id==R.id.Settings)
        {
            Toast.makeText(this, "Settings Opened", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showImageDialog() {

        // items to display in dialouge

        String[] items = {"Camera","Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Select Image");

        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    //camera option clicked
                    /* For OS marshmallow and above we need to ask runtime permissions for
                         camera and storage*/

                    if(!checkCameraPermissions()){
                        // request permission from the user

                        requestCameraPermission();
                    }else{
                        // permission granted. Take picture
                        pickCamera();
                    }

                }
                if (which==1){
                    //gallery option clicked

                    if(!checkStoragePermissions()){
                        // request permission from the user

                        requestStoragePermission();
                    }else{
                        pickGallery();
                    }

                }
            }
        });
        dialog.create().show();



    }

    private void pickGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);



    }

    private void pickCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Pic");  // title of the pic
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to Text"); // description
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions(this,galleryPermission,STORAGE_REQ_CODE);

    }

    private boolean checkStoragePermissions() {

        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== (PackageManager.PERMISSION_GRANTED);
        return result1;
    }

    private void requestCameraPermission() {

        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQ_CODE);

    }

    private boolean checkCameraPermissions() {

        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)== (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== (PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }


    // handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQ_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&writeStorageAccepted){
                        pickCamera();
                    }
                    else{
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQ_CODE:
                if(grantResults.length>0){
                    boolean writeStorageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickGallery();
                    }
                    else{
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    // permission granted
                    pdfSave();
                }else{
                    // error message

                    Toast.makeText(this, "Error.. Permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;

        }


    }

    // handle image result


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);   // this might give error later on

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {

                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON) // enable image guidelines
                        .start(this);


            }

            if (requestCode == IMAGE_PICK_CAMERA_CODE) {


                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON) // enable image guidelines
                        .start(this);

            }
        }


        // get cropped image

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri(); // get image url
                mImagePreview.setImageURI(resultUri);

                // get drawable bitmap for text recognition
                BitmapDrawable bitmapDrawable = (BitmapDrawable)mImagePreview.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                }else{

                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();
                    // get text from sb until there is no text

                    for(int i=0; i<items.size();i++){
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");

                    }

                    //mResult_et.setText(sb.toString());

                    //sb.toString().replaceAll("\\s{2,}", " ").trim();
                    //String str = sb.toString().trim().replaceAll(" +"," ");

                    /*mResult_et.append(str);
                    mResult_et.append("\n\n");*/

                    mResult_et.append(sb.toString());
                    mResult_et.append("\n\n");
                    /*temp.append(sb.toString());
                    temp.append("\n\n");*/
                }

            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                // if any error....

                Exception error = result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();

            }
        }


    }


    public void GoogleSearch(View view) {

        Uri uri = Uri.parse("https://www.google.co.in/search?q="+ mResult_et.getText().toString());
        Intent sIntent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(sIntent);
    }

    public void Clear_All_Text(View view) {

        mResult_et.setText("");
        mImagePreview.setImageResource(0);


    }

    public void Save_As_PDF(View view) {
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            //system OS>=Marshmallow(6.0), check if permission is enabled or not

            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
            {
                // permission not granted. So, request permission

                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,STORAGE_CODE);


            }else{

                // permission already granted, call save pdf method

                pdfSave();


            }

        }else{

            // system OS < Marshmallo no need to check for permission, call save pdf method

            pdfSave();


        }
    }

    private void pdfSave() {
        // create object of document class

        Document mDoc = new Document();
        // pdf file name

        String mFileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        // save pdf file path

        String mFilePath = Environment.getExternalStorageDirectory()+"/"+mFileName+".pdf";
        //String mFilePath = Environment.getExternalStorageState()+"/"+mFileName+".pdf";

        try {

            // create instance of pdf writer class
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            //PdfWriter.getInstance(mDoc,new FileOutputStream(mFilePath));

            // open document for writing

            mDoc.open();

            // get text from edit text

            String mText = mResult_et.getText().toString();
            mDoc.addAuthor("Written by You");


            // add paragraph to the document

            mDoc.addTitle("Probability and Stats");

            Paragraph paragraph = new Paragraph(mText);

            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            mDoc.add(paragraph);

            // close document

            mDoc.close();

            // show that the file is saved

            Toast.makeText(this, mFileName+" saved to "+mFilePath, Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

}















































































































