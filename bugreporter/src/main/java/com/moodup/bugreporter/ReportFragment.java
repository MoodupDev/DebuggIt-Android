package com.moodup.bugreporter;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import butterknife.ButterKnife;

public class ReportFragment extends DialogFragment implements ViewPager.OnPageChangeListener {

    protected static final String TAG = ReportFragment.class.getSimpleName();
    private static final int DOTS_COUNT = 2;

    private ApiClient apiClient;
    private AudioCaptureHelper audioCaptureHelper;
    private List<ImageView> dots;
    private LoadingDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new CustomDialog(getActivity(), R.style.CustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        apiClient = new ApiClient(
                BugReporter.getInstance().getRepoSlug(),
                BugReporter.getInstance().getAccountName(),
                BugReporter.getInstance().getAccessToken()
        );

        audioCaptureHelper = new AudioCaptureHelper();

        dialog = LoadingDialog.newInstance(getString(R.string.loading_dialog_message_report));
        dots = new ArrayList<>();

        return initViews(inflater, container);
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
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_reporter, container, false);
        initViewPager(view);
        initButtons(view);
        return view;
    }

    private void initButtons(View view) {
        MontserratTextView send = ButterKnife.findById(view, R.id.report_confirm);
        MontserratTextView cancel = ButterKnife.findById(view, R.id.report_cancel);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Report report = BugReporter.getInstance().getReport();

                dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                apiClient.addIssue(
                        report.getTitle(),
                        report.getContent()
                                + getUrlAsStrings(report.getScreensUrls(), false)
                                + getUrlAsStrings(report.getAudioUrls(), true),
                        report.getPriority(),
                        report.getKind(),
                        new ApiClient.HttpHandler() {
                            @Override
                            public void done(HttpResponse data) {
                                dialog.dismiss();
                                if (data.responseCode == HttpsURLConnection.HTTP_OK) {
                                    BugReporter.getInstance().getReport().clear();
                                    ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_SUCCESS).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                } else {
                                    ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_FAILURE).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                }
                            }
                        }
                );
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BugReporter.getInstance().getReport().clear();
                dismiss();
            }
        });
    }

    private void initViewPager(View view) {
        initViewPagerIndicator(view);
        ViewPager viewPager = ButterKnife.findById(view, R.id.report_view_pager);
        viewPager.addOnPageChangeListener(this);
        ReportViewPagerAdapter adapter = new ReportViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void initViewPagerIndicator(View view) {
        ViewGroup dotsContainer = ButterKnife.findById(view, R.id.view_pager_indicators);
        for (int i = 0; i < dotsContainer.getChildCount(); ++i) {
            dots.add((ImageView) dotsContainer.getChildAt(i));
            dots.get(i).setSelected(i == 0);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < DOTS_COUNT; i++) {
            dots.get(i).setSelected(position == i);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private String getUrlAsStrings(List<String> urls, boolean isMediaFile) {
        String urlsString = "";

        if (urls != null) {
            StringBuilder builder = new StringBuilder();
            for (String s : urls) {
                if (!isMediaFile) {
                    builder.append("![Alt text](");
                    builder.append(s);
                    builder.append(")");
                } else {
                    builder.append(s);
                }
            }

            urlsString = builder.toString();
        }

        return urlsString;
    }

    private class ReportViewPagerAdapter extends FragmentPagerAdapter {

        public ReportViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return BugDescriptionFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
