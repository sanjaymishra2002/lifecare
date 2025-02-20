package com.example.lifecare;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    int RC_SIGN_IN = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        gsc = GoogleSignIn.getClient(MainActivity.this, gso);

        // Check if user details exist in cookies
        if (getCookie("user_email") != null && getCookie("user_name") != null) {
            // If cookies exist, redirect to HomePage
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
            finish();
        } else {
            // If no cookie found, start Google Sign-In automatically
            signIn();
        }
    }

    private void signIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuth(account.getIdToken(), account.getDisplayName(), account.getEmail());
                }
            } catch (Exception e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuth(String idToken, String name, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            if (user != null) {
                                sendUserDataToAPI(name, email, idToken);

                                // Save user details in cookies
                                setCookie("user_name", name);
                                setCookie("user_email", email);

                                // Store in Firebase Database
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("id", user.getUid());
                                map.put("name", name);
                                map.put("profile", user.getPhotoUrl().toString());
                                database.getReference().child("users").child(user.getUid()).setValue(map);

                                // Redirect to HomePage
                                Intent intent = new Intent(MainActivity.this, HomePage.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to send user data to API
    private void sendUserDataToAPI(String name, String email, String googleToken) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // API Endpoint URL
                    URL url = new URL("https://lifecare-medicalstore.in/admin/api/save_user.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // JSON Data
                    String jsonInputString = "{ \"name\": \"" + name + "\", \"email\": \"" + email + "\", \"token\": \"" + googleToken + "\" }";

                    // Send request
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonInputString.getBytes());
                    os.flush();
                    os.close();

                    // Get response
                    Scanner scanner = new Scanner(conn.getInputStream());
                    while (scanner.hasNext()) {
                        System.out.println(scanner.nextLine());
                    }
                    scanner.close();

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Method to set cookies
    private void setCookie(String key, String value) {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie("https://lifecare-medicalstore.in", key + "=" + value);
        CookieSyncManager.getInstance().sync();
    }

    // Method to get cookies
    private String getCookie(String key) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie("https://lifecare-medicalstore.in");
        if (cookies != null) {
            String[] cookieArray = cookies.split("; ");
            for (String cookie : cookieArray) {
                String[] pair = cookie.split("=");
                if (pair.length == 2 && pair[0].equals(key)) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}
