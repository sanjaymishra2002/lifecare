package com.example.lifecare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class HomePage extends AppCompatActivity {
    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;
    private ValueCallback<Uri[]> fileChooserCallback;
    private static final int FILE_CHOOSER_REQUEST_CODE = 100;
    private Snackbar noInternetSnackbar;

    // Allowed URLs
    private static final String PRESCRIPTION_URL = "https://lifecare-medicalstore.in/prescription.php";
    private static final String HOME_URL = "https://lifecare-medicalstore.in/index.php";
    private static final String HOME = "https://lifecare-medicalstore.in";
    private static final String WISHLIST_URL = "https://lifecare-medicalstore.in/wishlist.php";
    private static final String ACCOUNT_URL = "https://lifecare-medicalstore.in/account.php";
    private static final String SEARCH_URL = "https://lifecare-medicalstore.in/searchi.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        webView = findViewById(R.id.webView);
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        fab = findViewById(R.id.fab);
        fab.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        // Enable JavaScript & File Upload
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Custom WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri.toString().startsWith("lifecare://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                checkInternetConnection(); // Check internet when page loads
                toggleUIElements(url); // Hide/show UI elements based on URL
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Save the last failed URL
                lastFailedUrl = failingUrl;
                view.loadUrl("about:blank"); // Show blank page
                showNoInternetSnackbar();
            }
        });


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(null);
                }
                fileChooserCallback = filePathCallback;

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*"); // Supports all file types

                Intent[] intentArray;
                intentArray = new Intent[]{};

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select File");

                startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE);
                return true;
            }

        });

        // Handle bottom navigation menu clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                webView.loadUrl("https://lifecare-medicalstore.in/index.php");
                return true;
            } else if (itemId == R.id.nav_prescription) {
                webView.loadUrl("https://lifecare-medicalstore.in/prescription.php");
                return true;
            } else if (itemId == R.id.nav_wishlist) {
                webView.loadUrl("https://lifecare-medicalstore.in/wishlist.php");
                return true;
            } else if (itemId == R.id.nav_account) {
                webView.loadUrl("https://lifecare-medicalstore.in/account.php");
                return true;
            }
            return false;
        });


        // Handle Floating Action Button click (Search Page)
        fab.setOnClickListener(view -> webView.loadUrl(SEARCH_URL));

        // Pull-to-Refresh Functionality
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        // Load website
        loadWebsite();

        // Monitor network connectivity
        monitorNetworkChanges();
    }

    private void loadWebsite() {
        if (isInternetAvailable()) {
            webView.loadUrl(HOME_URL);
        } else {
            webView.loadUrl("about:blank");
            showNoInternetSnackbar();
        }
    }

    // Hide or Show Bottom Navigation and FAB based on URL
    private void toggleUIElements(String url) {
        // Remove the hash part of the URL (anything after the '#')
        String baseUrl = url.split("#")[0];

        if (baseUrl.equals(HOME_URL) || baseUrl.equals(HOME) || baseUrl.equals(PRESCRIPTION_URL) ||
                baseUrl.equals(WISHLIST_URL) || baseUrl.equals(ACCOUNT_URL) || baseUrl.equals(SEARCH_URL)) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        } else {
            bottomNavigationView.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        }
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // Function to check Internet Connection
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

    // Show Snackbar if no internet
    private void showNoInternetSnackbar() {
        if (noInternetSnackbar == null) {
            noInternetSnackbar = Snackbar.make(findViewById(android.R.id.content),
                    "No Internet Connection", Snackbar.LENGTH_INDEFINITE);
            noInternetSnackbar.show();
        }
    }

    // Dismiss Snackbar if internet is back
    private void dismissNoInternetSnackbar() {
        if (noInternetSnackbar != null) {
            noInternetSnackbar.dismiss();
            noInternetSnackbar = null;
        }
    }

    // Check and update UI based on internet status
    private void checkInternetConnection() {
        if (!isInternetAvailable()) {
            showNoInternetSnackbar();
        } else {
            dismissNoInternetSnackbar();
        }
    }

    // Monitor network changes in real-time
    private String lastFailedUrl = null; // Store the last failed URL

    private void monitorNetworkChanges() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.registerNetworkCallback(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(android.net.Network network) {
                    runOnUiThread(() -> {
                        dismissNoInternetSnackbar();
                        if (webView.getUrl() == null || webView.getUrl().equals("about:blank")) {
                            if (lastFailedUrl != null) {
                                webView.loadUrl(lastFailedUrl); // Reload last failed URL
                                lastFailedUrl = null; // Reset after reload
                            } else {
                                webView.loadUrl(HOME_URL); // Load home if no previous URL
                            }
                        } else {
                            webView.reload(); // Reload the current page
                        }
                    });
                }

                @Override
                public void onLost(android.net.Network network) {
                    runOnUiThread(() -> showNoInternetSnackbar());
                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (fileChooserCallback == null) return;

            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                } else if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                }
            }
            fileChooserCallback.onReceiveValue(results);
            fileChooserCallback = null;
        }
    }



}
