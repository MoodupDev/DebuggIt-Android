package com.moodup.bugreporter;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.cloudinary.utils.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class DrawFragment extends DialogFragment {

    protected static final String TAG = DrawFragment.class.getSimpleName();

    private View surfaceRoot;
    private ImageView screenSurface;
    private PaintableImageView drawingSurface;
    private MontserratTextView cancel;
    private MontserratTextView confirm;
    private ImageView rubber;
    private ImageView freeDraw;
    private LoadingDialog dialog;

    private UploadImageAsyncTask uploadImageAsyncTask;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.CustomDialog);
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_draw, null);
        ButterKnife.bind(this, rootView);
        dialog.setContentView(rootView);
        initViews(rootView);

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() == null) {
            return;
        }

        getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.confirmation_dialog_width), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onDestroyView() {
        if (uploadImageAsyncTask != null) {
            uploadImageAsyncTask.cancel(true);
        }
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private void initViews(View view) {
        surfaceRoot = ButterKnife.findById(view, R.id.surface_root);
        screenSurface = ButterKnife.findById(view, R.id.image_surface);
        drawingSurface = ButterKnife.findById(view, R.id.draw_surface);
        cancel = ButterKnife.findById(view, R.id.draw_cancel);
        confirm = ButterKnife.findById(view, R.id.draw_confirm);
        rubber = ButterKnife.findById(view, R.id.draw_rubber);
        freeDraw = ButterKnife.findById(view, R.id.draw_free);

        dialog = LoadingDialog.newInstance(getString(R.string.loading_dialog_message_screenshot));

        initDrawingSurface();
        initButtons();
    }

    private void initDrawingSurface() {
        View rootView = getActivity().findViewById(android.R.id.content);
        screenSurface.setImageBitmap(Utils.getBitmapFromView(rootView));
    }

    private void initButtons() {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                uploadScreenshotAndGetUrl();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        rubber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingSurface.clear();
            }
        });

        freeDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                drawingSurface.setEnabled(v.isSelected());
            }
        });
    }

    private void uploadScreenshotAndGetUrl() {
        Bitmap bmp = Utils.getBitmapFromView(surfaceRoot);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, bos);

        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        uploadImageAsyncTask = new UploadImageAsyncTask();

        uploadImageAsyncTask.execute(bs);
    }

    private class UploadImageAsyncTask extends AsyncTask<InputStream, Void, List<String>> {
        @Override
        protected List<String> doInBackground(InputStream... params) {
            List<String> urls = new ArrayList<>();

            try {
                for (InputStream is : params) {
                    Map map = BugReporter.getInstance().getCloudinary().uploader().upload(is, ObjectUtils.emptyMap());
                    urls.add((String) map.get("url"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return urls;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            dialog.dismiss();
            BugReporter.getInstance().getReport().getScreensUrls().addAll(s);
            new ReportFragment().show(getActivity().getSupportFragmentManager(), ReportFragment.TAG);
            dismiss();

            super.onPostExecute(s);
        }
    }
}
