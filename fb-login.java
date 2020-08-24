package com.refknowledgebase.refknowledgebase;

import android.accounts.Account;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.refknowledgebase.refknowledgebase.buffer.mBuffer;
import com.refknowledgebase.refknowledgebase.utils.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

public class Activity_login extends AppCompatActivity implements View.OnClickListener{

    TextView tv_continue_name, tv_login, tv_signup_facebook;
    CircularImageView img_profile;
    RelativeLayout rl_continue_acount;
    ProfilePictureView profilePictureView;
    RelativeLayout rl_google_sign, rl_facebook_sign, rl_without_reg;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 0;
    String personeDisplayName, personEmail;
    Uri personePhoto;
    LoginButton login_button;
    private CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    Fragment fragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        rl_continue_acount = findViewById(R.id.rl_continue_acount);
        rl_continue_acount.setOnClickListener(this);
        tv_continue_name = findViewById(R.id.tv_continue_acount);
        tv_login = findViewById(R.id.tv_login);
        img_profile = findViewById(R.id.img_avater);
        rl_without_reg = findViewById(R.id.rl_without_reg);
        rl_without_reg.setOnClickListener(this);

//Google login
        rl_google_sign = findViewById(R.id.rl_google_sign);
        rl_google_sign.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//        FB login
        rl_facebook_sign = findViewById(R.id.rl_facebook_sign);
        rl_facebook_sign.setOnClickListener(this);
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        login_button = findViewById(R.id.login_button);
        login_button.setReadPermissions("email");

        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mBuffer.Access_Type = "FB";
                insertString(Constant.LOGINTYPE, "FB");
                fragment = new LandingFragment();
                loadFragment(fragment);
                GraphLoginRequest(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), R.string.cancel_login, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), R.string.error_login, Toast.LENGTH_SHORT).show();
            }
        });
        // Detect user is login or not. If logout then clear the TextView and delete all the user info from TextView.
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken accessToken2) {
                if (accessToken2 == null){
                }
            }
        };

        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePic);

        if (getString(Constant.LOGINTYPE).equals("GOOGLE")){
            String google_photo = getString(Constant.LOGIN_PHOTO);
            String google_name = getString(Constant.LOGIN_NAME);
            Picasso.with(Activity_login.this).load(Uri.parse(google_photo)).into(img_profile);
            tv_continue_name.setText("Continue as "+google_name);
        }
        if (getString(Constant.LOGINTYPE).equals("FB")){

            String fb_photo = getString(Constant.LOGIN_FB_PHOTO);
            String fb_name = getString(Constant.LOGIN_FB_NAME);
            profilePictureView.setProfileId(fb_photo);
            tv_continue_name.setText("Continue as "+fb_name);
        }

        if (getString(Constant.LOGINTYPE).equals("WITHOUT")){
            img_profile.setImageDrawable(getDrawable(R.drawable.avatar));
            tv_continue_name.setText("");
        }
//        new DownloadImageTask(img_profile).execute((Runnable) mBuffer.user_imgUrl);
//        Glide.with(img_profile.getContext()).load(mBuffer.user_imgUrl).into(img_profile);
//        new DownloadImageTask(img_profile).execute(mBuffer.user_imgUrl.toString());

//        tv_continue_name.setText("Continue as "+mBuffer.user_name);
    }

    private void GraphLoginRequest(AccessToken currentAccessToken) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                try{
//                    FacebookDataTextView.setText("ID: " + jsonObject.getString("id"));
                    String fb_id = jsonObject.getString("id");
                    String fb_name = jsonObject.getString("name");
                    String fb_email = jsonObject.getString("email");

                    mBuffer.fb_user_name = fb_name;
                    mBuffer.fb_user_id = fb_id;
                    mBuffer.fb_user_email = fb_email;

//                    profilePictureView.setProfileId(fb_id);
                    insertString(Constant.LOGIN_EMAIL, fb_email);
                    insertString(Constant.LOGIN_FB_NAME, fb_name);
                    insertString(Constant.LOGIN_FB_PHOTO, fb_id);

                    insertString(Constant.LOGINTYPE, "FB");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString(
                "fields",
                "id,name,email"
        );
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.tv_signup_resigteration:
//                Intent intent_main_resigteration = new Intent(Activity_login.this, DashboardActivity.class);
//                startActivity(intent_main_resigteration);
//                finish();
//                break;
            case R.id.rl_continue_acount:
//                Intent intent_main_continue = new Intent(Activity_login.this, Landing_three.class);
//                startActivity(intent_main_continue);
//                finish();
                fragment = new LandingFragment();
                loadFragment(fragment);
                break;

            case R.id.rl_google_sign:
                signIn();
                break;
            case R.id.rl_facebook_sign:
                if (AccessToken.getCurrentAccessToken() != null){
                    GraphLoginRequest(AccessToken.getCurrentAccessToken());
//            Toast.makeText(Activity_Sec.this,"Already logged in",Toast.LENGTH_SHORT).show();
                }else {

                    // If not login in then show the Toast.
//            Toast.makeText(Activity_Sec.this,"User not logged in",Toast.LENGTH_SHORT).show();
                }
                login_button.performClick();
                break;
            case R.id.rl_without_reg:
//                Intent intent_without_resigteration = new Intent(Activity_login.this, Landing_three.class);
////                Intent intent_without_resigteration = new Intent(Activity_login.this, Landing_three.class);
//                startActivity(intent_without_resigteration);
//                finish();
                fragment = new LandingFragment();
                loadFragment(fragment);
                mBuffer.Access_Type = "WITHOUT";
                insertString(Constant.LOGINTYPE, "WITHOUT");
                break;

        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            if (acct != null){

                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                personEmail = acct.getEmail();
                String personId = acct.getId();
                personePhoto = acct.getPhotoUrl();
                personeDisplayName = acct.getDisplayName();
                String personeIdToken = acct.getIdToken();
                Account personeAccount = acct.getAccount();
                String personeServerAuthCode = acct.getServerAuthCode();
                Set<Scope> personeRequestedScopes = acct.getRequestedScopes();
                Set<Scope> personeGrantedScopes = acct.getGrantedScopes();

                mBuffer.user_imgUrl = personePhoto;
                mBuffer.user_name = personeDisplayName;

                mBuffer.Access_Type = "GOOGLE";
                insertString(Constant.LOGINTYPE, "GOOGLE");
                insertString(Constant.LOGIN_EMAIL, personEmail);
                insertString(Constant.LOGIN_NAME, personeDisplayName);
                insertString(Constant.LOGIN_PHOTO, String.valueOf(personePhoto));
            }

//            startActivity(new Intent(Activity_login.this, DashboardActivity.class));

            fragment = new LandingFragment();
            loadFragment(fragment);
//            finish();
        }catch (ApiException e){
            Log.e("Google sign in error", "SignIResult: failed code "+ e);
        }
    }
    public String getString(String key) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constant.PREF, MODE_PRIVATE);
        String  selected =  mSharedPreferences.getString(key, "");
        return selected;
    }

    public synchronized void insertString(String key, String value) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constant.PREF, MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(key, value);
        mEditor.apply();
    }

    private boolean loadFragment(Fragment fragment){

        if (fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_login, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void setAppLocal(String localCode){
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(localCode.toLowerCase()));

        res.updateConfiguration(conf, dm);
    }
}
