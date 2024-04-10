package org.b3log.siyuan;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;

public class AgentWebActivity  extends AppCompatActivity implements com.blankj.utilcode.util.Utils.OnAppStatusChangedListener{
    private final String TAG = "AgentWebActivity";
    public AgentWeb mAgentWeb;
    private LinearLayout mLinearLayout;
    private TextView mTitleTextView;
    @Override
    protected void onCreate(final Bundle savedInstanceState) { // 只执行一次。在这里设置布局和初始化数据。在大多数情况下，不需要在 onRestart 中做太多事情，因为 onStart 已经处理了活动可见时的初始化。
        Log.w(TAG, "onCreate() invoked");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agentweb);
        mLinearLayout = (LinearLayout) this.findViewById(R.id.agentweb_container);
        mTitleTextView = (TextView) this.findViewById(R.id.toolbar_title);
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(mLinearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(getUrl());
//        mAgentWeb.getUrlLoader().loadUrl(getUrl());
    }
    private com.just.agentweb.WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //do you  work
            Log.i("Info", "BaseWebActivity onPageStarted");
        }
    };
    private com.just.agentweb.WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (mTitleTextView != null) {
                mTitleTextView.setText(title);
            }
        }
    };

    public String getUrl() {
        return "http://127.0.0.1:58131";
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onForeground(Activity activity) {

    }

    @Override
    public void onBackground(Activity activity) {

    }
    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }
}
