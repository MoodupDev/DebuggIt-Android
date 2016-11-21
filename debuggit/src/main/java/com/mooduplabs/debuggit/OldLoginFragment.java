package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class OldLoginFragment extends DialogFragment {
    //region Consts

    public static final String TAG = OldLoginFragment.class.getSimpleName();

    //endregion

    //region Fields

    private LoadingDialog loadingDialog;
    private MontserratEditText email;
    private MontserratEditText password;
    private LinearLayout twoFactorAuthCodeLayout;
    private MontserratEditText twoFactorAuthCode;

    //endregion

    //region Override Methods

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_br_login, null);
        dialog.setContentView(view);

        initView(view);

        return dialog;
    }

    //endregion

    //region Methods

    public OldLoginFragment() {
        // Required empty public constructor
    }

    protected static OldLoginFragment newInstance() {
        return new OldLoginFragment();
    }

    private void initView(View view) {
        initLogoSection(view);
        initLoginFields(view);
        MontserratTextView loginButton = (MontserratTextView) view.findViewById(R.id.bitbucket_login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = LoadingDialog.newInstance(getContext().getString(R.string.br_login_loading_info));
                loadingDialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                final String email = OldLoginFragment.this.email.getText().toString();
                final String password = OldLoginFragment.this.password.getText().toString();
                saveTwoFactorCode();
                DebuggIt.getInstance().getApiService().login(
                        email,
                        password,
                        new JsonResponseCallback() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                loadingDialog.dismiss();
                                switch(DebuggIt.getInstance().getConfigType()) {

                                    case BITBUCKET:
                                        handleBitBucketLoginResponse(response);
                                        break;
                                    case JIRA:
                                        handleJiraLoginResponse(email, password);
                                        break;
                                    case GITHUB:
                                        handleGitHubLoginResponse(response);
                                        break;
                                }
                                DebuggIt.getInstance().applySavedTokens();
                            }

                            @Override
                            public void onFailure(int responseCode, String errorMessage) {
                                loadingDialog.dismiss();
                                if(responseCode == HttpsURLConnection.HTTP_BAD_REQUEST || responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                                    if(DebuggIt.getInstance().getConfigType() == DebuggIt.ConfigType.GITHUB && errorMessage.contains("two-factor")) {
                                        ConfirmationDialog.newInstance(getString(R.string.br_login_error_2fa_required), true, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                twoFactorAuthCodeLayout.setVisibility(View.VISIBLE);
                                            }
                                        }).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                    } else {
                                        ConfirmationDialog.newInstance(Utils.getBitbucketErrorMessage(errorMessage, getString(R.string.br_login_error_wrong_credentials)), true)
                                                .show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                    }
                                } else {
                                    ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                }
                            }

                            @Override
                            public void onException(Exception ex) {
                                loadingDialog.dismiss();
                                ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                            }
                        }
                );
            }
        });
    }

    private void saveTwoFactorCode() {
        String code = twoFactorAuthCode.getText().toString();
        if(!code.isEmpty()) {
            Utils.putString(getActivity(), Constants.GitHub.TWO_FACTOR_AUTH_CODE, code);
            DebuggIt.getInstance().applySavedTokens();
        }
    }

    private void initLoginFields(View view) {
        email = (MontserratEditText) view.findViewById(R.id.login_email);
        password = (MontserratEditText) view.findViewById(R.id.login_password);
        twoFactorAuthCodeLayout = (LinearLayout) view.findViewById(R.id.login_2fa_layout);
        twoFactorAuthCode = (MontserratEditText) view.findViewById(R.id.login_2fa_code);
        if(DebuggIt.getInstance().getConfigType() == DebuggIt.ConfigType.GITHUB) {
            email.setHint(R.string.br_login_hint_email_username);
        }
    }

    private void initLogoSection(View view) {
        ImageView serviceLogo = (ImageView) view.findViewById(R.id.service_logo);
        int logoResId = 0;
        String serviceName = "";
        switch(DebuggIt.getInstance().getConfigType()) {
            case BITBUCKET:
                logoResId = R.drawable.debugg_and_bitbucket;
                serviceName = getString(R.string.br_service_bitbucket);
                break;
            case JIRA:
                logoResId = R.drawable.debugg_and_jira;
                serviceName = getString(R.string.br_service_jira);
                break;
            case GITHUB:
                logoResId = R.drawable.debugg_and_github;
                serviceName = getString(R.string.br_service_github);
                break;
        }
        serviceLogo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), logoResId, null));
        MontserratTextView loginInfo = (MontserratTextView) view.findViewById(R.id.login_info);
        loginInfo.setText(getString(R.string.br_login_info, serviceName));
    }

    private void handleGitHubLoginResponse(JSONObject response) {
        try {
            Utils.putString(getActivity(), Constants.GitHub.GITHUB_ACCESS_TOKEN, response.getString(Constants.Keys.TOKEN));
            ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        } catch(JSONException e) {
            ConfirmationDialog.newInstance(getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        }
    }

    private void handleJiraLoginResponse(String email, String password) {
        Utils.putString(getActivity(), Constants.Jira.EMAIL, email);
        Utils.putString(getActivity(), Constants.Jira.PASSWORD, password);
        ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
    }

    private void handleBitBucketLoginResponse(JSONObject response) {
        try {
            DebuggIt.getInstance().saveTokens(response);
            ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        } catch(JSONException e) {
            ConfirmationDialog.newInstance(getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        }
    }

    //endregion


}
