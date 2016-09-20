package com.mooduplabs.debuggit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;

import javax.net.ssl.HttpsURLConnection;

public class LoginFragment extends DialogFragment {
    //region Consts

    public static final String TAG = LoginFragment.class.getSimpleName();

    //endregion

    //region Fields

    protected ApiClient apiClient;

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

    //region Events

    //endregion

    //region Methods

    public LoginFragment() {
        // Required empty public constructor
    }

    protected static LoginFragment newInstance() {
        return new LoginFragment();
    }

    private void initView(View view) {
        final MontserratEditText email = (MontserratEditText) view.findViewById(R.id.bitbucket_email);
        final MontserratEditText password = (MontserratEditText) view.findViewById(R.id.bitbucket_password);
        MontserratTextView loginButton = (MontserratTextView) view.findViewById(R.id.bitbucket_login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initApiClient();
                final LoadingDialog dialog = LoadingDialog.newInstance(getContext().getString(R.string.br_login_loading_info));
                dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
                apiClient.login(
                        DebuggIt.getInstance().getClientId(),
                        DebuggIt.getInstance().getClientSecret(),
                        email.getText().toString(),
                        password.getText().toString(),
                        new ApiClient.HttpHandler() {
                            @Override
                            public void done(HttpResponse data) {
                                dialog.dismiss();
                                if(data.isSuccessful()) {
                                    try {
                                        DebuggIt.getInstance().saveTokens(data);
                                        ConfirmationDialog.newInstance(getString(R.string.br_login_successful), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                    } catch(JSONException e) {
                                        e.printStackTrace();
                                        ConfirmationDialog.newInstance(getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                    }
                                } else if(data.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                                    ConfirmationDialog.newInstance(Utils.getBitbucketErrorMessage(data, getString(R.string.br_login_error_wrong_credentials)), true)
                                            .show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                } else {
                                    ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                }
                            }
                        }
                );
            }
        });
    }

    private void initApiClient() {
        apiClient = new ApiClient(
                DebuggIt.getInstance().getRepoSlug(),
                DebuggIt.getInstance().getAccountName(),
                ""
        );
    }

    //endregion


}
