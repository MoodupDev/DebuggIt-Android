package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.net.HttpURLConnection;

public class ReportFragment extends DialogFragment implements ViewPager.OnPageChangeListener {
    protected static final String TAG = ReportFragment.class.getSimpleName();
    protected static final int MAX_RETRIES_COUNT = 3;

    private MontserratTextView send;
    private MontserratTextView cancel;

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
        if (getDialog() == null || getDialog().getWindow() == null || getContext() == null) {
            return;
        }

        if (Utils.isOrientationLandscape(getContext())) {
            getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.br_report_layout_width_landscape), WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            getDialog().getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.br_report_layout_width), WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_br_reporter, container, false);
        initViewPager(view);
        initButtons(view);
        return view;
    }

    private void initButtons(View view) {
        send = view.findViewById(R.id.report_confirm);
        cancel = view.findViewById(R.id.report_cancel);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send.setEnabled(false);
                String issueTitle = DebuggIt.getInstance().getReport().getTitle();

                if (issueTitle.isEmpty()) {
                    ConfirmationDialog dialog = ConfirmationDialog.newInstance(getString(R.string.br_title_empty), true);
                    dialog.setOnOkClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            send.setEnabled(true);
                        }
                    });

                    dialog.show(getChildFragmentManager(), ConfirmationDialog.TAG);

                } else if (issueTitle.length() > 255) {
                    ConfirmationDialog dialog = ConfirmationDialog.newInstance(getString(R.string.br_title_too_long, issueTitle.length()), true);
                    dialog.setOnOkClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            send.setEnabled(true);
                        }
                    });

                    dialog.show(getChildFragmentManager(), ConfirmationDialog.TAG);
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
        final Report report = DebuggIt.getInstance().getReport();
        DebuggIt.getInstance().getApiService().addIssue(
                report.getTitle(),
                report.getContent(getActivity()),
                Utils.convertPriorityName(report.getPriority()),
                report.getKind(),
                new StringResponseCallback() {
                    @Override
                    public void onSuccess(String response) {
                        dialog.dismiss();
                        report.clear();
                        resetReportButtonImage();
                        retriesCount = 0;
                        ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_SUCCESS).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                    }

                    @Override
                    public void onFailure(int responseCode, String errorMessage) {
                        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            if (retriesCount < MAX_RETRIES_COUNT) {
                                retriesCount++;
                                sendIssue();
                            } else {
                                dialog.dismiss();
                                retriesCount = 0;
                                showReloginMessage();
                                send.setEnabled(true);
                            }
                        } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                            dialog.dismiss();

                            ConfirmationDialog dialog = ConfirmationDialog.newInstance(getString(R.string.br_error_repo_access_forbidden), true);

                            dialog.setOnOkClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    send.setEnabled(true);
                                }
                            });

                            dialog.show(getChildFragmentManager(), ConfirmationDialog.TAG);
                        } else {
                            dialog.dismiss();

                            ConfirmationDialog dialog = ConfirmationDialog.newInstance(Utils.getBitbucketErrorMessage(errorMessage, getString(R.string.br_confirmation_failure)), true);

                            dialog.setOnOkClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    send.setEnabled(true);
                                }
                            });

                            dialog.show(getChildFragmentManager(), ConfirmationDialog.TAG);
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        dialog.dismiss();

                        ConfirmationDialog dialog = ConfirmationDialog.newInstance(ConfirmationDialog.TYPE_FAILURE);

                        dialog.setOnOkClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                send.setEnabled(true);
                            }
                        });

                        dialog.show(getChildFragmentManager(), ConfirmationDialog.TAG);
                    }
                });
    }

    private void showReloginMessage() {
        ConfirmationDialog.newInstance(getString(R.string.br_error_access_token_expired), true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearToken();
                DebuggIt.getInstance().authenticate(false);
            }
        }).show(getChildFragmentManager(), ConfirmationDialog.TAG);
    }

    private void clearToken() {
        switch (DebuggIt.getInstance().getConfigType()) {
            case BITBUCKET:
                Utils.putString(getContext(), Constants.BitBucket.ACCESS_TOKEN, "");
                break;
            case JIRA:
                Utils.putString(getContext(), Constants.Jira.EMAIL, "");
                Utils.putString(getContext(), Constants.Jira.PASSWORD, "");
                break;
            case GITHUB:
                Utils.putString(getContext(), Constants.GitHub.GITHUB_ACCESS_TOKEN, "");
                break;
        }
    }

    private void initViewPager(View view) {
        viewPagerIndicator = view.findViewById(R.id.view_pager_indicator);
        ViewPager viewPager = view.findViewById(R.id.report_view_pager);
        viewPager.addOnPageChangeListener(this);
        ReportViewPagerAdapter adapter = new ReportViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void resetReportButtonImage() {
        View rootView = getActivity().findViewById(android.R.id.content);
        ImageView reportButton = rootView.findViewById(R.id.report_button);
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
