package com.alex.content.title;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import com.alex.content.R;

/**
 * @author: duxingyu
 * @e-mail: duxy@13322.com
 * @time: 2019/3/26 17:49
 * @desc: 可跳转的textView
 * @version:
 **/

public class ClickableTextView extends AppCompatTextView {
  private String preSuffix;
  private String endSuffix;
  private int clickColor;

  public ClickableTextView(Context context) {
    super(context);
    init(null);
  }

  public ClickableTextView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ClickableTextView);
    init(array);
  }

  public ClickableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ClickableTextView);
    init(array);
  }

  private void init(TypedArray array) {
    if (array != null) {
      preSuffix = array.getString(R.styleable.ClickableTextView_clickable_pre_suffix_text);
      endSuffix = array.getString(R.styleable.ClickableTextView_clickable_end_suffix_text);
      clickColor =
          array.getColor(R.styleable.ClickableTextView_clickable_click_text_color, Color.BLUE);
      array.recycle();
    } else {
      preSuffix = "在\u0000\u0000";
      endSuffix = "\u0000\u0000中点赞了";
      clickColor = Color.BLUE;
    }
    setSingleLine();
  }

  public void setClickableText(String preSuffix, String nextSuffix, String clickText) {
    SpannableString sps = new SpannableString(preSuffix + clickText + nextSuffix);
    sps.setSpan(new ClickableSpan() {
                  @Override public void onClick(View view) {
                    if (lisenter != null) {
                      lisenter.toWebViews(url);
                    }
                  }

                  @Override public void updateDrawState(TextPaint ds) {
                    ds.setColor(clickColor);
                    ds.setUnderlineText(false);//该方法用于判断是否需要显示下划线
                  }
                }, preSuffix.length(), preSuffix.length() + clickText.length(),
        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

    setMovementMethod(LinkMovementMethod.getInstance());
    setText(sps);
  }

  public static final String TAG = "TAG_ClickableTextView";
  private String content;

  /**
   * 设置可点击的字符串
   */
  public void setClickableText(String clickText) {
    this.content = clickText;
    final ViewTreeObserver observer = getViewTreeObserver();
    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        setShowTitleText();
      }
    });
  }

  /**
   * 设置需要显示的内容
   */
  private void setShowTitleText() {

    float width = getWidth();//当前的控件宽度
    TextPaint paint = getPaint();

    String suffix = preSuffix + endSuffix;
    float suffixWidth = paint.measureText(suffix);//前后缀的总共长度

    float maxTitleWidth = width - suffixWidth - getPaddingLeft() - getPaddingRight();//这是标题能绘制的最大宽度
    float actualTitleWidth = paint.measureText(content);//标题全部内容的实际宽度

    if (maxTitleWidth < actualTitleWidth) {//表示标题过长，需要处理
      String instead = "...";
      float insteadWidth = paint.measureText(instead);
      maxTitleWidth -= insteadWidth;//此时最大的可绘制宽度要减去替代的字符串长度

      float charWidht = suffixWidth / suffix.length();//单个字符的长度
      int expectedCount = (int) (maxTitleWidth / charWidht);//字符串长度对应期望值下的字符个数
      float expectedWidth = paint.measureText(content, 0, expectedCount);

      if (expectedWidth > maxTitleWidth) {//如果当前的期望值字符个数对应字符串长度大于最大可绘制的宽度，那么要减少字符个数
        do {
          expectedCount--;
          if (paint.measureText(content, 0, expectedCount) < maxTitleWidth) {
            break;
          }
        } while (expectedWidth > maxTitleWidth);
      } else {
        do {
          expectedCount++;
          if (paint.measureText(content, 0, expectedCount) > maxTitleWidth) {
            expectedCount--;
            break;
          }
        } while (expectedWidth < maxTitleWidth);
      }
      content = content.substring(0, expectedCount);
      content += instead;
      setClickableText(preSuffix, endSuffix, content);
    } else {
      setClickableText(preSuffix, endSuffix, content);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        Object text = getText();
        if (text instanceof Spanned) {//处理当控件本身存在点击事件时，超链接的点击事件和点击事件本身相冲突的问题
          Spannable spannable = (Spannable) text;
          int x = (int) event.getX();
          int y = (int) event.getY();
          x -= getTotalPaddingLeft();
          y -= getTotalPaddingTop();
          x += getScrollX();
          y += getScrollY();
          Layout layout = getLayout();
          int line = layout.getLineForVertical(y);
          int off = layout.getOffsetForHorizontal(line, x);
          ClickableSpan[] link = spannable.getSpans(off, off, ClickableSpan.class);
          if (link != null && link.length > 0) {
            return true;
          }
        }
        break;
    }
    return super.onTouchEvent(event);
  }

  /**
   * 设置前缀字符串
   */
  public ClickableTextView setPreSuffix(String preSuffix) {
    this.preSuffix = preSuffix;
    return this;
  }

  /**
   * 设置后缀字符串
   */
  public ClickableTextView setEndSuffix(String endSuffix) {
    this.endSuffix = endSuffix;
    return this;
  }

  private String url;

  /**
   * 设置跳转url
   */
  public ClickableTextView setUrl(String url) {
    this.url = url;
    return this;
  }

  private ActionLisenter lisenter;

  public void setLisenter(ActionLisenter lisenter) {
    this.lisenter = lisenter;
  }
  public boolean getSetListener(){
    return lisenter ==null;
  }
  public interface ActionLisenter {
    void toWebViews(String url);
  }
}
