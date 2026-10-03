package com.scottbaker.pranksmssimulator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout messages;
    private ProgressBar progress;
    private TextView status;
    private Button start;
    private EditText recipient;
    private EditText message;
    private EditText count;
    private int sent;
    private int total;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

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
        e.setSingleLine(false);
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
        title.setText("Prank SMS Simulator");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView notice = new TextView(this);
        notice.setText("SIMULATION ONLY • No SMS are sent");
        notice.setTextColor(Color.rgb(255,190,70));
        notice.setTextSize(13);
        root.addView(notice, new LinearLayout.LayoutParams(-1, dp(30)));

        root.addView(label("Recipient (display only)"));
        recipient = field("555-123-4567");
        recipient.setSingleLine(true);
        root.addView(recipient, new LinearLayout.LayoutParams(-1, dp(52)));

        root.addView(label("Prank message"));
        message = field("This is a simulated prank message!");
        root.addView(message, new LinearLayout.LayoutParams(-1, dp(70)));

        root.addView(label("Number of simulated messages (1–50)"));
        count = field("10");
        count.setSingleLine(true);
        root.addView(count, new LinearLayout.LayoutParams(-1, dp(52)));

        start = new Button(this);
        start.setText("START SIMULATION");
        start.setTextColor(Color.WHITE);
        start.setBackgroundColor(Color.rgb(94,92,230));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(52));
        bp.topMargin = dp(12);
        root.addView(start, bp);

        status = new TextView(this);
        status.setText("Ready. Nothing will be sent.");
        status.setTextColor(Color.LTGRAY);
        status.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(34)));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(0);
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(8)));

        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(0, dp(12), 0, dp(24));
        scroll.addView(messages);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 0, 1);
        sp.topMargin = dp(6);
        root.addView(scroll, sp);

        start.setOnClickListener(v -> startSimulation());
        setContentView(root);
    }

    private void startSimulation() {
        String raw = count.getText().toString().trim();
        try { total = Math.max(1, Math.min(50, Integer.parseInt(raw))); }
        catch (Exception e) { total = 10; }
        String text = message.getText().toString().trim();
        if (text.isEmpty()) text = "Simulated prank message";
        String who = recipient.getText().toString().trim();
        if (who.isEmpty()) who = "555-123-4567";

        final String finalText = text;
        final String finalWho = who;
        sent = 0;
        messages.removeAllViews();
        start.setEnabled(false);
        progress.setProgress(0);
        status.setText("Simulating messages to " + finalWho + "…");

        Runnable task = new Runnable() {
            @Override public void run() {
                sent++;
                addBubble(finalText, sent);
                progress.setProgress((sent * 100) / total);
                status.setText("Simulated " + sent + " of " + total);
                if (sent < total) {
                    handler.postDelayed(this, 350);
                } else {
                    start.setEnabled(true);
                    status.setText("Simulation complete — 0 real SMS sent.");
                }
            }
        };
        handler.postDelayed(task, 350);
    }

    private void addBubble(String text, int number) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.END);
        TextView bubble = new TextView(this);
        bubble.setText(text + "\n" + new SimpleDateFormat("h:mm:ss a", Locale.US).format(new Date())
                + "  •  SIMULATED #" + number);
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(15);
        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));
        bubble.setBackgroundColor(Color.rgb(46,46,58));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
        p.width = Math.min(dp(310), dp(310));
        p.bottomMargin = dp(8);
        row.addView(bubble, p);
        messages.addView(row);
    }
}
