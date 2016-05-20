package com.moodup.bugreporter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
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
    public static final String DRAW_FRAGMENT_BUTTONS = "draw_fragment_buttons";
    public static final String FREE_DRAW_ACTIVE = "free_draw_active";

    private View surfaceRoot;
    private ImageView screenSurface;
    private PaintableImageView drawingSurface;
    private MontserratTextView cancel;
    private MontserratTextView confirm;
    private ImageView rubber;
    private ImageView freeDraw;
    private ImageView rectanglesDraw;
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
        rectanglesDraw = ButterKnife.findById(view, R.id.draw_rectangles);
        dialog = LoadingDialog.newInstance(getString(R.string.loading_dialog_message_screenshot));

        initDrawingSurface();
        initButtons();
        initButtonsState();

        setCancelable(false);
    }

    private void initButtonsState() {
        boolean isFreeDrawActive = getActivity().getSharedPreferences(DRAW_FRAGMENT_BUTTONS, Context.MODE_PRIVATE).getBoolean(FREE_DRAW_ACTIVE, false);
        freeDraw.setSelected(isFreeDrawActive);
        rectanglesDraw.setSelected(!isFreeDrawActive);
        drawingSurface.setType(isFreeDrawActive ? PaintableImageView.TYPE_FREE_DRAW : PaintableImageView.TYPE_RECTANGLE_DRAW);
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
                setDrawingSurfaceEnabled(v);
                saveActiveButton(v);
            }
        });

        rectanglesDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDrawingSurfaceEnabled(v);
                saveActiveButton(v);
            }
        });
    }

    private void saveActiveButton(View view) {
        getActivity().getSharedPreferences(DRAW_FRAGMENT_BUTTONS, Context.MODE_PRIVATE)
                .edit().putBoolean(FREE_DRAW_ACTIVE, view.getId() == R.id.draw_free)
                .commit();
    }

    private void setDrawingSurfaceEnabled(View v) {
        boolean isFreeDrawClicked = v.getId() == R.id.draw_free;
        if(isFreeDrawClicked) {
            rectanglesDraw.setSelected(false);
        } else {
            freeDraw.setSelected(false);
        }
        v.setSelected(!v.isSelected());
        drawingSurface.setEnabled(v.isSelected());
        if(v.isSelected()) {
            drawingSurface.setType(isFreeDrawClicked ? PaintableImageView.TYPE_FREE_DRAW : PaintableImageView.TYPE_RECTANGLE_DRAW);
        }
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
