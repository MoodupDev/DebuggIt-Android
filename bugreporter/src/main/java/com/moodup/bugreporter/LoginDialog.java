package com.moodup.bugreporter;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;

import javax.net.ssl.HttpsURLConnection;

public class LoginDialog extends DialogFragment {
    //region Consts

    public static final String TAG = LoginDialog.class.getSimpleName();

    //endregion

    //region Fields

    protected ApiClient apiClient;

    //endregion

    //region Override Methods

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.BrCustomDialog);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_login, null);
        dialog.setContentView(view);

        initView(view);

        return dialog;
    }

    //endregion

    //region Events

    //endregion

    //region Methods

    public LoginDialog() {
        // Required empty public constructor
    }

    protected static LoginDialog newInstance() {
        return new LoginDialog();
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
                        BugReporter.getInstance().getClientId(),
                        BugReporter.getInstance().getClientSecret(),
                        email.getText().toString(),
                        password.getText().toString(),
                        new ApiClient.HttpHandler() {
                            @Override
                            public void done(HttpResponse data) {
                                dialog.dismiss();
                                if(data.isSuccessful()) {
                                    try {
                                        BugReporter.getInstance().saveTokens(data);
                                        ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_successfull), false).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                    } catch(JSONException e) {
                                        e.printStackTrace();
                                        ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                    }
                                } else if(data.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                                    ConfirmationDialog.newInstance(getContext().getString(R.string.br_login_error_wrong_credentials), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
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
                BugReporter.getInstance().getRepoSlug(),
                BugReporter.getInstance().getAccountName(),
                ""
        );
    }

    //endregion


}
