package com.rubenvelasco.bookedup;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

public class BaseActivity {

    private static final int CHECK_INTERVAL = 1000; // Intervalo de verificación de conexión en milisegundos
    private static BaseActivity instance;
    private Handler handler = new Handler();
    private Runnable checkInternetRunnable;
    private Context context;

    private BaseActivity(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized BaseActivity getInstance(Context context) {
        if (instance == null) {
            instance = new BaseActivity(context);
        }
        return instance;
    }

    public void startCheckingInternetConnection() {
        stopCheckingInternetConnection(); // Asegurarse de que no haya tareas repetitivas en ejecución
        checkInternetRunnable = new Runnable() {
            @Override
            public void run() {
                checkInternetConnection();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(checkInternetRunnable);
    }

    public void stopCheckingInternetConnection() {
        handler.removeCallbacks(checkInternetRunnable);
    }

    private void checkInternetConnection() {
        if (!isConnectedToInternet()) {
            Intent intent = new Intent(context, Wifi.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
}
