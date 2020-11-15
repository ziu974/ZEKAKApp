/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.zekak;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.media.Image;
import android.net.TrafficStats;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

// 1. Google Vision API imports
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;


// 2. ZXING API imports
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;


import java.io.IOException;


// 2. Food Product Barcode API imports
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class BarcodeScanningActivity extends CaptureActivity {
    boolean inBAR_CD = false;
    boolean inPRDLST_NM = false;
    boolean inPOG_DAYCNT = false;
    boolean inStatusCode = false;

    String barcodeValue = null; // Google Vision API result
    String BAR_CD = null;       // Scanned barcode value
    String PRDLST_NM = null;    // Product name with the matching barcode no.
    String POG_DAYCNT = null;   // Product's expire date information
    String StatusCODE = null;

    String category = null;



    ///// 11/13 결국 Zxing
    private DecoratedBarcodeView barcodeScannerView;
    private CaptureManager captureManager;
    private BeepManager beepManager;
    private String lastText;    // 바코드 중복 인식을 막기 위해

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            //prevent duplicate scans
            if (result.getText() == null || result.getText().equals(lastText)) {
                return;
            }

            lastText = result.getText();
            barcodeScannerView.setStatusText(result.getText());
            beepManager.playBeepSound();

            barcodeScannerView.pauseAndWait();

            // [핵심] 인식된 아이템의 바코드 저장
            barcodeValue = result.getText();
            Log.i("바코드 번호", barcodeValue);


            StatusCODE = searchProduct(barcodeValue);     // 혹시 몰라서 전역변수이지만 또 저장
            if(!StatusCODE.isEmpty())
                setStatus();
        }
    };



    Intent intent = null;
    Intent returnIntent = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);


        category = getIntent().getStringExtra("INITIAL_CATEGORY");


        /////zxing
        // 바코드 스캔 시작(카메라)
        barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.EAN_13);
        barcodeScannerView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeScannerView.initializeFromIntent(getIntent());
        barcodeScannerView.decodeContinuous(callback);  // 계속 바코드 인식하는 함수 콜백

        beepManager = new BeepManager(this);


    }

    private String searchProduct(String barcodeValue) {
        TrafficStats.setThreadStatsTag((int) Thread.currentThread().getId());
        try {
            // 바코드연계제품정보 API: URL, query
            URL url = new URL("http://openapi.foodsafetykorea.go.kr/api/6a8b4f6ef9c24654bd86/C005/xml/1/100466/BAR_CD=" + barcodeValue);

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();
            Log.i("상태","===Start Parsing...===");

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if (parser.getName().equals("CODE")) { // Status Code of API response msg
                            inStatusCode = true;
                        }
                        // just in case
                        if (parser.getName().equals("BAR_CD")) { //title 만나면 내용을 받을수 있게 하자
                            inBAR_CD = true;
                        }
                        if (parser.getName().equals("PRDLST_NM")) { //address 만나면 내용을 받을수 있게 하자
                            inPRDLST_NM = true;
                        }
                        if (parser.getName().equals("POG_DAYCNT")) { //mapx 만나면 내용을 받을수 있게 하자
                            inPOG_DAYCNT = true;
                        }

                        break;

                    case XmlPullParser.TEXT://parser가 내용에 접근했을때
                        if (inStatusCode) { // Status code of Response msg
                            StatusCODE = parser.getText();
                            inStatusCode = false;

                            switch (StatusCODE) {
                                case "INFO-000":        // 정상처리 되었습니다.
                                    System.out.println("Matching Product Found");
                                    break;
                                case "INFO-200":        // 해당하는 데이터가 없습니다.
                                    System.out.println("No matching Product, Add Manually");
                                    break;
                                case "INFO-300":        // 유효 호출건수를 이미 초과하셨습니다.
                                    System.out.println("Used all provided requests(500)");
                                    break;
                                default:
                                    System.out.println("Status something else");
                                    break;
                            }
                        }
                        if (inBAR_CD) { // Requested Barcode value save it into another variable, just in case
                            BAR_CD = parser.getText();
                            inBAR_CD = false;
                        }
                        if (inPRDLST_NM) { // Product name with the matching barcode no.
                            PRDLST_NM = parser.getText();
                            inPRDLST_NM = false;
                        }
                        if (inPOG_DAYCNT) { // Product's expire date information
                            POG_DAYCNT = parser.getText();
                            inPOG_DAYCNT = false;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (StatusCODE.equals("INFO-000")) {//parser.getName().equals("row") && ) {
                            ////??
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
            StatusCODE = "ERROR";
            System.out.println("ERROR with barcode scanning Activity");
            //status1.setText("에러가..났습니다...");
        }

        return StatusCODE;
    }

    private void setStatus() {
        switch (StatusCODE) {
            case "INFO-000":        // 정상처리 되었습니다.
                intent = new Intent(BarcodeScanningActivity.this, AddItem.class);
                intent.putExtra("EXTRA_SESSION_ID", StatusCODE);
                intent.putExtra("barcodeValue", BAR_CD);
                intent.putExtra("productName", PRDLST_NM);
                intent.putExtra("productExp", POG_DAYCNT);
                break;
            case "INFO-200":        // 해당하는 데이터가 없습니다.
                // TODO: 찾는 바코드 없다는 다이얼로그 띄움
                Toast.makeText(this, "No such product :(", Toast.LENGTH_SHORT).show();
                //
                intent = new Intent(BarcodeScanningActivity.this, AddItem.class);
                intent.putExtra("EXTRA_SESSION_ID", StatusCODE);
                intent.putExtra("INITIAL_CATEGORY", category);
                break;
            case "INFO-300":        // 유효 호출건수를 이미 초과하셨습니다.
                intent = new Intent(BarcodeScanningActivity.this, AddItem.class);
                intent.putExtra("EXTRA_SESSION_ID", StatusCODE);
                intent.putExtra("INITIAL_CATEGORY", category);
                break;
            default:                // "ERROR"
                Log.e("BarcodeScanningActivity:", "ERROR with resultCode");
                setResult(RESULT_CANCELED, returnIntent);
                finish();
        }

        if (intent != null) {         // AddItem. 으로 화면 넘어감
            startActivity(intent);
            finish();
        }
    }


    ///// 결국 Zxing

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void pause(View view) {
        barcodeScannerView.pause();
    }

    public void resume(View view) {
        barcodeScannerView.resume();
    }

    public void cancel(View view) {      // Barcode Add cancel btn clicked
        returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

}