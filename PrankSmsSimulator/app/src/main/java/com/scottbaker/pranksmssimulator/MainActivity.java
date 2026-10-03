package com.scottbaker.pranksmssimulator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;
import android.content.SharedPreferences;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private EditText destination;
    private EditText endpoint;
    private EditText status;
    private SharedPreferences prefs;
    private Button callButton;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("caller", MODE_PRIVATE);
        buildUi();
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + .5f);
    }

    private TextView label(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.LTGRAY);
        v.setTextSize(13);
        v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setPadding(0, dp(8), 0, dp(4));
        return v;
    }

    private EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.GRAY);
        e.setTextColor(Color.WHITE);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setPadding(dp(14), dp(10), dp(14), dp(10));
        e.setBackgroundColor(Color.rgb(35,35,43));
        return e;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(12), dp(18), dp(12));
        root.setBackgroundColor(Color.rgb(16,16,20));

        TextView title = new TextView(this);
        title.setText("Local Caller");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView notice = new TextView(this);
        notice.setText("REAL CALLS • Uses a configured VoIP provider");
        notice.setTextColor(Color.rgb(255,190,70));
        notice.setTextSize(13);
        root.addView(notice, new LinearLayout.LayoutParams(-1, dp(30)));

        root.addView(label("Number to call"));
        destination = field("+13125550123");
        destination.setText(prefs.getString("destination", ""));
        root.addView(destination, new LinearLayout.LayoutParams(-1, dp(52)));

        root.addView(label("Calling service endpoint"));
        endpoint = field("https://your-server.example.com/call");
        endpoint.setText(prefs.getString("endpoint", ""));
        root.addView(endpoint, new LinearLayout.LayoutParams(-1, dp(52)));

        TextView info = new TextView(this);
        info.setText("The server must be configured with your VoIP account and its approved outbound caller ID. The app does not accept an arbitrary caller ID.");
        info.setTextColor(Color.LTGRAY);
        info.setTextSize(13);
        info.setPadding(0, dp(8), 0, dp(12));
        root.addView(info);

        callButton = new Button(this);
        callButton.setText("CALL");
        callButton.setTextColor(Color.WHITE);
        callButton.setBackgroundColor(Color.rgb(52, 168, 83));
        root.addView(callButton, new LinearLayout.LayoutParams(-1, dp(52)));

        status = new EditText(this);
        status.setText("Ready.");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(14);
        status.setGravity(Gravity.TOP);
        status.setFocusable(false);
        status.setBackgroundColor(Color.TRANSPARENT);
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(100)));

        callButton.setOnClickListener(v -> placeCall());
        setContentView(root);
    }

    private void placeCall() {
        final String to = destination.getText().toString().trim();
        final String url = endpoint.getText().toString().trim();

        if (to.isEmpty()) {
            status.setText("Enter the number to call.");
            return;
        }
        if (!url.startsWith("https://")) {
            status.setText("Use an HTTPS calling-service endpoint.");
            return;
        }

        prefs.edit()
                .putString("destination", to)
                .putString("endpoint", url)
                .apply();

        callButton.setEnabled(false);
        status.setText("Starting call…");

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL target = new URL(url);
                connection = (HttpURLConnection) target.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                String body = "{\"to\":\"" + jsonEscape(to) + "\"}";
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
                int code = connection.getResponseCode();
                runOnUiThread(() -> {
                    callButton.setEnabled(true);
                    if (code >= 200 && code < 300) {
                        status.setText("Call request accepted by the calling service.");
                    } else {
                        status.setText("Calling service returned HTTP " + code + ".");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    callButton.setEnabled(true);
                    status.setText("Call failed: " + e.getMessage());
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
