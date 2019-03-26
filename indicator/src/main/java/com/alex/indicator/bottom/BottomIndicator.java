package com.alex.indicator.bottom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.alex.indicator.R;

/**
 * @author: duxingyu
 * @e-mail: duxy@13322.com
 * @time: 2018/7/28 18:51
 * @desc:
 * @version: 
 **/

public class BottomIndicator extends LinearLayout implements View.OnClickListener{

  public static final String TAG="TAG_BottomIndicator";
  protected String[] tagNames;
  protected int[] tagBackgrounds;

  private int c_Sel,c_Unsel;
  private View[] tabs;
  private View choosenView;
  public BottomIndicator(Context context) {
    super(context);
    init(context,null);
  }

  public BottomIndicator(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public BottomIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs){
    setOrientation(HORIZONTAL);
    TypedArray array =context.obtainStyledAttributes(attrs, R.styleable.BottomIndicator);

    if(array!=null){
      tagNames =context.getResources().getStringArray(array.getResourceId(R.styleable.BottomIndicator_indic_tag_names,R.array.bottom_indicator));

      tagBackgrounds =new int[tagNames.length];
      int resouceId =array.getResourceId(R.styleable.BottomIndicator_indic_tag_background,R.array.bottom_indicator_bg);
      TypedArray drawables =context.getResources().obtainTypedArray(resouceId);
      for(int i=0;i<drawables.length();i++){
        tagBackgrounds[i] =drawables.getResourceId(i,Color.parseColor("#0bc481"));
      }
      drawables.recycle();
      //tagBackgrounds =context.getResources().getIntArray(array.getResourceId(R.styleable.BottomIndicator_indic_tag_background,R.array.bottom_indicator_bg));
      c_Sel =array.getColor(R.styleable.BottomIndicator_indic_tag_sele_color, Color.BLUE);
      c_Unsel =array.getColor(R.styleable.BottomIndicator_indic_tag_unsel_color, Color.GRAY);
      array.recycle();
    }else{
      tagNames =context.getResources().getStringArray(R.array.bottom_indicator);
      tagBackgrounds =context.getResources().getIntArray(R.array.bottom_indicator_bg);
      c_Sel = Color.BLUE;
      c_Unsel = Color.GRAY;
    }
    if(tagNames.length!=tagBackgrounds.length){
      throw new RuntimeException("参数不对等");
    }
    /*for(int each:tagBackgrounds){
      Log.i(TAG, "init: "+each);
    }*/
    tabs =new View[tagNames.length];
    setUnit();
  }

  public void setSelectedPosition(int pos){
    View view =tabs[pos];
    view.performClick();
  }
  private void setUnit(){
    for(int i=0,j =0;i<tagNames.length;i++,j++){
      View view =getEachUnit(tagNames[i],tagBackgrounds[i]);
      view.setOnClickListener(this);
      view.setLayoutParams(getUnitLayoutParams());
      addView(view);
      tabs[i] =view;
    }
    //Log.i(TAG, "setUnit: "+tabs.length);
  }
  protected LayoutParams getUnitLayoutParams(){
    LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
    params.weight =1;
    return params;
  }
  /**
   * 获取单元
   * */
  protected View getEachUnit(String tags,int bgs){
    RelativeLayout item = (RelativeLayout) inflate(getContext(),R.layout.item_indicator,null);
    ViewHoder holder =new ViewHoder();
    holder.tag =item.findViewById(R.id.im_item_indic_bg);
    holder.name =item.findViewById(R.id.tv_item_indic_name);
    holder.tag.setBackgroundDrawable(getContext().getResources().getDrawable(bgs));
    holder.name.setText(tags);
    holder.name.setTextColor(c_Unsel);
    item.setTag(holder);
    return item;
  }

  @Override public void onClick(View v) {
    setChoosenState(v);
    for(int pos=0;pos<tabs.length;pos++){
      if(v ==tabs[pos]&&listener!=null){
        listener.eventPosIndex(pos);
      }
    }
  }
  //先将先前的view设置为未选中状态，然后再将选中的view设置为选中状态，并且让choosenView指向该view的引用
  public void initChoosenState(int pos){
    if(pos>=tagNames.length){
      throw new RuntimeException("非法的下标");
    }
    setChoosenState(tabs[pos]);
  }
  /**
   * 控制单元状态改变。
   * */
  private void setChoosenState(View choosen){
    if(choosenView!=null){
      changeUnitState(choosenView,false);
    }
    changeUnitState(choosen,true);
    choosenView =choosen;
  }
  /**
   * 改变单元状态
   * @param view 单元控件
   * @param isChoosen 是否是被选中的单元
   * */
  protected boolean changeUnitState(View view,boolean isChoosen){
    ViewHoder hoder = (ViewHoder) view.getTag();
    if(hoder==null){
      return false;
    }
    hoder.tag.setSelected(isChoosen);
    hoder.name.setTextColor(isChoosen?c_Sel:c_Unsel);
    return true;
  }

  public static class ViewHoder{
    public ImageView tag;
    public TextView name;
  }
  private IndicatorEventListener listener;
  public void setChoosenLisenter(IndicatorEventListener lisenter){
    this.listener =lisenter;
  }
}
