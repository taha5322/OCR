package com.siddiqui.ocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView,checkView;
    Button btn_freeze,btn_yes,btn_no;
    CameraSource cameraSource;
    final int RequestCameraPermissionId = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RequestCameraPermissionId:{
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_no = findViewById(R.id.no);
        btn_yes = findViewById(R.id.yes);
        checkView = findViewById(R.id.checkView);

        btn_no.setVisibility(View.INVISIBLE);
        btn_yes.setVisibility(View.INVISIBLE);
        checkView.setVisibility(View.INVISIBLE);

        //surface view containing the camera screen
        cameraView = findViewById(R.id.surface_view);
        textView = findViewById(R.id.text_view);
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if(!textRecognizer.isOperational()){
            Log.w("MainActivity","Detector dependencies arent availible yet");
        } else{

            cameraSource = new CameraSource.Builder(getApplicationContext(),textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280,1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try{
                        //requesting permission
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionId);
                            return;
                        }
                        //binds surface view to user's camera source
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });


            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    // checks if text exists
                    if(items.size()!=0){
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i =0;i<items.size();++i){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                textView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });

            btn_freeze = findViewById(R.id.button);
            btn_freeze.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraSource.stop();
                    checkView.setVisibility(View.VISIBLE);
                    btn_no.setVisibility(View.VISIBLE);
                    btn_yes.setVisibility(View.VISIBLE);

                    btn_yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String current = textView.getText().toString();
                            textView.setText(current);
                            Toast.makeText(MainActivity.this,"Successfully recieved text",
                                    Toast.LENGTH_SHORT).show();
                            cameraSource.stop();
                            cameraView.setVisibility(View.INVISIBLE);
                            btn_yes.setVisibility(View.INVISIBLE);
                            btn_no.setVisibility(View.INVISIBLE);
                            checkView.setVisibility(View.INVISIBLE);
                            btn_freeze.setVisibility(View.INVISIBLE);
                        }
                    });

                    btn_no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(MainActivity.this,"Text not scanned",
                                    Toast.LENGTH_SHORT).show();
                            btn_yes.setVisibility(View.INVISIBLE);
                            btn_no.setVisibility(View.INVISIBLE);
                            checkView.setVisibility(View.INVISIBLE);

                            try {
                                cameraSource.start(cameraView.getHolder());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                }
            });
        }

    }
}
