package com.alex.content.foldable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.alex.content.R;
import java.util.ArrayList;

/**
 * @author: duxingyu
 * @e-mail: duxy@13322.com
 * @time: 2018/8/17 15:33
 * @desc: 可折叠内容的控件
 * @version:
 **/

public class FoldableTextView extends View {

  public static final String TAG="TAG_FoldableTextView";
  /**
   * 画笔，默认内容，标签
   * */
  private TextPaint paintDef,paintTag;
  /**
   * 内容颜色，标题颜色
   * */
  private int colorText,colorTag;
  /**
   * 字体尺寸
   * */
  private float textSize;
  /**
   * 临界值
   * */
  private int criticalLineCount;
  /**
   * 行间距
   * */
  private float intervalSpace;
  /**
   * 实际宽度
   * */
  private float drawTextWidth;
  /**
   * 是否测量完毕
   * */
  private boolean isMeasureOver =false;
  /**
   * 是否需要显示隐藏开关
   * */
  private boolean needShowClose =true;
  /**
   * 开始绘制的起始x坐标和起始y坐标
   * */
  private float startX,startY;
  /**
   * 绘制的内容
   * */
  private String content;
  /**
   * 收起、展开标记
   * */
  private String tagFold ="&#160;收起",tagUnfold="...展开";
  /**
   * 存放所有字符串的列表
   * */
  private ArrayList<String> strList;
  /**
   * 每行的高度
   * */
  private float textLineHeight;
  /**
   * 所有内容显示完需要的行数
   * */
  private int totalTextLineCount;
  /**
   * 是否显示全部内容，默认不是
   * */
  private boolean isShowAll =false;
  /**
   * 当处于隐藏多余内容的状态下的最小显示条数内容,取最小的值作为显示行数
   * */
  private int minCountWhileInHiden;
  /**
   * 测量text时绘制文字起始基线高度，绘制时要用计算的高度减去这个值，得到的效果才能正确显示
   * */
  private float textOffsetY2MiddleLine;
  /**
   * 绘制的隐藏tag是否在新的一行里。
   * */
  private boolean drawHidenTagInLastLine =false;
  /**
   * 默认显示展开的条数是否大于了实际内容的行数。
   * 按照正常方式绘制即可。
   * */
  private boolean isMinCountMoreThanActuralCount;
  /**
   * 收起显示tag的区域，用于判断点击事件发生的位置
   * */
  private RectF r_hiden,r_show;
  /**
   * 隐藏部分内容下的高度，显示所有内容下的高度
   * */
  private float heightInHidenState,heightInShowState;

  public FoldableTextView(Context context) {
    super(context);
    init(context,null);
  }

  public FoldableTextView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context,attrs);
  }

  public FoldableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context,attrs);
  }

  private void init(Context context, AttributeSet attrs){
    TypedArray array =context.obtainStyledAttributes(attrs, R.styleable.FoldableTextView);
    if(array!=null){
      textSize =array.getDimension(R.styleable.FoldableTextView_fold_text_content_size,15);
      colorText =array.getColor(R.styleable.FoldableTextView_fold_text_content_color, Color.GRAY);
      colorTag =array.getColor(R.styleable.FoldableTextView_fold_text_tag_color, Color.GREEN);
      criticalLineCount =array.getInt(R.styleable.FoldableTextView_fold_text_critical_hiden_line_count,3);
      intervalSpace =array.getFloat(R.styleable.FoldableTextView_fold_text_space_internal,0.2f);
      needShowClose =array.getBoolean(R.styleable.FoldableTextView_fold_text_show_close,true);
      isShowAll =array.getBoolean(R.styleable.FoldableTextView_fold_text_show_all,false);
      tagFold =array.getString(R.styleable.FoldableTextView_fold_text_hiden_text);
      tagUnfold =array.getString(R.styleable.FoldableTextView_fold_text_show_text);
      content =array.getString(R.styleable.FoldableTextView_fold_text_content);

      array.recycle();
    }else{
      textSize =15;
      colorText = Color.GRAY;
      colorTag = Color.GREEN;
      criticalLineCount =3;
      intervalSpace =0.2f;
      needShowClose =true;
      isShowAll =false;
    }
    paintDef =new TextPaint();
    paintTag =new TextPaint();

    paintDef.setAntiAlias(true);
    paintDef.setTextSize(textSize);
    paintDef.setColor(colorText);

    paintTag.setAntiAlias(true);
    paintTag.setTextSize(textSize);
    paintTag.setColor(colorTag);

    paintDef.measureText(tagUnfold);
    textLineHeight =paintDef.descent()-paintDef.ascent();

    strList =new ArrayList<>();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width,height;

    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    if(widthMode == MeasureSpec.EXACTLY){
      drawTextWidth =width =widthSize;
    }else{
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      requestRealWidth(getMeasuredWidth());
      return;
    }
    drawTextWidth =drawTextWidth-getPaddingLeft()-getPaddingRight();
    if(heightMode == MeasureSpec.EXACTLY){
      height =heightSize;
    }else{
      measureTextAndSize();
      height = (int) (getActualHeight()+1);
    }
    startX =getPaddingLeft();
    setMeasuredDimension(width,height);
  }
  /**
   * 获取控件的实际宽度,这是在未设置宽度时需要调用的方法
   * */
  private void requestRealWidth(float width){
    if(isMeasureOver){
      return;
    }
    if(drawTextWidth!=width){
      drawTextWidth =width;
    }else{
      startX =getPaddingLeft();
      startY =getPaddingTop();
      drawTextWidth =drawTextWidth -getPaddingLeft()-getPaddingRight();//实际绘制宽度等于有效宽度减去左右内间距
      isMeasureOver =true;
      setText(content);
    }
  }

  /**
   * 设置文字内容
   * */
  public void setText(String content){
    if(TextUtils.isEmpty(content)){
      return;
    }
    this.content =content;
    if(isMeasureOver){
      measureTextAndSize();
      resetHeight();
      return;
    }
    if(!isMeasureOver){
      isMeasureOver =true;
    }
  }

  public void setCurrentState(boolean isShowAll){
    this.isShowAll =isShowAll;
  }
  /**
   * 测量相关参数
   * */
  private void measureTextAndSize(){
    if(drawTextWidth==0){
      return;
    }
    if(TextUtils.isEmpty(content)){
      totalTextLineCount =0;
      return;
    }
    strList.clear();

    float totalTextWidth =paintDef.measureText(content);

    textLineHeight =paintDef.descent() -paintDef.ascent();
    textOffsetY2MiddleLine =-paintDef.ascent();
    startY =getPaddingTop()+textOffsetY2MiddleLine;

    totalTextLineCount = (int) Math.ceil(totalTextWidth/drawTextWidth);
    if(totalTextLineCount*drawTextWidth<totalTextLineCount&&totalTextLineCount<drawTextWidth*(totalTextLineCount+1)){
      totalTextLineCount ++;
    }
    String textTemp =content;

    measureTextGroup(textTemp);

    isMinCountMoreThanActuralCount =criticalLineCount>=strList.size();//是否大于等于实际的条数，那么，此时显示隐藏、展开开关就已经没有意义了。
    int count =strList.size();

    //重新计算控件的整体高度
    if(isMinCountMoreThanActuralCount){//此种情况下，隐藏和显示内容的高度都是一致的。
      heightInShowState =heightInHidenState =textLineHeight*(count-1)*intervalSpace+ textLineHeight*count +getPaddingTop()+ getPaddingBottom();
    }else{
      //获取隐藏状况下的高度。
      heightInHidenState = textLineHeight*criticalLineCount+textLineHeight*criticalLineCount*intervalSpace +getPaddingTop()+ getPaddingBottom();

      //在获取显示时的所有高度时，需要注意最后一行的宽度
      //如果最后一行的字符串宽度加上标记的字符串的宽度已经超过了最大可绘制宽度，那么就要新增一行，用于放置标记,因此高度同样要增加。
      String lastLineText =strList.get(strList.size()-1);
      float lastWidth =paintDef.measureText(lastLineText+tagFold);

      //如果最后一行的宽度大于了实际可绘制宽度，那么就要新增一行专门用于存放收起标记
      if(lastWidth>drawTextWidth){
        strList.add(tagFold);
        drawHidenTagInLastLine =true;
        count++;
      }
      heightInShowState =textLineHeight*count+textLineHeight*count*intervalSpace +getPaddingTop()+ getPaddingBottom();
    }
    //确定显示行数的最小值
    minCountWhileInHiden =criticalLineCount>strList.size()?strList.size():criticalLineCount;
  }

  /**
   * 使用系统自带的api裁剪字符串
   * */
  private void calculationTextWithPaint(String textTemp){
    //paintDef.breakText(textTemp,)
  }

  private void measureTextGroup(String textTemp){
    //以所有字符均为同一类型来计算每行显示字符的个数,这只是一个期望值而已
    int expectLineCharCount = content.length()/ totalTextLineCount;
    int lastLineEndCharIndex =0;//上一行最后一个字符的索引值

    float actualWidth;
    String temp;
    for(int i=0;i<totalTextLineCount;i++){
      if(i==totalTextLineCount-1){//当达到最后一行时
        int lastIndex =lastLineEndCharIndex+expectLineCharCount;
        if(lastIndex<textTemp.length()){
          temp =textTemp.substring(lastLineEndCharIndex,lastIndex);
        }else{
          temp =textTemp.substring(lastLineEndCharIndex,textTemp.length());
        }
        actualWidth =paintDef.measureText(temp);
        if(actualWidth<drawTextWidth){
          do{
            lastIndex ++;
            if(lastIndex>=textTemp.length()){
              break;
            }
            actualWidth =paintDef.measureText(textTemp,lastLineEndCharIndex,lastIndex);
            if(actualWidth>drawTextWidth){
              lastIndex --;//当测试的长度超过绘制宽度后，需要回退一个字符
              break;
            }
          }while (actualWidth<drawTextWidth);
        }else{
          do{
            lastIndex --;
            actualWidth =paintDef.measureText(textTemp,lastLineEndCharIndex,lastIndex);
            if(actualWidth<=drawTextWidth){
              break;
            }
          }while(actualWidth>drawTextWidth);
        }
        strList.add(temp);
        if(lastIndex<textTemp.length()-1){
          strList.add(textTemp.substring(lastIndex,textTemp.length()));
        }
      }else{
        int curLineEndCharIndex =lastLineEndCharIndex+expectLineCharCount;//当前行最后一个字符的位置,是基于上已行文字的最后位置
        actualWidth =paintDef.measureText(textTemp,lastLineEndCharIndex,curLineEndCharIndex);//根据期望值测量出来的实际长度
        if(actualWidth>drawTextWidth){
          do{
            curLineEndCharIndex --;
            actualWidth =paintDef.measureText(textTemp,lastLineEndCharIndex,curLineEndCharIndex);
            if(actualWidth<=drawTextWidth){
              break;
            }
          }while(actualWidth>drawTextWidth);
        }else{
          do{
            curLineEndCharIndex ++;
            actualWidth =paintDef.measureText(textTemp,lastLineEndCharIndex,curLineEndCharIndex);
            if(actualWidth>drawTextWidth){
              curLineEndCharIndex --;//当测试的长度超过绘制宽度后，需要回退一个字符
              break;
            }
          }while (actualWidth<drawTextWidth);
        }
        temp =textTemp.substring(lastLineEndCharIndex,curLineEndCharIndex);
        lastLineEndCharIndex =curLineEndCharIndex;//将当前行的最后一个字符索引赋值为当前的最后一行的最后一个字符
        strList.add(temp);
      }
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    float tempY =startY;//Y轴的起始坐标
    if(isMinCountMoreThanActuralCount){//正常绘制即可，不需要考虑绘制tag了。
      for(String each:strList){
        tempY =drawTextStr(canvas,each,startX,tempY,paintDef);
      }
    }else{
      if(isShowAll){//如果是展示全部内容
        if(!needShowClose){//但是不需要显示收起的标签，那么此时控件不再具有收起功能，只是一个普通显示文本内容的控件。
          for(String each:strList){
            tempY =drawTextStr(canvas,each,startX,tempY,paintDef);
          }
        }else{
          int count =strList.size();
          if(drawHidenTagInLastLine){//表示最后一行才是隐藏标签
            float lastTemp;
            for(int i=0;i<count-1;i++){
              tempY =drawTextStr(canvas,strList.get(i),startX,tempY,paintDef);
            }
            lastTemp =tempY;
            drawTextStr(canvas,strList.get(count-1),startX,tempY,paintTag);
            r_hiden =new RectF(startX,tempY+paintDef.ascent(),lastTemp+paintTag.measureText(tagFold),lastTemp+paintDef.descent());
          }else{//表示隐藏标签跟在最后一行的末尾
            float lastLineWidth=0;
            float lastLineY=0;
            for(int i=0;i<count;i++){
              String text =strList.get(i);
              if(i==count-1){
                lastLineY =tempY;
                lastLineWidth =paintDef.measureText(text);
              }
              tempY =drawTextStr(canvas,strList.get(i),startX,tempY,paintDef);
            }
            drawTextStr(canvas,tagFold,startX+lastLineWidth,lastLineY,paintTag);
            r_hiden =new RectF(startX+lastLineWidth,lastLineY+paintDef.ascent(),startX+lastLineWidth+paintTag.measureText(tagFold),lastLineY+paintDef.descent());
          }
        }
      }else{
        float lastLineY;
        for(int i=0;i<minCountWhileInHiden;i++){
          String text =strList.get(i);
          if(i==minCountWhileInHiden-1){
            text =text.substring(0,text.length()-tagUnfold.length());//是为了避免某些宽的字导致绘制显示不完全的情况
            lastLineY =tempY;
            float textWidth =paintDef.measureText(text);
            drawTextStr(canvas,text,startX,tempY,paintDef);
            drawTextStr(canvas,tagUnfold,startX+textWidth,lastLineY,paintTag);
            float tagUnfoldWidth =paintTag.measureText(tagUnfold);
            r_show =new RectF(startX+textWidth,lastLineY+paintTag.ascent(),startX+textWidth+tagUnfoldWidth,lastLineY+paintTag.descent());
          }else{
            tempY =drawTextStr(canvas,text,startX,tempY,paintDef);
          }
        }
      }
    }
  }
  @Override public boolean onTouchEvent(MotionEvent event) {
    if(isMinCountMoreThanActuralCount||(isShowAll&&!needShowClose)){
      return super.onTouchEvent(event);
    }
    switch (event.getAction()){
      case MotionEvent.ACTION_UP:
        isInArea(event.getX(),event.getY());
        break;
    }
    return true;
  }
  public String getText(){
    return content;
  }

  /**
   * 是否在点击区
   * */
  private void isInArea(float x,float y){
    boolean traggier =isShowAll?x>r_hiden.left&&x<r_hiden.right&&y>r_hiden.top&&y<r_hiden.bottom:x>r_show.left&&x<r_show.right&&y>r_show.top&&y<r_show.bottom;
    if(traggier){
      isShowAll =!isShowAll;
      resetHeight();
    }
  }

  /**
   * 绘制每行文字
   * */
  private float drawTextStr(Canvas canvas, String text,float startX,float startY, Paint paint){
    canvas.drawText(text,startX,startY,paint);
    return startY+textLineHeight+textLineHeight*intervalSpace;
  }

  /**
   * 获取实际高度
   * */
  private float getActualHeight(){
    float actualHeight =isShowAll?heightInShowState:heightInHidenState;
    return actualHeight;
  }
  /**
   * 重新设置高度
   * */
  private void resetHeight(){
    getLayoutParams().height = (int) getActualHeight();
    requestLayout();
  }
}
