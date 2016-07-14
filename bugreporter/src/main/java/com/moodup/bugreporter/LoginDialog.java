package com.moodup.bugreporter;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;

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
        CustomDialog dialog = new CustomDialog(getActivity(), R.style.CustomDialog);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_login, null);
        dialog.setContentView(view);

        initView(view);

        return dialog;
    }

    private void initView(View view) {
        final MontserratEditText email = (MontserratEditText) view.findViewById(R.id.bitbucket_email);
        final MontserratEditText password = (MontserratEditText) view.findViewById(R.id.bitbucket_password);
        MontserratTextView loginButton = (MontserratTextView) view.findViewById(R.id.bitbucket_login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiClient.login(
                        BugReporter.getInstance().getClientId(),
                        BugReporter.getInstance().getClientSecret(),
                        email.getText().toString(),
                        password.getText().toString(),
                        new ApiClient.HttpHandler() {
                            @Override
                            public void done(HttpResponse data) {
                                if(data.isSuccessfull()) {
                                    try {
                                        BugReporter.getInstance().saveTokens(data);
                                        LoginDialog.this.dismiss();
                                    } catch(JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    ConfirmationDialog.newInstance("Wrong email or password").show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                }
                            }
                        }
                );
            }
        });
    }

    //endregion

    //region Events

    //endregion

    //region Methods

    public LoginDialog() {
        // Required empty public constructor
    }

    protected static LoginDialog newInstance(ApiClient apiClient) {
        LoginDialog dialog = new LoginDialog();
        dialog.apiClient = apiClient;
        return dialog;
    }

    //endregion


}
