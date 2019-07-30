package com.nxmtl.android.sportsview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    boolean connect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SportsView sportsView = (SportsView) findViewById(R.id.sportsView);

        SportsData sportsData = new SportsData();
        sportsData.step = 2714;
        sportsData.distance = 1700;
        sportsData.calories = 34;
        sportsData.progress = 75;
        sportsView.setSportsData(sportsData);

        handler = new Handler();
        final Button connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connect = !connect;
                        sportsView.setIsLoading(!connect);
                        connectButton.setText(connect? "disconnect" : "connect");
                    }
                }, 500);
            }
        });
    }
}
