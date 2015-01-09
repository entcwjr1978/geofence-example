package com.lightcyclesoftware.goldenfrogcodesample;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ewilliams on 1/8/15.
 */
public class DisplayToast implements Runnable {
    private final Context mContext;
    String mText;

    public DisplayToast(Context mContext, String text){
        this.mContext = mContext;
        mText = text;
    }

    public void run(){
        Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
    }
}
