package com.alex.grade.star;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.alex.grade.R;
import com.alex.utils.DecodeBitmapUtil;

/**
 * @author: duxingyu
 * @e-mail: duxy@13322.com
 * @time: 2019/3/26 17:50
 * @desc: 评分控件
 * @version:
 **/

public class GradeStarView extends View {

  public static final String TAG="TAG_GradeStarView";
  /**
   * 画笔
   * */
  private Paint paint;
  /**
   * 星星总数
   * */
  private int starCount;
  /**
   * 每颗星星之间的水平间距
   * */
  private float starSpaceInterval;
  /**
   * 差评的图片，好评的图片
   * */
  private Bitmap def,select;
  /**
   * 评分
   * */
  private float curGrade;
  /**
   * 宽、高
   * */
  private float widht,height,eachWidth,eachHeight,startX,startY;
  public GradeStarView(Context context) {
    super(context);
    init(context,null);
  }

  public GradeStarView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public GradeStarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs){
    TypedArray array =context.obtainStyledAttributes(attrs, R.styleable.GradeStarView);
    try{
      starCount =array.getInt(R.styleable.GradeStarView_star_count,5);
      starSpaceInterval =array.getDimensionPixelSize(R.styleable.GradeStarView_star_space,10);
      int resDef =array.getResourceId(R.styleable.GradeStarView_star_def,R.mipmap.icon_star_stroke);
      int resSel =array.getResourceId(R.styleable.GradeStarView_star_select,R.mipmap.icon_star_full);
      Drawable drawable =context.getResources().getDrawable(resDef);
      eachWidth = drawable.getMinimumWidth();
      eachHeight =drawable.getMinimumHeight();

      def = DecodeBitmapUtil.decodeBitmapFromResources(context.getResources(),resDef,
          (int) eachWidth, (int) eachHeight);
      select =DecodeBitmapUtil.decodeBitmapFromResources(context.getResources(),resSel,
          (int) eachWidth, (int) eachHeight);

      widht =eachWidth*starCount+starSpaceInterval*(starCount-1)+getPaddingLeft()+getPaddingRight();
      height =eachHeight+getPaddingTop()+getPaddingBottom();

      curGrade =array.getFloat(R.styleable.GradeStarView_star_grade,5f);
    }finally {
      array.recycle();
    }
    startX =getPaddingLeft();
    startY =getPaddingTop();

    paint =new Paint();
    paint.setAntiAlias(true);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension((int)(widht+1),(int)(height+1));
  }

  @Override protected void onDraw(Canvas canvas) {
    int integerPart = (int) curGrade;//整数部分
    float decimalPart =curGrade -integerPart;//小数部分
    startX =getPaddingLeft();
    for(int i=0;i<starCount;i++){
      RectF rect =new RectF(startX,startY,startX+eachWidth,startY+eachHeight);
      if((i+1)<=integerPart){
        canvas.drawBitmap(select,null,rect,paint);
      }else{
        float temp =(i+1)-curGrade;
        if(temp<1){
          canvas.save();
          canvas.drawBitmap(select,null,rect,paint);
          canvas.clipRect(startX+eachWidth*decimalPart,startY,startX+eachWidth,startY+eachHeight);
          canvas.drawBitmap(def,null,rect,paint);
          canvas.restore();
        }else{
          canvas.drawBitmap(def,null,rect,paint);
        }
      }
      startX =startX+eachWidth+starSpaceInterval;
    }
  }

  public void setCurGrade(float curGrade){
    if(curGrade<0||curGrade>starCount){
      throw new RuntimeException("ilegal parametor!!!");
    }
    this.curGrade =curGrade;
    invalidate();
  }

  public float getCurGrade(){
    return curGrade;
  }
}
