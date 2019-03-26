package com.alex.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author: duxingyu
 * @e-mail: duxy@13322.com
 * @time: 2018/8/14 11:48
 * @desc:
 * @version:
 **/

public class DecodeBitmapUtil {

  /**
   * 解析图片到内存中
   */
  public static Bitmap decodeBitmapFromResources(Resources resources, int resId, int reqWidth,
      int reqHeight) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(resources, resId, options);
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(resources, resId, options);
  }

  private static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
    int height = options.outHeight;
    int width = options.outWidth;
    int inSampleSize = 1;
    if (width > maxWidth || height > maxHeight) {
      if (width > height) {
        inSampleSize = Math.round((float) height / (float) maxHeight);
      } else {
        inSampleSize = Math.round((float) width / (float) maxWidth);
      }
      float totalPixels = (float) (width * height);
      for (float maxTotalPixels = (float) (maxWidth * maxHeight * 2);
          totalPixels / (float) (inSampleSize * inSampleSize) > maxTotalPixels; ++inSampleSize) {
      }
    }
    return inSampleSize;
  }
}
