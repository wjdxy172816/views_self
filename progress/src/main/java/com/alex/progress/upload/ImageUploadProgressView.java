package com.alex.progress.upload;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import androidx.annotation.Nullable;
import com.alex.progress.R;
import com.alex.utils.DecodeBitmapUtil;

/**
 * @author: duxingyu
 * @e-mail: duxy@13322.com
 * @time: 2018/8/14 11:27
 * @desc: 上传图片的进度控件
 * @version: 
 **/

public class ImageUploadProgressView extends View {

  public static final String TAG="TAG_ImageUploadProgress";
  /**
   * 当前动画回调次数
   * */
  private int count;
  /**
   * 画笔
   * */
  private Paint paint;
  /**
   * 文字颜色
   * */
  private int textColor;
  /**
   * 扫描图片
   * */
  private Bitmap bitmap;
  /**
   * 文字尺寸
   * */
  private float textSize;
  /**
   * 图片动态高度
   * */
  private float hDynamic;
  /**
   * 当前百分比
   * */
  private float curPercent;
  /**
   * 记录着当前小数模式下的进度。
   * */
  private float currentProg;
  /**
   * 控件宽高
   * */
  private float width,height;
  /**
   * 两行文字之间的间隔
   * */
  private float intervalSpace;
  /**
   * 文字的宽
   * */
  private float textWidth,perWidth;
  /**
   * 百分比描述文字
   * */
  private String textPercent="0.00%";
  /**
   * 默认的断层差值
   * */
  private int AVOID_FAULTAGE_DRAW_D_VALUE =5;
  /**
   * 文字的起始x,y坐标，百分比的起始x,y坐标
   * */
  private float x_textStr,y_textStr,x_perStr,y_perStr;
  /**
   * 提示文字
   * */
  private String textStr,textWait="等待上传中",textOver="上传成功!",textLoading="图片上传中",textFailed="上传失败";

  public static final int TYPE_UPLOAD_WAITING =0;//等待上传
  public static final int TYPE_UPLOAD_LOADING =1;//正在上传
  public static final int TYPE_UPLOAD_FINISH =2;//上传结束
  public static final int TYPE_UPLOAD_FAILED =3;//上传失败

  private int curState =TYPE_UPLOAD_WAITING;

  public ImageUploadProgressView(Context context) {
    super(context);
    init(context,null);
  }

  public ImageUploadProgressView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context,attrs);
  }

  public ImageUploadProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context,attrs);
  }

  private void init(Context context, AttributeSet attrs) {

    TypedArray array =context.obtainStyledAttributes(attrs, R.styleable.ImageUploadProgressView);
    if(array!=null){
      int res =array.getResourceId(R.styleable.ImageUploadProgressView_image_progress_imgs,R.mipmap.image_upload_sweeper);
      bitmap = DecodeBitmapUtil.decodeBitmapFromResources(getResources(),res,(int)width,(int) height);

      textLoading =array.getString(R.styleable.ImageUploadProgressView_image_text_loading);
      textWait =array.getString(R.styleable.ImageUploadProgressView_image_text_wait);
      textOver =array.getString(R.styleable.ImageUploadProgressView_image_text_over);

      textColor =array.getColor(R.styleable.ImageUploadProgressView_image_text_color, Color.BLACK);
      textSize =array.getDimension(R.styleable.ImageUploadProgressView_image_text_size,15);
      intervalSpace =array.getFloat(R.styleable.ImageUploadProgressView_image_text_interval,1.2f);
      array.recycle();
    }else{
      bitmap =DecodeBitmapUtil.decodeBitmapFromResources(getResources(),R.mipmap.image_upload_sweeper,(int)width, (int) height);
      textColor = Color.BLACK;
      textSize =15;
      intervalSpace =1.2f;
    }
    paint =new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth(2);
    paint.setAntiAlias(true);
    paint.setTextSize(textSize);
    paint.setColor(textColor);

    textStr =textWait;
    textWidth =paint.measureText(textStr);
    perWidth =paint.measureText("60.0%");

    setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        if(curState==TYPE_UPLOAD_FAILED){
          listener.reupload();
          uploadWaiting();
        }else{
          listener.toDetail();
        }
      }
    });
  }

  private void initTextLoc(){
    x_textStr =(width -textWidth)/2;
    x_perStr =(width -perWidth)/2;
    float textHeight =paint.descent() -paint.ascent();
    y_textStr =height/2+textHeight/2+paint.ascent();
    y_perStr =y_textStr+textHeight*intervalSpace;
  }


  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    width =getMeasuredWidth();
    height =getMeasuredHeight();
    hDynamic =height;
    initTextLoc();
  }


  @Override protected void onDraw(Canvas canvas) {
    RectF rectF =new RectF(0,hDynamic,width,height);
    canvas.drawBitmap(bitmap,null,rectF,paint);
    canvas.drawText(textStr,x_textStr,y_textStr,paint);
    canvas.drawText(textPercent,x_perStr,y_perStr,paint);
  }

  /**
   * 当前上传进度
   * @param currentProg 当前进度的百分比,0-1之间的一个值
   * */
  public void uploading(float currentProg){
    //如果当前有动画正在进行，说明是柔性绘制，那么不需要再进行绘制了，由动画来进行更新。
    if(animator!=null&&animator.isRunning()){
      this.currentProg =currentProg;
      return;
    }
    int dd;
    if(currentProg<=1f){
      dd = (int) (currentProg*100);
    }else{
      dd = (int) currentProg;
    }
    //如果超过了断层差值，就使用动画绘制
    if(dd-curPercent>=AVOID_FAULTAGE_DRAW_D_VALUE){
      start(this.currentProg,currentProg);
    }else{//否则直接更新
      if(currentProg==1.0f){
        uploadingOver();
      }else{
        curPercent =dd;
        hDynamic =height*(1-currentProg);
        textPercent =curPercent+"%";
        invalidate();
      }
    }
    this.currentProg =currentProg;
  }
  public void uploadWaiting(){
    initTags(TYPE_UPLOAD_WAITING);
  }

  public void uploadUploading(){
    initTags(TYPE_UPLOAD_LOADING);
  }

  public void uploadFinished(){
    initTags(TYPE_UPLOAD_FINISH);
  }

  public void uploadFailed(){
    initTags(TYPE_UPLOAD_FAILED);
  }
  private void initTags(int type){
    curState = type;
    switch (curState){
      case TYPE_UPLOAD_WAITING:
        textStr =textWait;
        textPercent ="0.00%";
        paint.setColor(textColor);
        hDynamic =height;
        break;
      case TYPE_UPLOAD_LOADING:
        textStr =textLoading;
        paint.setColor(textColor);
        hDynamic =height;
        break;
      case TYPE_UPLOAD_FINISH:
        textStr =textOver;
        textPercent ="100%";
        paint.setColor(textColor);
        hDynamic =0;
        break;
      case TYPE_UPLOAD_FAILED:
        textStr =textFailed;
        textPercent ="点击从新上传!";
        paint.setColor(Color.parseColor("#fc5f5f"));
        hDynamic =height;
        break;
    }
    perWidth =paint.measureText(textPercent);
    initTextLoc();
    invalidate();
  }
  /**
   * 使用动画更新ui
   * @param percentNow 当前percent
   * @param originPercent 原始的进度
   * @param count 属性动画当前执行的次数。属性动画每秒回调次数大概为60次。
   * */
  private void uploadingInAnim(float percentNow,float originPercent,int count){
    float realPercent;
    if(originPercent<currentProg){
      float d_value =currentProg -originPercent;
      if(count<=0){//因为回调次数会有少许的偏差，可能导致该值为负值。
        count=1;
      }
      realPercent =percentNow+d_value/count;
    }else{
      realPercent =percentNow;
    }
    curPercent =(int)(realPercent*100);
    textPercent =curPercent+"%";
    hDynamic =height*(1-realPercent);
    //2018/8/15 如果在最后一次跟新状态中，百分比已经是1.0f了。那么就直接调用结束状态
    if(percentNow==originPercent&&currentProg==1.0f){
      uploadingOver();
    }else{
      invalidate();
    }
  }

  /**
   * 开始进度
   * */
  public void uploadingStart(){
    if(listener!=null){
      listener.start();
    }
    textStr =textLoading;
    textPercent ="0.00%";
    invalidate();
  }
  /**
   * 处于等待上传的状态
   * */
  public void uploadingWait(){
    textStr =textWait;
    textPercent ="0.00%";
    invalidate();
  }
  
  /**
   * 设置当前的上传进度，用于直接定位到某一进度
   * */
  public void setCurrentUploadPercent(float currentProg){
    int dd;
    if(currentProg<=1f){
      dd = (int) (currentProg*100);
    }else{
      dd = (int) currentProg;
    }
    if(currentProg==1.0f){
      uploadingOver();
    }else{
      curPercent =dd;
      hDynamic =height*(1-currentProg);
      textPercent =curPercent+"%";
      this.currentProg =currentProg;
      invalidate();
    }
  }
  private ValueAnimator selfAnimator;
  /**
   * 循环加载效果
   * @param repeatCount 重复次数
   * @param duration 每次持续时间
   * */
  public void startSelfLoading(int repeatCount,int duration){
    selfAnimator =new ValueAnimator();
    selfAnimator.setFloatValues(0,1.0f);
    selfAnimator.setDuration(duration);
    selfAnimator.setRepeatCount(repeatCount);
    selfAnimator.setInterpolator(new DecelerateInterpolator());
    selfAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        uploading(value);
        if(value==1.0f){
          selfAnimator.cancel();
        }
      }
    });
    selfAnimator.start();
  }

  /**
   * 进度结束
   * */
  public void uploadingOver(){
    hDynamic =0;
    textStr =textOver;
    textPercent =" ";
    invalidate();
    if(listener!=null){
      listener.over();
    }
  }

  ValueAnimator animator;
  /**
   * 当两次上传数据之间返回的进度差太大的时候，开启动画进行柔性绘制
   * @param start 上一次的进度
   * @param end 当前进度
   * */
  private void start(float start, final float end){
    count =0;
    animator =new ValueAnimator();
    animator.setFloatValues(start,end);
    animator.setDuration(500);
    animator.setInterpolator(new LinearInterpolator());
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        count++;
        float temp = (float) animation.getAnimatedValue();
        if(temp==1.0f){
          uploadingOver();
        }else{
          uploadingInAnim(temp,end,30-count);
        }
      }
    });
    animator.start();
  }

  private UploadEventListener listener;
  public void setUploadEventListener(UploadEventListener listener){
    this.listener =listener;
  }
  public interface UploadEventListener{
    void start();
    void over();
    /**
     * 重新上传
     * */
    void reupload();
    void toDetail();
  }
}
