package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

public class LoginFragment extends DialogFragment {
    //region Consts

    public static final String TAG = LoginFragment.class.getSimpleName();

    //endregion

    //region Fields

    private LoadingDialog loadingDialog;
    private MontserratEditText email;
    private MontserratEditText password;
    private JsonResponseCallback bitbucketLoginResponseCallback = new JsonResponseCallback() {
        @Override
        public void onSuccess(JSONObject response) {
            loadingDialog.dismiss();
            handleLoginResponse(response);
        }

        @Override
        public void onFailure(int responseCode, String errorMessage) {
            if(responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
                ConfirmationDialog.newInstance(Utils.getBitbucketErrorMessage(errorMessage, getString(R.string.br_login_error_wrong_credentials)), true)
                        .show(getChildFragmentManager(), ConfirmationDialog.TAG);
            } else {
                ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
            }
        }

        @Override
        public void onException(Exception ex) {
            ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        }
    };

    private StringResponseCallback jiraLoginResponseCallback = new StringResponseCallback() {

        @Override
        public void onSuccess(String response) {
            loadingDialog.dismiss();
            DebuggIt.getInstance().getJiraConfig().setUsername(email.getText().toString());
            DebuggIt.getInstance().getJiraConfig().setPassword(password.getText().toString());
            ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        }

        @Override
        public void onFailure(int responseCode, String errorMessage) {
            loadingDialog.dismiss();
            if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error_wrong_credentials), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
            } else {
                ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
            }
        }

        @Override
        public void onException(Exception ex) {
            loadingDialog.dismiss();
            ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        }
    };

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

    public LoginFragment() {
        // Required empty public constructor
    }

    protected static LoginFragment newInstance() {
        return new LoginFragment();
    }

    private void initView(View view) {
        email = (MontserratEditText) view.findViewById(R.id.bitbucket_email);
        password = (MontserratEditText) view.findViewById(R.id.bitbucket_password);
        MontserratTextView loginButton = (MontserratTextView) view.findViewById(R.id.bitbucket_login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = LoadingDialog.newInstance(getContext().getString(R.string.br_login_loading_info));
                loadingDialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                switch(DebuggIt.getInstance().getConfigType()) {
                    case BITBUCKET:
                        BitBucketApiClient apiClient = new BitBucketApiClient(DebuggIt.getInstance().getBitBucketConfig());
                        apiClient.login(
                                DebuggIt.getInstance().getBitBucketConfig().getClientId(),
                                DebuggIt.getInstance().getBitBucketConfig().getClientSecret(),
                                email.getText().toString(),
                                password.getText().toString(),
                                bitbucketLoginResponseCallback
                        );
                        break;
                    case JIRA:
                        JiraApiClient jiraApiClient = new JiraApiClient(DebuggIt.getInstance().getJiraConfig());
                        jiraApiClient.login(email.getText().toString(), password.getText().toString(), jiraLoginResponseCallback);
                        break;
                    case GITHUB:
                        break;
                }
            }
        });
    }

    private void handleLoginResponse(JSONObject response) {
        try {
            DebuggIt.getInstance().saveTokens(response);
            ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        } catch(JSONException e) {
            ConfirmationDialog.newInstance(getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
        }
    }

    //endregion


}
