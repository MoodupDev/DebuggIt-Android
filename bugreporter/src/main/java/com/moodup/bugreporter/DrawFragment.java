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

public class DrawFragment extends DialogFragment {

    protected static final String TAG = DrawFragment.class.getSimpleName();
    protected static final String FREE_DRAW_ACTIVE = "free_draw_active";

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
        super.onDestroyView();
    }

    private void initViews(View view) {
        surfaceRoot = view.findViewById(R.id.surface_root);
        screenSurface = (ImageView) view.findViewById(R.id.image_surface);
        drawingSurface = (PaintableImageView) view.findViewById(R.id.draw_surface);
        cancel = (MontserratTextView) view.findViewById(R.id.draw_cancel);
        confirm = (MontserratTextView) view.findViewById(R.id.draw_confirm);
        rubber = (ImageView) view.findViewById(R.id.draw_rubber);
        freeDraw = (ImageView) view.findViewById(R.id.draw_free);
        rectanglesDraw = (ImageView) view.findViewById(R.id.draw_rectangles);
        dialog = LoadingDialog.newInstance(getString(R.string.loading_dialog_message_screenshot));

        initDrawingSurface();
        initButtons();
        initButtonsState();

        setCancelable(false);
    }

    private void initButtonsState() {
        boolean isFreeDrawActive = Utils.getBoolean(getContext(), FREE_DRAW_ACTIVE, false);
        freeDraw.setSelected(isFreeDrawActive);
        rectanglesDraw.setSelected(!isFreeDrawActive);
        drawingSurface.setType(isFreeDrawActive ? PaintableImageView.TYPE_FREE_DRAW : PaintableImageView.TYPE_RECTANGLE_DRAW);
    }

    private void initDrawingSurface() {
        View rootView = getActivity().findViewById(android.R.id.content);
        ImageView reportButton = (ImageView) rootView.findViewById(R.id.report_button);
        reportButton.setVisibility(View.INVISIBLE);
        screenSurface.setImageBitmap(Utils.getBitmapFromView(rootView));
        reportButton.setVisibility(View.VISIBLE);
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
        Utils.putBoolean(getContext(), FREE_DRAW_ACTIVE, view.getId() == R.id.draw_free);
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
            if(s.size() != 0) {
                BugReporter.getInstance().getReport().getScreensUrls().addAll(s);
                new ReportFragment().show(getActivity().getSupportFragmentManager(), ReportFragment.TAG);
                dismiss();
            } else {
                ConfirmationDialog.newInstance(getString(R.string.screenshot_upload_error)).show(getChildFragmentManager(), ConfirmationDialog.TAG);
            }

            super.onPostExecute(s);
        }
    }
}
