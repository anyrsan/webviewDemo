package any.com.webview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.AnimRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * Created by anyrsan on 2018/2/10.
 */
public class MainActivity extends AppCompatActivity {

    WebView webView;
    SwipeRefreshLayout refreshLayout;
    String baseurl = "https://m.galaxyclub.cn/bbs/galaxys_s7-0-last-0.html";

    ProgressBar pbar;
    FrameLayout frameLayout;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessage5;
    public static final int FILECHOOSER_RESULTCODE = 5173;
    public static final int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 5174;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        refreshLayout = findViewById(R.id.refresh);
        webView = findViewById(R.id.webview);
        pbar = findViewById(R.id.pbar);

        frameLayout = findViewById(R.id.frame);

        initWebViewSetting(webView);

        webView.loadUrl(baseurl);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.loadUrl(baseurl);
            }
        });
    }


    @Override
    public void onBackPressed() {
        final int size = frameLayout.getChildCount();
        if (size > 0) {
            animView(frameLayout, R.anim.bottom_out_anim, new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    frameLayout.removeViewAt(0);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void initWebViewSetting(WebView a) {
        a.setWebViewClient(webViewClient);
        a.setWebChromeClient(webChromeClient);
        a.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        a.getSettings().setLoadWithOverviewMode(true);
        a.getSettings().setUseWideViewPort(true);
        a.getSettings().setLoadsImagesAutomatically(true);
        a.getSettings().setSaveFormData(false);
        a.getSettings().setUserAgentString(a.getSettings().getUserAgentString() + "sm-bbs");
        a.setVerticalScrollBarEnabled(false);
        a.getSettings().setSupportZoom(false);
        a.getSettings().setJavaScriptEnabled(true);
        a.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        a.getSettings().setPluginState(WebSettings.PluginState.ON);
        a.getSettings().setBuiltInZoomControls(false);
        a.getSettings().setDomStorageEnabled(true);
        a.getSettings().setSupportMultipleWindows(true);
        a.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        a.getSettings().setDefaultTextEncodingName("UTF-8");
        //处理https下加载http资源
        a.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }


    private void animView(View view, @AnimRes int animRId, Animation.AnimationListener animationListener) {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), animRId);
        view.startAnimation(animation);
        animation.setAnimationListener(animationListener);
    }


    WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    };

    WebChromeClient webChromeClient = new WebChromeClient() {


        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress > 95) {
                refreshLayout.setRefreshing(false);
            }
            pbar.setProgress(newProgress);
            if (newProgress >= 95) {
                pbar.setVisibility(View.GONE);
            } else {
                pbar.setVisibility(View.VISIBLE);
            }

        }


        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {

            WebView newWebView = new WebView(MainActivity.this);
            initWebViewSetting(newWebView);
            frameLayout.addView(newWebView, frameLayout.getWidth(), frameLayout.getHeight());

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();

            newWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    view.loadUrl(request.getUrl().toString());
                    return true;
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();
                }
            });

            animView(frameLayout, R.anim.bottom_in_anim, null);

            return true;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
        }

        @Override
        public void onCloseWindow(final WebView window) {
            animView(frameLayout, R.anim.bottom_out_anim, new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    frameLayout.removeView(window);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        }

        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            this.openFileChooser(uploadMsg, "*/*");
        }

        // For Android >= 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType) {
            this.openFileChooser(uploadMsg, acceptType, null);
        }

        // For Android >= 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, "File Browser"),
                    FILECHOOSER_RESULTCODE);
        }

        // For Lollipop 5.0+ Devices
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView,
                                         ValueCallback<Uri[]> filePathCallback,
                                         WebChromeClient.FileChooserParams fileChooserParams) {
            if (mUploadMessage5 != null) {
                mUploadMessage5.onReceiveValue(null);
                mUploadMessage5 = null;
            }
            mUploadMessage5 = filePathCallback;
            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent,
                        FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
            } catch (ActivityNotFoundException e) {
                mUploadMessage5 = null;
                return false;
            }
            return true;
        }
    };



    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            if (null == mUploadMessage5) {
                return;
            }
            mUploadMessage5.onReceiveValue(WebChromeClient.FileChooserParams
                    .parseResult(resultCode, intent));
            mUploadMessage5 = null;
        }
    }

}
