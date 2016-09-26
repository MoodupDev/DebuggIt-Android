package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.net.HttpURLConnection;

public class ReportFragment extends DialogFragment implements ViewPager.OnPageChangeListener {

    protected static final String TAG = ReportFragment.class.getSimpleName();
    protected static final int MAX_RETRIES_COUNT = 3;

    private ImageView viewPagerIndicator;
    private LoadingDialog dialog;
    private int retriesCount = 0;
    private StringResponseCallback sendingIssueCallback;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new CustomDialog(getActivity(), R.style.BrCustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialog = LoadingDialog.newInstance(getString(R.string.br_loading_dialog_message_report));
        setCancelable(false);
        return initViews(inflater, container);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() == null) {
            return;
        }

        getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.br_confirmation_dialog_width), WindowManager.LayoutParams.WRAP_CONTENT);
    }


    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_br_reporter, container, false);
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
                if(DebuggIt.getInstance().getReport().getTitle().isEmpty()) {
                    ConfirmationDialog.newInstance(getString(R.string.br_title_empty), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                } else {
                    dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                    sendIssue();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebuggIt.getInstance().getReport().clear();
                resetReportButtonImage();
                ApiClient.postEvent(getContext(), ApiClient.EventType.REPORT_CANCELED);
                dismiss();
            }
        });
    }

    private void sendIssue() {
        final Report report = DebuggIt.getInstance().getReport();
        sendingIssueCallback = new StringResponseCallback() {
            @Override
            public void onSuccess(String response) {
                dialog.dismiss();
                postEventsAfterSendingReport(report);
                report.clear();
                resetReportButtonImage();
                retriesCount = 0;
                ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_SUCCESS).show(getChildFragmentManager(), ConfirmationDialog.TAG);
            }

            @Override
            public void onFailure(int responseCode, String errorMessage) {
                if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if(retriesCount < MAX_RETRIES_COUNT) {
                        retriesCount++;
                        sendIssue();
                    } else {
                        dialog.dismiss();
                        retriesCount = 0;
                        showReloginMessage();
                    }
                } else {
                    dialog.dismiss();
                    ConfirmationDialog.newInstance(Utils.getBitbucketErrorMessage(errorMessage, getString(R.string.br_confirmation_failure)), true)
                            .show(getChildFragmentManager(), ConfirmationDialog.TAG);
                }
            }

            @Override
            public void onException(Exception ex) {
                dialog.dismiss();
                ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_FAILURE)
                        .show(getChildFragmentManager(), ConfirmationDialog.TAG);
            }
        };
        switch(DebuggIt.getInstance().getConfigType()) {
            case BITBUCKET:
                sendIssueToBitbucket(report);
                break;
            case JIRA:
                JiraApiClient apiClient = new JiraApiClient(DebuggIt.getInstance().getJiraConfig());
                // TODO: 26.09.2016 implement
                break;
            case GITHUB:
                break;
        }
    }

    private void sendIssueToBitbucket(final Report report) {
        BitBucketApiClient apiClient = new BitBucketApiClient(DebuggIt.getInstance().getBitBucketConfig());
        apiClient.addIssue(
                report.getTitle(),
                report.getContent()
                        + Utils.getUrlAsStrings(report.getScreensUrls(), false)
                        + Utils.getUrlAsStrings(report.getAudioUrls(), true)
                        + Utils.getDeviceInfoString(getActivity()),
                report.getPriority(),
                report.getKind(),
                sendingIssueCallback
        );
    }

    private void postEventsAfterSendingReport(Report report) {
        if(!report.getActualBehaviour().isEmpty()) {
            ApiClient.postEvent(getContext(), ApiClient.EventType.ACTUAL_BEHAVIOUR_FILLED);
        }
        if(!report.getStepsToReproduce().isEmpty()) {
            ApiClient.postEvent(getContext(), ApiClient.EventType.STEPS_TO_REPRODUCE_FILLED);
        }
        if(!report.getExpectedBehaviour().isEmpty()) {
            ApiClient.postEvent(getContext(), ApiClient.EventType.EXPECTED_BEHAVIOUR_FILLED);
        }
        ApiClient.postEvent(getContext(), ApiClient.EventType.SCREENSHOT_AMOUNT, report.getScreensUrls().size());
        ApiClient.postEvent(getContext(), ApiClient.EventType.AUDIO_AMOUNT, report.getAudioUrls().size());
        ApiClient.postEvent(getContext(), ApiClient.EventType.REPORT_SENT);
    }

    private void showReloginMessage() {
        ConfirmationDialog.newInstance(getString(R.string.br_error_access_token_expired), true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.putString(getContext(), DebuggIt.ACCESS_TOKEN, "");
                DebuggIt.getInstance().authenticate(false);
            }
        }).show(getChildFragmentManager(), ConfirmationDialog.TAG);
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
        reportButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.logo_bug_small, null));
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
