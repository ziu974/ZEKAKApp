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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


// 1. ZXING API imports
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

// 2. Food Product Barcode API imports
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;



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

    int category = 0;



    ///// 11/13 결국 Zxing
    private DecoratedBarcodeView barcodeScannerView;
    private CaptureManager captureManager;
    private BeepManager beepManager;
    private String lastText;    // 바코드 중복 인식을 막기 위해


    Intent intent = null;
    Intent returnIntent = null;

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


            // [중요] Android 4.0 이상 부터는 네트워크를 이용할 때 반드시 Thread 사용해야 함
            new Thread(new Runnable() {
                @Override
                public void run() {
                    StatusCODE = searchProduct(barcodeValue);           //아래 메소드를 호출하여 XML data를 파싱해서 String 객체로 얻어오기
                    Log.i("공공API 스레드 값",  StatusCODE);
                    if(!StatusCODE.equals("attempt")) {       // 결과값을 intent에 담아 AddItem로 intent
                        setStatus(StatusCODE);
                    }
                }
            }).start();

            Log.i("메인 스레드 값",  StatusCODE);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);

        StatusCODE = "attempt";     // nullpointError 방지하기 위해(searchProduct() 실행전이라는 의미)

        category = getIntent().getIntExtra("INITIAL_CATEGORY", 0);

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
        StringBuffer buffer = new StringBuffer();
        String queryUrl = "http://openapi.foodsafetykorea.go.kr/api/6a8b4f6ef9c24654bd86/C005/xml/1/1000/BAR_CD=" + barcodeValue;       // TODO: 여기 1000건에 없으면 어떡할건데

        //https://recipes4dev.tistory.com/134 참고
        XmlPullParserFactory parserCreator;
        XmlPullParser xmlParser;


        try {
            // 바코드연계제품정보 API: URL, query
            URL url = new URL(queryUrl);    // URL 객체
            InputStream inputStream = url.openStream();     // URL 위치로 입력스트림 연결

            parserCreator = XmlPullParserFactory.newInstance();
            xmlParser = parserCreator.newPullParser();
            xmlParser.setInput(new InputStreamReader(inputStream));        // inputstream으로부터 xml 입력받기

            xmlParser.next();       // START_DOCUMENT 다음으로 가기 위해
            int parserEvent = xmlParser.getEventType();
            Log.i("searchProduct()","===Start Parsing...===");

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:     //parser가 <시작 태그>를 만나면 실행, 그 내용을 받을 수 있게 in** = true;
                        if (xmlParser.getName().equals("CODE")) { // Status Code of API response msg
                            Log.i("<CODE>", "in");
                            inStatusCode = true;
                        }
                        // just in case
                        if (xmlParser.getName().equals("BAR_CD")) { // barcode value (이미 알고 있지만)
                            Log.i("<BAR_CD>", "바코드");
                            inBAR_CD = true;
                        }
                        if (xmlParser.getName().equals("PRDLST_NM")) { // product name
                            Log.i("<PRDLST_NM>", "제품명");
                            inPRDLST_NM = true;
                        }
                        if (xmlParser.getName().equals("POG_DAYCNT")) { // expire date
                            Log.i("<POG_DAYCNT>", "유통기한");
                            inPOG_DAYCNT = true;
                        }

                        break;

                    case XmlPullParser.TEXT:            //parser가 내용에 접근했을때
                        if (inStatusCode) {             // Status code of Response msg
                            StatusCODE = xmlParser.getText();
                            inStatusCode = false;

                            switch (StatusCODE) {
                                case "INFO-000":        // "정상처리 되었습니다."
                                    Log.i("API","Matching Product Found");
                                    break;
                                case "INFO-200":        // "해당하는 데이터가 없습니다."
                                    Log.i("API","No matching Product, Add Manually");
                                    break;
                                case "INFO-300":        // "유효 호출건수를 이미 초과하셨습니다."
                                    Log.i("API","Used all provided requests(500)");
                                    break;
                                default:
                                    Log.i("Status something else",StatusCODE);
                                    break;
                            }
                        }
                        if (inBAR_CD) { // Requested Barcode value save it into another variable, just in case
                            BAR_CD = xmlParser.getText();
                            inBAR_CD = false;
                        }
                        if (inPRDLST_NM) { // Product name with the matching barcode no.
                            PRDLST_NM = xmlParser.getText();
                            inPRDLST_NM = false;
                        }
                        if (inPOG_DAYCNT) { // Product's expire date information
                            POG_DAYCNT = xmlParser.getText();
                            inPOG_DAYCNT = false;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (StatusCODE.equals("INFO-000")) {//parser.getName().equals("row") && ) {
                            ////??
                        }
                        break;
                }
                parserEvent = xmlParser.next();         // 다음 태그로 파싱 진행(안하면 무한루프겠죠?)
            }
            Log.i("End of XML document(식품안전나라)", "returning search results..");
        } catch (Exception e) {
            e.printStackTrace();
            StatusCODE = "ERROR";
            Log.i("바코드 클래스","ERROR with barcode scanning Activity");
        }

        return StatusCODE;
    }

    private void setStatus(String StatusCODE) {     // 이름은 같지만 추가스레드에서 온 값임
        switch (StatusCODE) {
            case "INFO-000":        // 정상처리 되었습니다.
                Log.i("API Search result", PRDLST_NM);
                intent = new Intent(BarcodeScanningActivity.this, AddItem.class);
                intent.putExtra("EXTRA_SESSION_ID", StatusCODE);
                intent.putExtra("barcodeValue", BAR_CD);
                intent.putExtra("productName", PRDLST_NM);
                intent.putExtra("productExp", POG_DAYCNT);
                break;
            case "INFO-200":        // 해당하는 데이터가 없습니다.
                // 찾는 바코드 없다는 메세지 띄움
                //Toast.makeText(this, "No such product :(", Toast.LENGTH_LONG).show();       // 아 맞다 스레드에서는 뷰 변경 불가
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(BarcodeScanningActivity.this, "No such product :(", Toast.LENGTH_LONG).show();
                    }
                });
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
                break;
        }

        if (intent != null) {         // AddItem. 으로 화면 넘어감
            onDestroy();
            finish();
            startActivity(intent);
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

    // [복사본] 나중에 쓸 수도 있을 것 같아서
//    new Thread(new Runnable(){
//        @Override
//        public void run(){
//            StatusCODE=searchProduct(barcodeValue);           //아래 메소드를 호출하여 XML data를 파싱해서 String 객체로 얻어오기
//
//            // TODO 이말 중요!! ui 스레드 부분
//            UI Thread(Main Thread)를 제외한 어떤 Thread도 화면을 변경할 수 없기때문에
//            runOnUiThread()를 이용하여 UI Thread가 TextView 글씨 변경하도록 함
//            runOnUiThread(new Runnable(){
//                @Override
//                public void run(){
//                }
//         });
//    }).start();