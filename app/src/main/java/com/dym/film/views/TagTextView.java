package com.dym.film.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.BaseMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/19
 */
public class TagTextView extends CustomTypefaceTextView
{
    public final static String TAG = "TagTextView";

    private TagClickListener mClickListener = null;

    public TagTextView(Context context)
    {
        this(context, null, 0);
    }

    public TagTextView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public void setTagClickListener(TagClickListener listener)
    {
        mClickListener = listener;
        setMovementMethod(mClickMovementMethod);
    }

    public void setTagText(ArrayList<String> tags, String content)
    {
        if (tags == null || tags.isEmpty()) {
            setText(content == null ? "" : content);
            return;
        }

        setText("");
        for (String tag : tags) {
            String ctag = "#" + tag + "# ";
            SpannableString spannableString = new SpannableString(ctag);
            spannableString.setSpan(new TagClickableSpan(tag), 0, ctag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            this.append(spannableString);
        }

        this.append(content);

//        TODO: 超出三行
//        int count = getLineCount();
//        if (count >= 3) {
//            Rect rect = new Rect();
//            getLineBounds(3, rect);
//        }
//
    }

    private class TagClickableSpan extends ClickableSpan
    {
        private String mTag = "";

        public TagClickableSpan(String tag)
        {
            mTag = tag;
        }

        @Override
        public void updateDrawState(TextPaint ds)
        {
            super.updateDrawState(ds);
            ds.setColor(Color.parseColor("#FDFDFE"));       //设置文件颜色
            ds.setUnderlineText(false);      //设置下划线
        }

        @Override
        public void onClick(View widget)
        {
            //Loge(TAG, "On Tag Clicked: " + mTag);

            if (mClickListener != null) {
                mClickListener.onTagClicked(mTag);
            }
        }
    }

    public interface TagClickListener
    {
        void onTagClicked(String tag);
    }

    private final static MovementMethod mClickMovementMethod = new MovementMethod()
    {
        @Override
        public void initialize(TextView widget, Spannable text)
        {

        }

        @Override
        public boolean onKeyDown(TextView widget, Spannable text, int keyCode, KeyEvent event)
        {
            return false;
        }

        @Override
        public boolean onKeyUp(TextView widget, Spannable text, int keyCode, KeyEvent event)
        {
            return false;
        }

        @Override
        public boolean onKeyOther(TextView view, Spannable text, KeyEvent event)
        {
            return false;
        }

        @Override
        public void onTakeFocus(TextView widget, Spannable text, int direction)
        {

        }

        @Override
        public boolean onTrackballEvent(TextView widget, Spannable text, MotionEvent event)
        {
            return false;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event)
        {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    }
//                    else if (action == MotionEvent.ACTION_DOWN) {
//                        Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
//                    }

                    return true;
                }
                else {
                    Selection.removeSelection(buffer);
                }
            }
            return false;
        }

        @Override
        public boolean onGenericMotionEvent(TextView widget, Spannable text, MotionEvent event)
        {
            return false;
        }

        @Override
        public boolean canSelectArbitrarily()
        {
            return false;
        }
    };
}
