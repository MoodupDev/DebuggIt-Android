package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DrawFragment extends DialogFragment {
    public static final String SCREENSHOT = "screenshot";
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
    private JSONObject savedResponse;

    private Bitmap screenshot;

    private boolean uploadCancelled;
    private boolean uploadedImagePending;
    private boolean savedInstanceStateDone;

    protected static DrawFragment newInstance(Bitmap screenshot) {
        DrawFragment fragment = new DrawFragment();
        fragment.screenshot = screenshot;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_br_draw, null);
        dialog.setContentView(rootView);
        if (savedInstanceState != null) {
            screenshot = savedInstanceState.getParcelable(SCREENSHOT);
        }
        initViews(rootView);

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        savedInstanceStateDone = false;

        if (getDialog() == null) {
            return;
        }

        if (Utils.isOrientationLandscape(getContext())) {
            getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.br_confirmation_dialog_width_landscape), WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.br_confirmation_dialog_width), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        if (uploadedImagePending) {
            onImageUploaded(savedResponse);
        }
    }

    @Override
    public void onDestroyView() {
        Utils.lockScreenRotation(getActivity(), DebuggIt.getInstance().getActivityOrientation());
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SCREENSHOT, screenshot);
        super.onSaveInstanceState(outState);
        savedInstanceStateDone = true;
    }

    private void initViews(View view) {
        surfaceRoot = view.findViewById(R.id.surface_root);
        screenSurface = view.findViewById(R.id.image_surface);
        drawingSurface = view.findViewById(R.id.draw_surface);
        cancel = view.findViewById(R.id.draw_cancel);
        confirm = view.findViewById(R.id.draw_confirm);
        rubber = view.findViewById(R.id.draw_rubber);
        freeDraw = view.findViewById(R.id.draw_free);
        rectanglesDraw = view.findViewById(R.id.draw_rectangles);
        dialog = LoadingDialog.newInstance(getString(R.string.br_loading_dialog_message_screenshot), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadCancelled = true;
            }
        });

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
        ImageView reportButton = rootView.findViewById(R.id.report_button);
        if (reportButton != null) {
            reportButton.setVisibility(View.INVISIBLE);
        }
        screenSurface.setImageBitmap(screenshot);
        if (reportButton != null) {
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
        if (isFreeDrawClicked) {
            rectanglesDraw.setSelected(false);
        } else {
            freeDraw.setSelected(false);
        }
        v.setSelected(true);
        drawingSurface.setType(isFreeDrawClicked ? PaintableImageView.TYPE_FREE_DRAW : PaintableImageView.TYPE_RECTANGLE_DRAW);
    }

    private void onImageUploaded(JSONObject response) {
        dialog.dismiss();

        try {
            String url = response.getString("url");
            DebuggIt.getInstance().getReport().getScreens().add(new ScreenModel(Utils.getActiveFragmentName(getActivity()), url));
            new ReportFragment().show(getActivity().getSupportFragmentManager(), ReportFragment.TAG);
            savedResponse = null;
            uploadedImagePending = false;
            dismiss();
        } catch (JSONException e) {
            // ignored
        }
    }

    private void uploadScreenshotAndGetUrl() {
        ScreenshotUtils.trimBitmap(getActivity(), Utils.getBitmapFromView(surfaceRoot), new ScreenshotUtils.ScreenshotListener() {
            @Override
            public void onScreenshotReady(Bitmap bitmap) {
                if (!uploadCancelled) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

                    byte[] bitmapData = bos.toByteArray();

                    if (AWSClient.isAWSClientConfigured()) {
                        InputStream imageInputStream = new ByteArrayInputStream(bitmapData);

                        AWSClient.uploadImage(
                                imageInputStream,
                                onUploadImageResult
                        );
                    } else {
                        ApiClient.uploadImage(
                                Base64.encodeToString(bitmapData, Base64.NO_WRAP),
                                onUploadImageResult
                        );
                    }
                }

                uploadCancelled = false;
            }
        });
    }

    private JsonResponseCallback onUploadImageResult = new JsonResponseCallback() {
        @Override
        public void onSuccess(JSONObject response) {
            if (!uploadCancelled) {
                if (!savedInstanceStateDone) {
                    onImageUploaded(response);
                } else {
                    savedResponse = response;
                    uploadedImagePending = true;
                }
            }

            uploadCancelled = false;
        }

        @Override
        public void onFailure(int responseCode, String errorMessage) {
            if (!uploadCancelled) {
                dialog.dismiss();
                ConfirmationDialog.newInstance(getString(R.string.br_screenshot_upload_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
            }

            uploadCancelled = false;
        }

        @Override
        public void onException(Exception ex) {
            if (!uploadCancelled) {
                dialog.dismiss();
                ConfirmationDialog.newInstance(getString(R.string.br_screenshot_upload_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
            }

            uploadCancelled = false;
        }
    };
}
