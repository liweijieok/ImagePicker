package com.lzy.imagepicker.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import com.isseiaoki.simplecropview.FreeCropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.isseiaoki.simplecropview.util.Logger;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.R;
import com.lzy.imagepicker.bean.ImageItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FreeCropActivity extends ImageBaseActivity implements View.OnClickListener {

    private ArrayList<ImageItem> mImageItems;
    private ImagePicker imagePicker;
    private FreeCropImageView mCropImageView;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private Uri mSourceUri = null;
    private View mLoadingBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_free_crop);

        imagePicker = ImagePicker.getInstance();

        mCropImageView = findViewById(R.id.freeCropImageView);

        findViewById(R.id.btn_back).setOnClickListener(this);
        Button btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setText(getString(R.string.ip_complete));
        btn_ok.setOnClickListener(this);
        mLoadingBox = (View) findViewById(R.id.ip_rl_box);

        mImageItems = imagePicker.getSelectedImages();
        mSourceUri = mImageItems.get(0).uri;
        mCropImageView.setCropMode(imagePicker.mFreeCropMode);
        mCropImageView.load(mSourceUri)
                .initialFrameScale(0.3f)
                .useThumbnail(true)
                .execute(mLoadCallback);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (id == R.id.btn_ok) {
            mLoadingBox.setVisibility(View.VISIBLE);
            mCropImageView.crop(mSourceUri).execute(mCropCallback);
        }
    }

    private final LoadCallback mLoadCallback = new LoadCallback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(Throwable e) {
        }
    };
    private final CropCallback mCropCallback = new CropCallback() {
        @Override
        public void onSuccess(Bitmap cropped) {
            mCropImageView.save(cropped)
                    .compressFormat(mCompressFormat)
                    .execute(createSaveUri(), mSaveCallback);
        }

        @Override
        public void onError(Throwable e) {

            mLoadingBox.setVisibility(View.GONE);
        }
    };
    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override
        public void onSuccess(Uri outputUri) {
            mLoadingBox.setVisibility(View.GONE);
            mImageItems.remove(0);
            ImageItem imageItem = new ImageItem();
            imageItem.uri = outputUri;
            mImageItems.add(imageItem);
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, mImageItems);
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);
            finish();
        }

        @Override
        public void onError(Throwable e) {
            mLoadingBox.setVisibility(View.GONE);
        }
    };

    public Uri createSaveUri() {
        return createNewUri(this, mCompressFormat);
    }

    public static Uri createNewUri(Context context, Bitmap.CompressFormat format) {
        long currentTimeMillis = System.currentTimeMillis();
        Date today = new Date(currentTimeMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String title = dateFormat.format(today);
        String fileName = "scv" + title + "." + getMimeType(format);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType(format));
        long time = currentTimeMillis / 1000;
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Logger.i("SaveUri = " + uri);
        return uri;
    }

    public static String getDirPath() {
        String dirPath = "";
        File imageDir = null;
        File extStorageDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
        if (extStorageDir.canWrite()) {
            imageDir = new File(extStorageDir.getPath() + "/crop_pic");
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.getPath();
            }
        }
        return dirPath;
    }

    public static String getMimeType(Bitmap.CompressFormat format) {
        Logger.i("getMimeType CompressFormat = " + format);
        switch (format) {
            case JPEG:
                return "jpeg";
            case PNG:
                return "png";
        }
        return "png";
    }

}
