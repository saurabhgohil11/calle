package com.finch.mycalls;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class ChartWebViewActivity extends Activity {

    WebView webView;
    UsageHistoryItem data;
    long local,std,roamingIC,roamingOG,isd,totalMins;  //to store percentage for chart
    String startDate = "13 Jun";
    String endDate = "13 Jul";
    private int chartWidth,chartHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chart_web_view);
        LinearLayout root = (LinearLayout) findViewById(R.id.root_web_view);

        chartWidth = (int) getResources().getDimension(R.dimen.chart_width);
        chartHeight = (int) getResources().getDimension(R.dimen.chart_height);

        data = (UsageHistoryItem) getIntent().getParcelableExtra("USAGE");
        local=std=roamingIC=roamingOG=isd=0;
        calculateMinutes();

        webView = (WebView)findViewById(R.id.web_view);
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/chart.html");
    }

    private void calculateMinutes() {
        totalMins = data.localSecs + data.stdSecs + data.roamingIncoimgSecs + data.roamingOutgoingSecs + data.ISDSecs;
        if(totalMins == 0) return;
        local = data.localSecs / 1;
        std = data.stdSecs / 1;
        roamingIC = data.roamingIncoimgSecs / 1;
        roamingOG = data.roamingOutgoingSecs / 1;
        isd = data.ISDSecs / 1;
        totalMins = totalMins/ 1;
    }

    public class WebAppInterface {
        @JavascriptInterface
        public long getLocal() {
            return local;
        }
        @JavascriptInterface
        public long getStd() {
            return std;
        }
        @JavascriptInterface
        public long getRoamingIC() {
            return roamingIC;
        }
        @JavascriptInterface
        public long getRoamingOG() {
            return roamingOG;
        }
        @JavascriptInterface
        public long getIsd() {
            return isd;
        }
        @JavascriptInterface
        public long getTotalMins() { return totalMins; }
        @JavascriptInterface
        public String getStartDate() {
            return startDate;
        }
        @JavascriptInterface
        public String getEndDate() {
            return endDate;
        }
        @JavascriptInterface
        public int getChartWidth() { return chartWidth; }
        @JavascriptInterface
        public int getChartHeight() { return chartHeight; }

    }
}
