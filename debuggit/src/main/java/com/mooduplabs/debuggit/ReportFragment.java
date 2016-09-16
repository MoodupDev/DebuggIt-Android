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

public class ReportFragment extends DialogFragment implements ViewPager.OnPageChangeListener {

    protected static final String TAG = ReportFragment.class.getSimpleName();

    private ImageView viewPagerIndicator;
    private LoadingDialog dialog;
    private int retriesCount = 0;

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
                dismiss();
            }
        });
    }

    private void sendIssue() {
        Report report = DebuggIt.getInstance().getReport();
        ApiClient apiClient = new ApiClient(
                DebuggIt.getInstance().getRepoSlug(),
                DebuggIt.getInstance().getAccountName(),
                DebuggIt.getInstance().getAccessToken()
        );
        apiClient.addIssue(
                report.getTitle(),
                report.getContent()
                        + Utils.getUrlAsStrings(report.getScreensUrls(), false)
                        + Utils.getUrlAsStrings(report.getAudioUrls(), true)
                        + Utils.getDeviceInfoString(getActivity()),
                report.getPriority(),
                report.getKind(),
                new ApiClient.HttpHandler() {
                    @Override
                    public void done(HttpResponse data) {
                        if(!data.isUnauthorized()) {
                            dialog.dismiss();
                        }
                        if(data.isSuccessful()) {
                            DebuggIt.getInstance().getReport().clear();
                            resetReportButtonImage();
                            retriesCount = 0;
                            ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_SUCCESS).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                        } else if(data.isUnauthorized() && retriesCount < 3) {
                                retriesCount++;
                                sendIssue();
                        } else {
                            retriesCount = 0;
                            ConfirmationDialog.newInstance(Utils.getBitbucketErrorMessage(data, getString(R.string.br_confirmation_failure)), true)
                                    .show(getChildFragmentManager(), ConfirmationDialog.TAG);
                        }
                    }
                }
        );
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
