package com.wuxianggujun.lcnetdiskapi.utils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.graphics.Color;
import android.view.View.OnClickListener;
import android.text.TextPaint;

/**
 * @作者: 无相孤君
 * @QQ: 3344207732
 * @描述:    
 */
public class TextClickSpan extends ClickableSpan {

    private int mHighLightColor = Color.RED;
    private boolean mUnderLine = false;
    private View.OnClickListener mClickListener;

    public TextClickSpan(int mHighLightColor, boolean mUnderLine, View.OnClickListener mClickListener) {
        this.mHighLightColor = mHighLightColor;
        this.mUnderLine = mUnderLine;
        this.mClickListener = mClickListener;
    }

    public TextClickSpan(View.OnClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }
    
    @Override
    public void onClick(View v) {
        if(mClickListener!=null){
            mClickListener.onClick(v);
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
       ds.setColor(mHighLightColor);
       ds.setUnderlineText(mUnderLine);
    }
    
    
    
    
    
}
