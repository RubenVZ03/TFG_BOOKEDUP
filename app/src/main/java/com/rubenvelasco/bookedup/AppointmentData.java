package com.rubenvelasco.bookedup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
public class AppointmentData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_data);

        TextView fechayhoraTextView = findViewById(R.id.fechayhora);

        // Obtener los datos de la cita del Intent
        String fechayhora = getIntent().getStringExtra("fechayhora");

        // Mostrar los datos de la cita
        fechayhoraTextView.setText(fechayhora);
    }

    public void salir(View view) {
        Intent cargarHome = new Intent(this, HomeScreen.class);
        startActivity(cargarHome);
        finish();
    }

    @Override
    public void onBackPressed() {}
}