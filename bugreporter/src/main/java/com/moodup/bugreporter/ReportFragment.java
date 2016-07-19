package com.moodup.bugreporter;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ReportFragment extends DialogFragment implements ViewPager.OnPageChangeListener {

    protected static final String TAG = ReportFragment.class.getSimpleName();

    private ImageView viewPagerIndicator;
    private LoadingDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new CustomDialog(getActivity(), R.style.CustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialog = LoadingDialog.newInstance(getString(R.string.loading_dialog_message_report));
        setCancelable(false);
        return initViews(inflater, container);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() == null) {
            return;
        }

        getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.confirmation_dialog_width), WindowManager.LayoutParams.WRAP_CONTENT);
    }


    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_reporter, container, false);
        initViewPager(view);
        initButtons(view);
        return view;
    }

    private void initButtons(View view) {
        MontserratTextView send = (MontserratTextView) view.findViewById(R.id.report_confirm);
        MontserratTextView cancel = (MontserratTextView) view.findViewById(R.id.report_cancel);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setApplicationVersion();
                if(BugReporter.getInstance().getReport().getTitle().isEmpty()) {
                    ConfirmationDialog.newInstance(getString(R.string.title_empty), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                } else {
                    dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                    sendIssue();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BugReporter.getInstance().getReport().clear();
                resetReportButtonImage();
                dismiss();
            }
        });
    }

    private void sendIssue() {
        Report report = BugReporter.getInstance().getReport();
        ApiClient apiClient = new ApiClient(
                BugReporter.getInstance().getRepoSlug(),
                BugReporter.getInstance().getAccountName(),
                BugReporter.getInstance().getAccessToken()
        );
        apiClient.addIssue(
                report.getTitle(),
                report.getContent()
                        + getUrlAsStrings(report.getScreensUrls(), false)
                        + getUrlAsStrings(report.getAudioUrls(), true)
                        + Utils.getDeviceInfo(getActivity()),
                report.getPriority(),
                report.getKind(),
                new ApiClient.HttpHandler() {
                    @Override
                    public void done(HttpResponse data) {
                        dialog.dismiss();
                        if(data.responseCode == HttpsURLConnection.HTTP_OK) {
                            BugReporter.getInstance().getReport().clear();
                            resetReportButtonImage();
                            ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_SUCCESS).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                        } else if(data.responseCode == HttpsURLConnection.HTTP_FORBIDDEN) {
                            ConfirmationDialog.newInstance(data.message, true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                        } else {
                            ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_FAILURE).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                        }
                    }
                }
        );
    }

    private void setApplicationVersion() {
        Activity activity = BugReporter.getInstance().getActivity();
        PackageManager manager = activity.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
            BugReporter.getInstance().getReport().setApplicationVersion(String.format("%s (version code: %d)", info.versionName, info.versionCode));
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initViewPager(View view) {
        viewPagerIndicator = (ImageView) view.findViewById(R.id.view_pager_indicator);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.report_view_pager);
        viewPager.addOnPageChangeListener(this);
        ReportViewPagerAdapter adapter = new ReportViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void resetReportButtonImage() {
        View rootView = getActivity().findViewById(android.R.id.content);
        ImageView reportButton = (ImageView) rootView.findViewById(R.id.report_button);
        reportButton.setImageDrawable(getResources().getDrawable(R.drawable.logo_bug_small));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        viewPagerIndicator.setRotation(position == 0 ? 0 : 180);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private String getUrlAsStrings(List<String> urls, boolean isMediaFile) {
        StringBuilder builder = new StringBuilder();
        if(urls != null) {
            for(String s : urls) {
                if(!isMediaFile) {
                    builder.append("![Alt text](")
                            .append(s)
                            .append(")")
                            .append('\n');
                } else {
                    builder.append(s)
                            .append('\n');
                }
            }
        }

        return builder.toString();
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
