package com.moodup.bugreporter;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class DrawFragment extends Fragment {

    protected static final String TAG = DrawFragment.class.getSimpleName();

    private ImageView screenSurface;
    private PaintableImageView drawingSurface;
    private Button cancel;
    private Button confirm;

    private UploadImageAsyncTask uploadImageAsyncTask;
    private ProgressDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initViews(inflater, container);
    }

    @Override
    public void onDestroyView() {
        if (uploadImageAsyncTask != null) {
            uploadImageAsyncTask.cancel(true);
        }
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_draw, container, false);

        screenSurface = ButterKnife.findById(view, R.id.image_surface);
        drawingSurface = ButterKnife.findById(view, R.id.draw_surface);
        cancel = ButterKnife.findById(view, R.id.draw_cancel);
        confirm = ButterKnife.findById(view, R.id.draw_confirm);

        dialog = new ProgressDialog(getActivity());
        dialog.setTitle("Wait!");
        dialog.setMessage("you fool");

        initDrawingSurface();
        initButtons();

        return view;
    }

    private void initDrawingSurface() {
        View rootView = getActivity().findViewById(android.R.id.content);
        screenSurface.setImageBitmap(Utils.getBitmapFromView(rootView));
    }

    private void initButtons() {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                uploadScreenshotAndGetUrl();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
    }

    private void uploadScreenshotAndGetUrl() {
        Bitmap bmp = Utils.getBitmapFromView(getView());

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
            dialog.hide();

            BugReporter.getInstance().getScreensUrls().addAll(s);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.add(android.R.id.content, ReportFragment.newInstance(), ReportFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();

            super.onPostExecute(s);
        }
    }
}
