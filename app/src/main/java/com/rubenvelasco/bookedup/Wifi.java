package com.rubenvelasco.bookedup;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Wifi extends AppCompatActivity {

    private static final int PROGRESS_INTERVAL = 50; // Intervalo de actualización del progreso en milisegundos
    private static final int CHECK_INTERVAL = 1000; // Intervalo de verificación de conexión en milisegundos
    private TextView connectionTextView;
    private ProgressBar progressBar;
    private Handler handler = new Handler();
    private int progressStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        connectionTextView = findViewById(R.id.connectionTextView);
        progressBar = findViewById(R.id.progressBar);

        // Iniciar verificación de conexión periódica
        startCheckingInternetConnection();
    }

    private void startCheckingInternetConnection() {
        progressBar.setVisibility(View.VISIBLE);
        connectionTextView.setVisibility(View.GONE);

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isConnectedToInternet()) {
                    progressStatus += 2; // Incrementar el progreso
                    progressBar.setProgress(progressStatus);
                    if (progressStatus >= 100) {
                        // Conexión exitosa, pasar a la pantalla de inicio de sesión
                        Intent intent = new Intent(Wifi.this, LoginScreen.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Continuar incrementando el progreso si no ha alcanzado el 100%
                        handler.postDelayed(this, PROGRESS_INTERVAL);
                    }
                } else {
                    if (progressStatus >= 55) {
                        // Detener el progreso en el 50% si no hay conexión
                        connectionTextView.setVisibility(View.VISIBLE);
                        connectionTextView.setText("No hay conexión a Internet.\nVerifica tu conexión e intenta nuevamente.");
                        // Reiniciar el progreso para volver a intentarlo
                        progressStatus = 0;
                    } else {
                        // Continuar incrementando el progreso hasta el 50%
                        progressStatus += 10;
                        progressBar.setProgress(progressStatus);
                    }
                    // Reintentar verificación después de CHECK_INTERVAL
                    handler.postDelayed(this, CHECK_INTERVAL);
                }
            }
        });
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
}