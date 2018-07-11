package com.hotbitmapgg.bilibili.utils;

import com.hotbitmapgg.bilibili.entity.AppContext;

public class PathUtil {

    ///<summary>获取获取ZZX图片URL地址</summary>
    ///<param name="isCheckAndBackWholeURL">true表示进行URL检查并返回完整的URL,false表示原样返回不做任何修改</param>
    ///<result></result>
    public static String GetZZXImageURL(String path,boolean isCheckAndBackWholeURL)
    {
        if(isCheckAndBackWholeURL && !path.startsWith("http"))
        {
            if(path.indexOf('/')<=0)//这是一个图片文件路径
                return AppContext.ServerBaseURL + "Content/product_images/" + path;

            //这是一个相对路径
            return AppContext.ServerBaseURL + path;
        }
        return path;
    }
}
