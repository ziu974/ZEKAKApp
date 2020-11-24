package com.example.zekak;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

// 11/10 아이템 구조체 정의
// 이거 별도의 클래스로 뺌(11/21) (너무 길어 ㅜㅜ & 아이템 관리 불편)
public class Item implements Parcelable {    // intent의 extra에 이 구조체를 주고받기 위해 Parcelable
    public final int id;
    public final String name;
    public final String product;
    public final String barcode;
    public final String exp;
    public final int portion;
    public final String category;
    public final String photo;
    public final String memo;
    public final boolean flag;

    // Item 객체 만들 때 사용
    public Item(int id, String name, String product, String barcode, String exp, int portion, String category, String photo, String memo, boolean flag) {
        this.id = id;
        this.name = name;
        this.product = product;
        this.barcode = barcode;
        this.exp = exp;
        this.portion = portion;
        this.category = category;
        this.photo = photo;
        this.memo = memo;
        this.flag = flag;
    }

    // intent로 넘어온 Item 객체를 처리할 때 사용
    public Item(Parcel src){
        id = src.readInt();
        name = src.readString();
        product = src.readString();
        barcode  = src.readString();
        exp = src.readString();
        portion = src.readInt();
        category = src.readString();
        photo = src.readString();
        memo = src.readString();
        if(Build.VERSION.SDK_INT >= 29)
            flag = src.readBoolean();
        else {
            flag = (boolean) (src.readByte() == 1);  // readBoolean 버전 문제: boolean으로 다시 변환
        }
    }


    // Parcelable 구조체에 기록하는 함수 (주의: 순서 같아야 함)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.product);
        dest.writeString(this.barcode);
        dest.writeString(this.exp);
        dest.writeInt(this.portion);
        dest.writeString(this.category);
        dest.writeString(this.photo);
        dest.writeString(this.memo);
        if(Build.VERSION.SDK_INT >= 29)
            dest.writeBoolean(this.flag);
        else {
            dest.writeByte((byte)(this.flag ? 1 : 0));  // writeBoolean 버전 문제: byte로 변환
        }
    }

    // Parcelable을 생성하는 코드: 'CREATOR' (필수)
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>(){
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    public long getId() {
        return id;
    }       // RecyclerView에서 사용
}