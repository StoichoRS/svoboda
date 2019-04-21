package com.example.svoboda;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements Callback{
    private static final String TAG = "LoginActivity";
    private EditText username;
    private EditText password;
    private RelativeLayout loginBtn;
    private TextView loginText;
    private ProgressBar loginSpinner;
    private JSONObject loginCredentials;
    private ContextData contextData;
    private IOHandler ioHandler;
    private SvobodaAPIClient svobodaAPIClient;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        contextData = ContextData.getInstance();
        svobodaAPIClient = SvobodaAPIClient.getInstance();
        ioHandler = new IOHandler(this);
        username = findViewById(R.id.usernameInput);
        password = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginInput);
        loginText = findViewById(R.id.loginText);
        loginSpinner = findViewById(R.id.loginSpinner);
        loginCredentials = new JSONObject();

        /*
            This makes an attempt to log the user in with the session id if there
            is one in the internal storage
         */
        JSONObject internalData = ioHandler.readInternalDataAsJSON("svoboda.txt");
        try
        {
            if (internalData != null && internalData.getString("sess_id") != null)
            {
                sendCredentialsToServer(internalData);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // When login button is clicked
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String usr = username.getText().toString();
                String pass = password.getText().toString();
                if (usr.equals(""))
                {
                    Toast.makeText(LoginActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
                }
                else if (pass.equals(""))
                {
                    Toast.makeText(LoginActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    try {
                        loginCredentials
                                .put("username", usr)
                                .put("password", pass);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendCredentialsToServer(loginCredentials);
                }
            }
        });
    }

    private void setLoginButtonState(boolean active)
    {
        if (active)
        {
            loginBtn.setClickable(true);
            loginText.setVisibility(View.VISIBLE);
            loginSpinner.setVisibility(View.GONE);
        }
        else
        {
            loginBtn.setClickable(false);
            loginText.setVisibility(View.GONE);
            loginSpinner.setVisibility(View.VISIBLE);
        }
    }

    public void sendCredentialsToServer(JSONObject requestData)
    {
        /*
            Dont allow user to click login button while validating the credentials
         */
        setLoginButtonState(false);
        svobodaAPIClient.makeRequest(contextData.loginUrl, requestData, this);
    }

    @Override
    public void onFailure(@NonNull  Call call, @NonNull IOException e)
    {
        e.printStackTrace();
    }

    @Override
    public void onResponse(@NonNull Call call,@NonNull Response response)
    {
        if (!response.isSuccessful())
        {
            /*
                Unsuccessful login. We clear the login input fields,
                the loginCredentials object and enable the login button.
             */
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (response.body() != null)
                    {
                        try
                        {
                            Toast.makeText(LoginActivity.this, response.body().string(), Toast.LENGTH_SHORT).show();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    username.getText().clear();
                    password.getText().clear();
                    loginCredentials = new JSONObject();
                    setLoginButtonState(true);
                }
            });
        }
        else
        {
            /*
                Successful login. We save the data to the internal storage
                and to the ContextData object for easy access and open the
                Map for the user.
             */
            JSONObject jsonData = null;
            if (response.body() != null)
            {
                try
                {
                    jsonData = new JSONObject(response.body().string());
                    response.body().close();
                    ioHandler.writeJSONToInternalData(jsonData);
                    contextData.userProfile = jsonData;
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(LoginActivity.this,
                                    "Successful login", Toast.LENGTH_SHORT).show();                                }
                    });
                    Intent openMapIntent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(openMapIntent);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
