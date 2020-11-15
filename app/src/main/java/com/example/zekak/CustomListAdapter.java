package com.example.zekak;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zekak.AppMain.Item;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.zekak.AppMain.imagePath;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Item}.
 */
/// 11/10 안드로이드스튜디오 file > new > fragment > fragment(list) 샘플코드 참고



public class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.ViewHolder> {
    public List<Item> itemDisplay = new ArrayList<>();           // 현재 디스플레이되고 있는 아이템들(카테고리든, 전체든)
    Context context;
    String targetCategory = null; // 이 어댑터에서 보여줘야 할 카테고리, 이렇게 안하면 전체 아이템만 보이고 갱신이 안됨

    public interface OnListItemSelectedInterface {
        void onItemSelected(int itemID, View v, int position);
    }

    public interface OnListItemLongSelectedInterface {
        void onItemLongHold(int itemID, View v, int position);
    }

    public interface OnListItemSwipedInterface {
        void onItemSwipe(int itemID, View v, int position);
    }

    private OnListItemSelectedInterface clickListener;
    private OnListItemLongSelectedInterface holdListener;
    private OnListItemSwipedInterface swipeListener;

    public CustomListAdapter(Context context, String currentCategory, List<Item> items, OnListItemSelectedInterface clickListener, OnListItemLongSelectedInterface holdListener, OnListItemSwipedInterface swipeListener) {
        this.context = context;
        targetCategory = currentCategory;
        if(items != null){
            itemDisplay = items;
        }
        this.clickListener = clickListener;
        this.holdListener = holdListener;
        this.swipeListener = swipeListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {          // 새로운 뷰 만들기(하나의 아이템 탭)
        View viewHolder = LayoutInflater.from(context)
                .inflate(R.layout.item_tab, parent, false);
        return new ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {           // 아이템 탭에 내용 매핑

        if(itemDisplay.get(position).category.equals(targetCategory)){              // 설정한 카테고리에 있는 것들만 매핑
            holder.itemID = itemDisplay.get(position).id;

            // (CODE: 포토) 파일 경로에서 파일 이름 append해서 이미지 가져옴(절대경로X)
            String path = imagePath.getPath() + "/"+itemDisplay.get(position).photo;
            holder.iImageView.setImageBitmap(BitmapFactory.decodeFile(path));

            //Item.portion의 0# 같은 형태를 setProgress()에 적용
            int divided = itemDisplay.get(position).portion / 10;   // 사용자 1회분 설정값
            int used = itemDisplay.get(position).portion - divided * 10; // 사용량

            holder.mItem = itemDisplay.get(position);
            ///////임시 holder.iImageView.setImageBitmap(originalBm);
            holder.iNameView.setText(itemDisplay.get(position).name);
            holder.iExpView.setText(itemDisplay.get(position).exp);
            holder.iPortionView.setProgress(used/divided * 100);
            if(itemDisplay.get(position).flag){     // 핀 ON인 경우(default: OFF)
                holder.iPinView.setVisibility(View.VISIBLE);

           }
        }
    }

    // '하나의 아이템 탭'의 view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View iTabView;

        public final ImageView iImageView;
        public final TextView iNameView;
        public final TextView iExpView;
        public final ProgressBar iPortionView;
        public final ImageView iPinView;

        public Item mItem;
        public int itemID;  // 각 아이템 탭에 대한 리스너 처리를 위한 식별 용도(Item.id 값), 이거 안하면 리사이클러 갱신 불가
        public boolean categoryFit; // 현재 카테고리에 속해 있는 아이템인지 구분하기 위해

        public ViewHolder(View singleTabVew) {      // 하나의 아이탭 탭 뷰 의미
            super(singleTabVew);
            iTabView = singleTabVew;
            iImageView = singleTabVew.findViewById(R.id.item_image);
            iNameView = singleTabVew.findViewById(R.id.item_name);
            iExpView = singleTabVew.findViewById(R.id.exp_date);
            iPortionView = singleTabVew.findViewById(R.id.portion_bar);
            iPinView = singleTabVew.findViewById(R.id.pin_icon);

            singleTabVew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemSelected(itemID, v, getAdapterPosition());

                }
            });

            singleTabVew.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holdListener.onItemLongHold(itemID, v, getAdapterPosition());
                    return false;
                }
            });

            singleTabVew.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    swipeListener.onItemSwipe(itemID, v, getAdapterPosition());
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {     // 현재 보이는 리스트 아이템 수 정의(필수)
        return itemDisplay.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
        // or
        //AppMain.ITEM_MAP.get(?);

    }
}