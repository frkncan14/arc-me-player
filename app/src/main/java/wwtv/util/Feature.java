/**
 * @Description: TODO()
 *
 */

package com.mediatek.wwtv.util;

/**
 *
 */
public class Feature {

  private static final int CN_CHECK_NOT_INIT = -1;
  private static final int CN_CHECK_WW = 0;
  private static final int CN_CHECK_CN = 1;

  private static int mCn = CN_CHECK_NOT_INIT;

  public static boolean isAospCnPlatform() {
    if (CN_CHECK_NOT_INIT == mCn) {
        try {
            Class cls = Class.forName("com.mediatek.wwtv.util.Cn");          
            mCn = CN_CHECK_CN;
        } catch (ClassNotFoundException e) {
            mCn = CN_CHECK_WW;
        }
    }
    return (CN_CHECK_CN == mCn) ? true : false;
  }

  public static boolean isSupportCnSamba() {
    if (true == isAospCnPlatform()) {
      return true;
    }
    return false;
  }

} // public class Feature
