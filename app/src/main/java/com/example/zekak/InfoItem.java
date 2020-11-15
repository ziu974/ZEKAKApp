package com.example.zekak;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class InfoItem extends AppMain {
    int itemID;
    Item itemInfo;

    TextView name;
    TextView category;
    ImageView pin;
    ImageView photo;
    TextView exp;
    TextView remainingDates;
    TextView portion;
    ProgressBar portionBar;
    TextView portionLabel;
    ScrollView memoView;
    TextView memo;

    Button btnAllUsed;
    ImageButton btnEditItem;
    ImageButton btnDeleteItem;
    TextView btnCloseInfo;

    static final int EDIT_ITEM = 1;

    int initialUsage;       // 처음 아이템 사용량
    int divided;
    int used;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);

        // PART1: 어디에서 왔는지
        // intent 엑스트라로 id 값 넘갸줄거라서 해시맵에서 이 아이디값 가지고 탐색 진행해야함
        itemID = getIntent().getIntExtra("ITEM_ID", 0);

        // PART2: 해시맵 탐색해서 아이템 정보 가져옴
        itemInfo = ITEM_MAP.get(itemID);


        ///// PART3: 레이아웃 관련
        name = (TextView) findViewById(R.id.text_item_name);
        category = (TextView) findViewById(R.id.text_item_category);
        pin = (ImageView) findViewById(R.id.icon_item_pin);
        photo = (ImageView) findViewById(R.id.image_item_photo);
        exp = (TextView) findViewById(R.id.exp_date);
        remainingDates = (TextView) findViewById(R.id.text_remaining_dates);
        portion = (TextView) findViewById(R.id.text_item_portion);
        portionBar = (ProgressBar) findViewById(R.id.portion_bar);
        portionLabel = (TextView) findViewById(R.id.text_item_portion_bar);
        memoView = (ScrollView) findViewById(R.id.scrollview_item_memo);     // TODO: 이거 쓸라나
        memo = (TextView) findViewById(R.id.item_memo);

        btnAllUsed = (Button) findViewById(R.id.all_used_btn);
        btnEditItem = (ImageButton) findViewById(R.id.edit_item_btn);
        btnDeleteItem = (ImageButton) findViewById(R.id.delete_item_btn);
        btnCloseInfo = (TextView) findViewById(R.id.item_info_close_btn);

        initialUsage = itemInfo.portion;
        divided = initialUsage / 10;   // 사용자 1회분 설정값
        used = initialUsage - divided * 10; // 사용량


        // item name
        name.setText(itemInfo.name);
        // category
        category.setText(itemInfo.category);
        // pin icon
        if(itemInfo.flag){
            pin.setVisibility(View.VISIBLE);    // OR View.INVISIBLE
        }
        // add_photo_background.xml thumbnail
        //(CODE: 포토) 이렇게 하면 이미지 잘 뜸!!! (최종 끝)
        if(itemInfo.photo != null) {
            String path = imagePath.getPath() + "/" + itemInfo.photo;
            photo.setImageBitmap(BitmapFactory.decodeFile(path));
        }

        // exp info
        exp.setText(itemInfo.exp);
        calcRemainingDates(itemInfo.exp);

        // item portion bar
        portion.setText("남은 양\n(설정된 1회분: "+divided+")");
        portionBar.setProgress(used/divided * 100);
        portionLabel.setText(used+"/"+divided);
        // memo

        memo.setText(itemInfo.memo);


        // 버튼들에 리스너 등록
        BtnOnClickListener onClickListener = new BtnOnClickListener();
        btnAllUsed.setOnClickListener(onClickListener);
        btnEditItem.setOnClickListener(onClickListener);
        btnDeleteItem.setOnClickListener(onClickListener);
        btnCloseInfo.setOnClickListener(onClickListener);
    }

    class BtnOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            Intent returnIntent = new Intent();
            switch (view.getId()){
                case R.id.all_used_btn:         // 모두 먹음 처리
                    final Dialog askAllUse = new Dialog(InfoItem.this);
                    askAllUse.setContentView(R.layout.all_used_dialog);
                    TextView itemName1 = (TextView) askAllUse.findViewById(R.id.item_name_text);
                    itemName1.setText(name.getText()+"을(를)");
                    Button allUsedBtn1 = (Button) askAllUse.findViewById(R.id.all_used_btn);
                    allUsedBtn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO: 모두 먹음 처리
                            //
                            returnIntent.putExtra("ITEM_USED", "all");
                            setResult(RESULT_OK, returnIntent);
                            askAllUse.dismiss();
                            finish();
                        }
                    });
                    if((divided - used) != 1){  // 1회분 사용 가능할 경우(모두먹음 처리 안되고)
                        Button portionUsedBtn = (Button) askAllUse.findViewById(R.id.use_portion_btn);
                        portionUsedBtn.setVisibility(View.VISIBLE);
                        portionUsedBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {   // 사용량 저장하고, progress bar 업데이트
                                used++;
                                returnIntent.putExtra("ITEM_USED", "portion");
                                setResult(RESULT_OK, returnIntent);
                                portionBar.setProgress(used/divided * 100);
                                portionLabel.setText(used+"/"+divided);
                                itemsDB.usePortion(itemID, initialUsage++);
                                //TODO: usePortion() 에러처리(false)
                                askAllUse.dismiss();
                            }
                        });
                    }
                    break;

                case R.id.edit_item_btn:        // edit item
                    Intent intent = new Intent(InfoItem.this, AddItem.class);
                    intent.putExtra("EXTRA_SESSION_ID", "edit");
                    intent.putExtra("ITEM_ID", itemID);
                    intent.putExtra("INITIAL_CATEGORY", categoryList.indexOf(itemInfo.category));

                    startActivityForResult(intent, EDIT_ITEM);
                    break;

                case R.id.delete_item_btn:      // delete item
                    final Dialog askAgain = new Dialog(InfoItem.this);
                    askAgain.setContentView(R.layout.delete_item_dialog);
                    TextView itemName2 = (TextView) askAgain.findViewById(R.id.item_name_text);
                    Button allUsedBtn2 = (Button) askAgain.findViewById(R.id.all_used_btn);
                    Button deletePermanentBtn = (Button) askAgain.findViewById(R.id.item_delete_btn);

                    itemName2.setText(name.getText()+"을(를)");
                    allUsedBtn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO: 위에 있는 첫번째 case에서 하는 동작들 다이얼로그 제외하고 동일하게 -- 차라리 '모두먹음' 함수로 빼자
                        }
                    });
                    deletePermanentBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(itemsDB.delete("item", Integer.toString(itemID))>0){
                                // successfully deleted
                                Toast.makeText(InfoItem.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i("zekak", "Error deleting item from DB");
                                Toast.makeText(InfoItem.this, "Error deleting", Toast.LENGTH_SHORT).show();
                            }
                            askAgain.dismiss();
                            returnIntent.putExtra("DELETED_ITEM_ID", itemID);
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        }
                    });
                    break;

                case R.id.item_info_close_btn:  // close info activity
                    // main 화면으로 돌아감
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                    break;

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == EDIT_ITEM){
            if(resultCode == RESULT_OK){        // 아이템 수정이 되었으면 새로운 액티비티가 켜질 것이므로 이 창은 닫음
                finish();
            }
        }
    }

    public void calcRemainingDates(String exp) {
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
            SimpleDateFormat resultFormat = new SimpleDateFormat("yyyy년 MM달 dd일 남음");
            final int oneDay = 24 * 60 * 60 * 1000;


            Date expDate = dateFormat.parse(exp);

            Calendar calendar = new GregorianCalendar();
            long today = calendar.getTimeInMillis() / oneDay;
            calendar.setTime(expDate);
            long expire = calendar.getTimeInMillis() / oneDay;

            Date remaining = new Date(expire - today);


            remainingDates.setText(resultFormat.format(remaining));
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
    }
}
