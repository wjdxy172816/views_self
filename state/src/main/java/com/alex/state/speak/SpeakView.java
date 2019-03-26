package com.alex.state.speak;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.alex.state.R;
import com.alex.utils.DecodeBitmapUtil;

/**
 * @author: duxingyu
 * @e-mail: duxy@13322.com
 * @time: 2018/8/10 16:15
 * @desc: 语音控件
 * @version:
 **/

public class SpeakView extends View {

  public static final String TAG="TAG_SpeakView";
  /**
   * 图片的尺寸
   * */
  private float s_centImgs;
  /**
   * 宽、高
   * */
  private float width,height;
  /**
   * 颜色，内圈、外圈
   * */
  private int c_inner,c_outer;
  /**
   * 最大直径，内圈、外圈、涟漪
   * */
  private float d_inner,d_outer,d_ripple;
  /**
   * 背景图片，默认、讲话时
   * */
  private Bitmap b_def,b_speaking;
  /**
   * 涟漪色对应的rgb值
   * */
  private int red,green,blue;
  /**
   * 画笔，外圈、内圈、和画图片的
   * */
  private Paint p_outer,p_inner,p_imgs,p_ripple,p_rec;
  /**
   * 当前内圆半径
   * */
  private float coor_innerDynamic,coor_rippleDynamic;
  /**
   * 内外半径之差,涟漪与外圆半径之差
   * */
  private float d_outer2Ineer,d_ripple2Outer;
  /**
   * 当前涟漪色的alpha值
   * */
  private float alphaRipple;
  /**
   * 圆心的横纵坐标
   * */
  private float coordinate_X,coordinate_Y;
  /**
   * 是否已经进入到涟漪效果
   * */
  private boolean isRippleState =false;
  /**
   * 某些动态元素的范围
   * */
  private float iLeft,iTop,iRight,iBottom,iHeight,iDynamic;
  /**
   * 提示文字的内容
   * */
  private String textSpeak,textRec;
  /**
   * 绘制外部进度圈的范围
   * */
  private RectF rectRec;
  /**
   * 扫过的角度
   * */
  private float sweepDegree =0;
  /**
   * 图片区域的范围
   * */
  private RectF rectImgs;
  /**
   * 是否处于绘制外部进度圈的状态
   * */
  private boolean drawRecCircle =false;
  /**
   * 外部进度圈与外圈的间距
   * */
  private float recCricleWidth =10;
  /**
   * 语音输入的模式
   * */
  private int speakMode;
  /**
   * 默认是非按压式的
   * */
  public static final int MODE_UNPRESS=0;
  /**
   * 按着说话
   * */
  public static final int MODE_PRESS=1;

  public SpeakView(Context context) {
    super(context);
    init(context,null);
  }

  public SpeakView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public SpeakView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs){
    TypedArray array =context.obtainStyledAttributes(attrs, R.styleable.SpeakView);
    float density =context.getResources().getDisplayMetrics().density;
    if(array!=null){
      width =array.getDimension(R.styleable.SpeakView_speak_width,25);
      height =array.getDimension(R.styleable.SpeakView_speak_height,25);
      c_inner =array.getColor(R.styleable.SpeakView_speak_circle_color_inner, Color.BLUE);
      c_outer =array.getColor(R.styleable.SpeakView_speak_circle_color_outter, Color.GREEN);
      d_inner =array.getDimension(R.styleable.SpeakView_speak_diameter_inner,10)/2;
      d_outer =array.getDimension(R.styleable.SpeakView_speak_diameter_outter,20)/2;
      d_ripple =array.getDimension(R.styleable.SpeakView_speak_diameter_ripple,25)/2;
      s_centImgs =array.getDimension(R.styleable.SpeakView_speak_img_size,10);
      int resDef =array.getResourceId(R.styleable.SpeakView_speak_center_img_def,R.mipmap.ic_speak_def);
      int resSpeaking =array.getResourceId(R.styleable.SpeakView_speak_center_img_speaking,R.mipmap.ic_speak_speaking);
      b_def = DecodeBitmapUtil.decodeBitmapFromResources(getResources(),resDef,(int)s_centImgs, (int) s_centImgs);
      b_speaking =DecodeBitmapUtil.decodeBitmapFromResources(getResources(),resSpeaking,(int)s_centImgs, (int) s_centImgs);

      textRec =array.getString(R.styleable.SpeakView_speak_prompt_text_rec);
      textSpeak =array.getString(R.styleable.SpeakView_speak_prompt_text_speak);

      speakMode =array.getInt(R.styleable.SpeakView_speak_mode,MODE_UNPRESS);

      array.recycle();
    }else{
      width =height =25*density;
      c_inner = Color.BLUE;
      c_outer = Color.GREEN;
      d_inner = d_outer =10*density;
      d_ripple =15*density;
      s_centImgs =10*density;
      b_def =DecodeBitmapUtil.decodeBitmapFromResources(getResources(),R.mipmap.ic_speak_def,(int)s_centImgs, (int) s_centImgs);
      b_speaking =DecodeBitmapUtil.decodeBitmapFromResources(getResources(),R.mipmap.ic_speak_speaking,(int)s_centImgs, (int) s_centImgs);

      textSpeak ="语音中";
      textRec ="识别中";
      speakMode =MODE_UNPRESS;
    }
    strTags =new String[]{".","..","..."};
    resolveColor();

    p_imgs =new Paint();
    p_inner =new Paint();
    p_outer =new Paint();
    p_ripple =new Paint();
    p_rec =new Paint();
    initPaint(p_imgs,p_inner,p_outer,p_rec);
    p_outer.setColor(c_outer);
    p_inner.setColor(c_inner);

    p_rec.setStyle(Paint.Style.STROKE);
    p_rec.setStrokeWidth(5);
    p_rec.setColor(c_inner);

    coordinate_X =width/2;
    coordinate_Y =width/2;

    coor_innerDynamic =d_inner;
    coor_rippleDynamic =d_outer;
    d_outer2Ineer =d_outer-d_inner;
    d_ripple2Outer =d_ripple -d_outer;

    float imgStartX,imgStartY,imgEndX,imgEndY;
    imgStartX =(width-s_centImgs)/2;
    imgStartY =(width-s_centImgs)/2;
    imgEndX =imgStartX+s_centImgs;
    imgEndY =imgStartY+s_centImgs;
    rectImgs =new RectF(imgStartX,imgStartY,imgEndX,imgEndY);
    rectRec =new RectF(coordinate_X-d_outer-recCricleWidth,coordinate_Y-d_outer-recCricleWidth,coordinate_X+d_outer+recCricleWidth,coordinate_X+d_outer+recCricleWidth);

    //下面的具体数值是基于提供资源中测量出来的各元素所占比例，美工是个好同志，嗯，芦苇同学是个好同志，觉悟高。
    iLeft =coordinate_X -s_centImgs*13/100;
    iRight =coordinate_X +s_centImgs*13/100;
    iTop =coordinate_Y -s_centImgs*32/100;
    iBottom =coordinate_Y +s_centImgs*14/100;
    iHeight =s_centImgs*46/100;
    iDynamic =iBottom;
  }

  private void initPaint(Paint...paints){
    for(Paint each:paints){
      each.setStyle(Paint.Style.FILL);
      each.setStrokeCap(Paint.Cap.ROUND);
      each.setStrokeWidth(1);
      each.setAntiAlias(true);
    }
  }

  /**
   * 分解涟漪色
   * */
  private void resolveColor(){
    red =(0xff0000&c_inner)>>16;
    green =(0xff00&c_inner)>>8;
    blue =0xff&c_inner;
  }

  /**
   * 合成新的涟漪色
   * @param alpha 透明度
   * */
  @RequiresApi(api = Build.VERSION_CODES.O) private int compoundRippleColor(float alpha){
    if(alpha<=0){
      alpha =0f;
    }
    int alphaColor = (int) (alpha*255);
    return Color.argb(alphaColor,red,green,blue);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension((int)width, (int) height);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  protected void onDraw(Canvas canvas) {

    p_ripple.setColor(compoundRippleColor(alphaRipple));
    canvas.drawCircle(coordinate_X,coordinate_Y,coor_rippleDynamic,p_ripple);
    canvas.drawCircle(coordinate_X,coordinate_Y,d_outer,p_outer);

    canvas.drawCircle(coordinate_X,coordinate_Y,d_inner,p_inner);
    //绘制中心固定的圆
    if(isRippleState){
      canvas.drawCircle(coordinate_X,coordinate_Y,coor_innerDynamic,p_ripple);
    }else{
      canvas.drawCircle(coordinate_X,coordinate_Y,coor_innerDynamic,p_inner);
    }
    canvas.drawBitmap(b_def,null,rectImgs,p_imgs);
    if(!drawRecCircle){
      canvas.clipRect(iLeft,iDynamic,iRight,iBottom, Region.Op.INTERSECT);
      canvas.drawBitmap(b_speaking,null,rectImgs,p_imgs);
    }else{
      canvas.drawArc(rectRec,-90,sweepDegree,false,p_rec);
    }
  }

  ObjectAnimator animator;

  /**
   * 开始语音录入
   * */
  public void startSpeak(){
    if(animator!=null&&animator.isRunning()){
      return;
    }
    animator =new ObjectAnimator();
    animator.setDuration(3000);
    animator.setRepeatCount(50);
    animator.setInterpolator(new LinearInterpolator());
    animator.setFloatValues(0,1f,2f);
    notifyText(TYPE_YY);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float temp = (float) animation.getAnimatedValue();
        if(temp<=1.0f){
          isRippleState =false;
          coor_innerDynamic =d_inner+temp*d_outer2Ineer;
          iDynamic=iBottom-iHeight*temp;
        }else{
          alphaRipple =2-temp;
          if(!isRippleState){//表示刚进入到涟漪效果
            coor_innerDynamic =d_outer;//此时要将内圆半径归零到最大半径处
            iDynamic =iBottom;
            notifyText(TYPE_YY);
          }
          isRippleState =true;
          //绘制涟漪效果,渐变逐渐扩大变暗的半圆
          coor_rippleDynamic =d_outer+(1-alphaRipple)*d_ripple2Outer;
          iDynamic =iBottom-iHeight*alphaRipple;
          if(temp==2f){
            recoveryState();
            notifyText(TYPE_YY);
          }
        }
        invalidate();
      }
    });
    animator.setRepeatMode(ObjectAnimator.RESTART);
    animator.start();
  }

  /**
   * 这个接口供外部调用，目的是直接置为相反状态
   * */
  public boolean getInReconize(){
    //inReconizing =!inReconizing;
    return inReconizing;
  }

  public void setInReconizing(boolean inReconizing){
    this.inReconizing =inReconizing;
  }
  private boolean inReconizing =false;
  /**
   * 停止语音录入
   * */
  public void stopSpeakAndRec(){
    stopSpeak();
    startDrawRecCircle();
  }
  /**
   * 取消动画并恢复状态
   * */
  public void stopSpeak(){
    if(animator==null){
      return;
    }
    animator.cancel();
    animator =null;
    recoveryState();
  }

  /**
   * 计时器
   * */
  private long startTimer;

  @Override public boolean onTouchEvent(MotionEvent event) {
    if(speakMode ==MODE_PRESS){
      switch (event.getAction()){
        case MotionEvent.ACTION_DOWN:
          startTimer = System.currentTimeMillis();
          startSpeak();
          break;
        case MotionEvent.ACTION_UP:
          long curTimer = System.currentTimeMillis();
          long distance =curTimer -startTimer;
          if(distance/1000<2){
            stopSpeak();
            showRecordCancel(TYPE_BLINK,TYPE_NO_INPUT);
          }else{
            stopSpeakAndRec();
          }
          break;
        case MotionEvent.ACTION_CANCEL:
          stopSpeak();
          showRecordCancel(TYPE_CANCEL,TYPE_NO_INPUT);
          break;
      }
      return true;
    }else{
      return super.onTouchEvent(event);
    }
  }

  @Override public void setOnClickListener(@Nullable View.OnClickListener l) {
    if(speakMode==MODE_UNPRESS){
      super.setOnClickListener(l);
    }
  }

  /**
   * 绘制外圈
   * */
  private void startDrawRecCircle(){
    drawRecCircle =true;
    ValueAnimator recAnim;
    recAnim =new ValueAnimator();
    recAnim.setDuration(1000);
    recAnim.setInterpolator(new LinearInterpolator());
    recAnim.setFloatValues(0,1f);
    notifyText(TYPE_REC);
    recAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float temp = (float) animation.getAnimatedValue();
        sweepDegree =360*temp;
        if(temp==1.0f){
          drawRecCircle =false;
          notifyText(TYPE_NO_INPUT);
        }
        invalidate();
      }
    });
    recAnim.start();
  }

  /**
   * 状态归零
   * */
  private void recoveryState(){
    coor_innerDynamic =d_outer;//此时要将内圆半径归零到最大半径处
    coor_innerDynamic =d_inner;// 归零
    coor_rippleDynamic =d_outer;//归零
    iDynamic =iBottom;//归零
  }

  /**
   * 无语音输入状态
   * */
  public static final int TYPE_NO_INPUT=0;
  /**
   * 有语音输入状态
   * */
  public static final int TYPE_YY =1;
  /**
   * 语音识别状态
   * */
  public static final int TYPE_REC=2;
  /**
   * 语音识别取消
   * */
  public static final int TYPE_CANCEL=3;
  /**
   * 语音输入时间太短
   * */
  public static final int TYPE_BLINK =4;
  /**
   * 联动通知
   * */
  private void notifyText(int type){
    if(listener==null){
      return;
    }
    switch (type){
      case TYPE_NO_INPUT:
        listener.notifyState("");
        curTextCount=0;//归零
        break;
      case TYPE_YY:
        int mode =curTextCount%3;
        listener.notifyState(textSpeak+strTags[mode]);
        curTextCount++;
        break;
      case TYPE_REC:
        listener.notifyState(textRec+strTags[2]);
        break;
      case TYPE_BLINK:
        listener.notifyState("录入时间太短暂");
        break;
      default:
        listener.notifyState("语音录入已取消");
        break;
    }
  }

  /**
   * 停止录入
   * */
  private void showRecordCancel(int taskNow, final int taskLast){
    notifyText(taskNow);
    invalidate();
    postDelayed(new Runnable() {
      @Override public void run() {
        notifyText(taskLast);
        invalidate();
      }
    }, 2000);
  }
  /**
   * 当前标签
   * */
  private int curTextCount =0;
  /**
   * 标签集
   * */
  private String[] strTags;
  /**
   * 联动监听
   * */
  private LinkageListener listener;

  public void setLinkageListener(LinkageListener listener){
    this.listener =listener;
  }
  public interface LinkageListener{
    void notifyState(String text);
  }
}
