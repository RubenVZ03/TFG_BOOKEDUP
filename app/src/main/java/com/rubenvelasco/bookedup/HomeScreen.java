package com.rubenvelasco.bookedup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    private static final String PREFS_NAME = "HomeScreenPrefs";
    private static final String KEY_CATEGORIA = "categoria";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ServicioAdapter servicioAdapter;
    private String categoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.servicios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicialización del adapter
        servicioAdapter = new ServicioAdapter(this);
        recyclerView.setAdapter(servicioAdapter);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        categoria = preferences.getString(KEY_CATEGORIA, "1"); // Categoría predeterminada "1"

        // Cargar servicios de la categoría almacenada
        cargarServiciosDesdeFirebase(categoria);

        BaseActivity.getInstance(this).startCheckingInternetConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseActivity.getInstance(this).startCheckingInternetConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaseActivity.getInstance(this).stopCheckingInternetConnection();
    }

    private void cargarServiciosDesdeFirebase(String categoria) {
        db.collection("Servicios")
                .whereEqualTo("categoria", categoria)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Servicio> servicios = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Servicio servicio = document.toObject(Servicio.class);
                            servicio.setId(document.getId());
                            servicios.add(servicio);
                        }
                        servicioAdapter.setServicios(servicios); // Actualizar el adapter con los servicios cargados
                    } else {
                        // Manejar errores
                        Log.e("HomeScreen", "Error al cargar datos: ", task.getException());
                    }
                });
    }

    public void irAccount(View view) {
        Intent cargarAccount = new Intent(this, AccountScreen.class);
        cargarAccount.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarAccount);
        finish();
    }

    public void irCitas(View view) {
        Intent cargarCitas = new Intent(this, AppointmentsScreen.class);
        cargarCitas.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarCitas);
        finish();
    }

    public void irSearch(View view) {
        Intent cargarSearch = new Intent(this, SearchScreen.class);
        cargarSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarSearch);
        finish();
    }

    @Override
    public void onBackPressed() {
    }

    private void guardarCategoriaEnPreferencias(String categoria) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_CATEGORIA, categoria);
        editor.apply();
    }

    public void cargarBerberia(View view) {
        categoria = "1";
        guardarCategoriaEnPreferencias(categoria);
        cargarServiciosDesdeFirebase(categoria);
    }

    public void cargarTattoo(View view) {
        categoria = "2";
        guardarCategoriaEnPreferencias(categoria);
        cargarServiciosDesdeFirebase(categoria);
    }

    public void cargarPiercing(View view) {
        categoria = "3";
        guardarCategoriaEnPreferencias(categoria);
        cargarServiciosDesdeFirebase(categoria);
    }
}
