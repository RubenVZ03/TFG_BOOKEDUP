package com.rubenvelasco.bookedup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentsScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewPendientes;
    private RecyclerView recyclerViewCompletadas;
    private CitaAdapter adapterPendientes;
    private CitaAdapter adapterCompletadas;
    private List<Cita> citasPendientes;
    private List<Cita> citasCompletadas;

    private Handler handler;
    private final int INTERVALO_ACTUALIZACION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewPendientes = findViewById(R.id.recyclerViewPendientes);
        recyclerViewPendientes.setLayoutManager(new LinearLayoutManager(this));
        citasPendientes = new ArrayList<>();
        adapterPendientes = new CitaAdapter(citasPendientes);
        recyclerViewPendientes.setAdapter(adapterPendientes);

        recyclerViewCompletadas = findViewById(R.id.recyclerViewCompletadas);
        recyclerViewCompletadas.setLayoutManager(new LinearLayoutManager(this));
        citasCompletadas = new ArrayList<>();
        adapterCompletadas = new CitaAdapter(citasCompletadas);
        recyclerViewCompletadas.setAdapter(adapterCompletadas);

        loadAppointments();
    }

    private void loadAppointments() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            List<DocumentReference> citasRefs = (List<DocumentReference>) documentSnapshot.get("citas");
            if (citasRefs != null) {
                for (DocumentReference citaRef : citasRefs) {
                    citaRef.get().addOnSuccessListener(citaSnapshot -> {
                        Cita cita = citaSnapshot.toObject(Cita.class);
                        if (cita != null) {
                            if ("Pendiente".equals(cita.getEstado())) {
                                citasPendientes.add(cita);
                                adapterPendientes.notifyDataSetChanged();
                            } else if ("Completado".equals(cita.getEstado())) {
                                citasCompletadas.add(cita);
                                adapterCompletadas.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        iniciarActualizacionCitasPasadas();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detenerActualizacionCitasPasadas();
    }

    private void iniciarActualizacionCitasPasadas() {
        handler = new Handler();
        handler.post(actualizarCitasPasadasRunnable);
    }

    private void detenerActualizacionCitasPasadas() {
        handler.removeCallbacks(actualizarCitasPasadasRunnable);
    }

    private Runnable actualizarCitasPasadasRunnable = new Runnable() {
        @Override
        public void run() {
            actualizarCitasPasadas();
            handler.postDelayed(this, INTERVALO_ACTUALIZACION);
        }
    };

    public void actualizarCitasPasadas() {
        Date fechaActual = new Date(); // Obtener la fecha y hora actuales

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Citas")
                .whereLessThan("timestamp", fechaActual) // Filtrar citas cuyo timestamp sea anterior al momento actual
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Actualizar el estado de cada cita a "Completado"
                            String citaId = document.getId();
                            db.collection("Citas").document(citaId)
                                    .update("estado", "Completado")
                                    .addOnSuccessListener(aVoid -> {
                                        // Estado actualizado exitosamente para la cita con ID citaId
                                    })
                                    .addOnFailureListener(e -> {
                                        // Manejar errores
                                    });
                        }
                    } else {
                        // Manejar errores al obtener las citas
                    }
                });
    }

    public void irHome(View view) {
        Intent cargarHomeScreen = new Intent(this, HomeScreen.class);
        cargarHomeScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarHomeScreen);
        finish();
    }

    public void irAccount(View view) {
        Intent cargarAccount = new Intent(this, AccountScreen.class);
        cargarAccount.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarAccount);
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
}