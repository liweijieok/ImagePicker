package com.lzy.imagepicker;

/**
 * Project Name: ImagePicker
 *
 * @author vj
 * @date : 2020-06-08 18:09
 * email:liweijieok@qq.com
 * desc:
 * lastModify:
 */
public interface ImageTypeFilter {
    /**
     * filter
     *
     * @param mimeType image/png,image/jpeg,image/*,image/jpeg,image/webp,image/gif so...
     * @return true:不过滤，会显示出来，false：过滤，不会加载出来
     */
    boolean filter(String mimeType);
}
