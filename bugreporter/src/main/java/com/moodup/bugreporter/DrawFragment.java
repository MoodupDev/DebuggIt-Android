package com.moodup.bugreporter;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class DrawFragment extends DialogFragment {

    protected static final String TAG = DrawFragment.class.getSimpleName();
    protected static final String FREE_DRAW_ACTIVE = "free_draw_active";
    public static final String SCREENSHOT = "screenshot";

    private View surfaceRoot;
    private ImageView screenSurface;
    private PaintableImageView drawingSurface;
    private MontserratTextView cancel;
    private MontserratTextView confirm;
    private ImageView rubber;
    private ImageView freeDraw;
    private ImageView rectanglesDraw;
    private LoadingDialog dialog;

    private Bitmap screenshot;

    private UploadImageAsyncTask uploadImageAsyncTask;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.CustomDialog);
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_draw, null);
        dialog.setContentView(rootView);
        if(savedInstanceState != null) {
            screenshot = savedInstanceState.getParcelable(SCREENSHOT);
        }
        initViews(rootView);

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() == null) {
            return;
        }
        if(Utils.isOrientationLandscape(getContext())) {
            getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.confirmation_dialog_width_landscape), WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.confirmation_dialog_width), WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        if (uploadImageAsyncTask != null) {
            uploadImageAsyncTask.cancel(true);
        }
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SCREENSHOT, screenshot);
        super.onSaveInstanceState(outState);
    }

    protected static DrawFragment newInstance(Bitmap screenshot) {
        DrawFragment fragment = new DrawFragment();
        fragment.screenshot = screenshot;
        return fragment;
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
        dialog = LoadingDialog.newInstance(getString(R.string.br_loading_dialog_message_screenshot));

        initDrawingSurface();
        initButtons();
        initButtonsState();

        setCancelable(false);
    }

    private void initButtonsState() {
        boolean isFreeDrawActive = Utils.getBoolean(getContext(), FREE_DRAW_ACTIVE, true);
        freeDraw.setSelected(isFreeDrawActive);
        rectanglesDraw.setSelected(!isFreeDrawActive);
        drawingSurface.setType(isFreeDrawActive ? PaintableImageView.TYPE_FREE_DRAW : PaintableImageView.TYPE_RECTANGLE_DRAW);
    }

    private void initDrawingSurface() {
        View rootView = getActivity().findViewById(android.R.id.content);
        ImageView reportButton = (ImageView) rootView.findViewById(R.id.report_button);
        if(reportButton != null) {
            reportButton.setVisibility(View.INVISIBLE);
        }
        screenSurface.setImageBitmap(screenshot);
        if(reportButton != null) {
            reportButton.setVisibility(View.VISIBLE);
        }
    }

    private void initButtons() {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                drawingSurface.drawActiveRectangle();
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
                drawingSurface.previousDrawing();
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
        v.setSelected(true);
        drawingSurface.setType(isFreeDrawClicked ? PaintableImageView.TYPE_FREE_DRAW : PaintableImageView.TYPE_RECTANGLE_DRAW);
    }

    private void uploadScreenshotAndGetUrl() {
        Bitmap bmp = Utils.getBitmapFromView(surfaceRoot);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, bos);

        byte[] bitmapdata = bos.toByteArray();

        HashMap<String, String> params = new HashMap<>();
        params.put("file", Base64.encodeToString(bitmapdata, Base64.URL_SAFE));
        params.put("mimetype", ApiClient.MIME_TYPE_IMAGE);
        params.put("package", getActivity().getPackageName());

        uploadImageAsyncTask = new UploadImageAsyncTask(params);
        uploadImageAsyncTask.execute();
    }

    private class UploadImageAsyncTask extends AsyncTask<String, Void, String> {

        private HashMap<String, String> postParams;

        public UploadImageAsyncTask(HashMap<String, String> params) {
            this.postParams = params;
        }

        @Override
        protected String doInBackground(String... params) {
            return ApiClient.getUploadedFileUrl(postParams);
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            if(s != null && !s.isEmpty()) {
                BugReporter.getInstance().getReport().getScreensUrls().add(s);
                new ReportFragment().show(getActivity().getSupportFragmentManager(), ReportFragment.TAG);
                dismiss();
            } else {
                ConfirmationDialog.newInstance(getString(R.string.br_screenshot_upload_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
            }
            super.onPostExecute(s);
        }
    }
}
