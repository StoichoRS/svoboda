package com.example.svoboda;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText username;
    private EditText password;
    private RelativeLayout loginBtn;
    private TextView loginText;
    private ProgressBar loginSpinner;
    private JSONObject loginCredentials;
    private ContextData contextData;

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        contextData = ContextData.getInstance();
        username = (EditText)findViewById(R.id.usernameInput);
        password = (EditText)findViewById(R.id.passwordInput);
        loginBtn = (RelativeLayout)findViewById(R.id.loginInput);
        loginText = (TextView)findViewById(R.id.loginText);
        loginSpinner = (ProgressBar)findViewById(R.id.loginSpinner);
        loginCredentials = new JSONObject();

        /*
            This makes an attempt to log the user in with the session id if there
            is one in the internal storage
         */
        JSONObject internalData = readInternalDataAsJSON();
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

    /*
        Writes the provided JSONObject to internal storage
     */
    private void writeJSONToInternalData(JSONObject json)
    {
        try
        {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("svoboda.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(json.toString());
            outputStreamWriter.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /*
        Reads the internal storage data and return it as a JSONObject or null if
        an error occurs
     */
    private JSONObject readInternalDataAsJSON()
    {

        JSONObject json = null;

        try
        {
            InputStream inputStream = this.openFileInput("svoboda.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                String ret = stringBuilder.toString();
                json = new JSONObject(ret);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            Log.e("login activity", "File not found: " + e.toString());
        }
        catch (IOException e)
        {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return json;
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

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody req = RequestBody.create(JSON, requestData.toString());
        Request request = new Request.Builder()
                .url(contextData.loginUrl)
                .post(req)
                .build();

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response)
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
                            Toast.makeText(LoginActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        }
                    });
                    username.getText().clear();
                    password.getText().clear();
                    loginCredentials = new JSONObject();
                    setLoginButtonState(true);
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
                            writeJSONToInternalData(jsonData);
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
        });
    }
}
