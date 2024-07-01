package com.rubenvelasco.bookedup;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchScreen extends AppCompatActivity {

    private AutoCompleteTextView etUbi, etSearch;
    private TextInputEditText etClock;
    private FirebaseFirestore db;

    private Set<String> ubicacionesSet = new HashSet<>();
    private Set<String> nombresSet = new HashSet<>();

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_LOCATION_SETTINGS = 2;

    private ViewPager viewPager, viewPagerOfertas;
    private ServicioPagerAdapter servicioPagerAdapter, servicioOfertasPagerAdapter;
    private List<Servicio> servicios, serviciosConOfertas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        db = FirebaseFirestore.getInstance();
        etUbi = findViewById(R.id.etUbi);
        etSearch = findViewById(R.id.etSearch);

        etClock = findViewById(R.id.etClock);
        etClock.setOnClickListener(v -> showTimePickerDialog());

        // Configurar adaptador para el AutoCompleteTextView
        ArrayAdapter<String> adapterU = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        ArrayAdapter<String> adapterN = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        etUbi.setAdapter(adapterU);
        etSearch.setAdapter(adapterN);

        cargarUbicaciones(adapterU);
        cargarActividades(adapterN);

        viewPager = findViewById(R.id.viewPager);
        viewPagerOfertas = findViewById(R.id.viewPagerOfertas);
        servicios = new ArrayList<>();
        serviciosConOfertas = new ArrayList<>();

        // Añadir un listener para detectar cuando se selecciona una actividad
        etSearch.setOnItemClickListener((parent, view, position, id) -> {
            String actividadSeleccionada = adapterN.getItem(position);
            String horaSeleccionada = etClock.getText().toString();
            String ciudadSeleccionada = etUbi.getText().toString();

            if (!horaSeleccionada.isEmpty() && !ciudadSeleccionada.isEmpty()) {
                cargarServiciosPorActividadYHoraYCiudad(actividadSeleccionada, horaSeleccionada, ciudadSeleccionada);
            } else if (!horaSeleccionada.isEmpty()) {
                cargarServiciosPorActividadYHora(actividadSeleccionada, horaSeleccionada);
            } else if (!ciudadSeleccionada.isEmpty()) {
                cargarServiciosPorActividadYCiudad(actividadSeleccionada, ciudadSeleccionada);
            } else {
                cargarServiciosPorActividad(actividadSeleccionada);
            }
        });

        // Añadir un listener para detectar cuando se selecciona una ciudad
        etUbi.setOnItemClickListener((parent, view, position, id) -> {
            String ciudadSeleccionada = (String) parent.getItemAtPosition(position);
            String actividadSeleccionada = etSearch.getText().toString(); // Obtener la actividad seleccionada desde el etSearch
            String horaSeleccionada = etClock.getText().toString();

            if (!horaSeleccionada.isEmpty() && !actividadSeleccionada.isEmpty()) {
                cargarServiciosPorActividadYHoraYCiudad(actividadSeleccionada, horaSeleccionada, ciudadSeleccionada);
            } else if (!horaSeleccionada.isEmpty()) {
                cargarServiciosPorCiudadYHora(ciudadSeleccionada, horaSeleccionada);
            } else if (!actividadSeleccionada.isEmpty()) {
                cargarServiciosPorActividadYCiudad(actividadSeleccionada, ciudadSeleccionada);
            } else {
                cargarServiciosPorCiudad(ciudadSeleccionada);
            }
        });


        cargarIdsAleatorios();
        cargarOfertas();
        BaseActivity.getInstance(this).startCheckingInternetConnection();
    }

    private void cargarServiciosPorActividadYCiudad(String actividad, String ciudad) {
        db.collection("Servicios")
                .whereArrayContains("actividades", actividad)
                .whereEqualTo("ciudad", ciudad)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> servicioIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            servicioIds.add(document.getId());
                        }
                        cargarDetallesServicios(servicioIds);
                    } else {
                        Log.d("SearchScreen", "Error al cargar servicios", task.getException());
                    }
                });
    }

    private void cargarServiciosPorActividadYHoraYCiudad(String actividad, String horaSeleccionada, String ciudad) {
        db.collection("Servicios")
                .whereArrayContains("actividades", actividad)
                .whereEqualTo("ciudad", ciudad)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> servicioIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ArrayList<String> diasDisponibles = (ArrayList<String>) document.get("dias");
                            if (diasDisponibles != null) {
                                for (String dia : diasDisponibles) {
                                    if (isHoraEnRango(dia, horaSeleccionada)) {
                                        servicioIds.add(document.getId());
                                        break;
                                    }
                                }
                            }
                        }
                        cargarDetallesServicios(servicioIds);
                    } else {
                        Log.d("SearchScreen", "Error al cargar servicios", task.getException());
                    }
                });
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

    private void cargarOfertas() {
        db.collection("Servicios").whereEqualTo("oferta", true).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> servicioIds = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            String servicioId = document.getId();
                            servicioIds.add(servicioId);
                        }
                        cargarDetallesOfertas(servicioIds);
                    } else {
                        Toast.makeText(SearchScreen.this, "Error al obtener servicios con ofertas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarIdsAleatorios() {
        db.collection("Servicios").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documentos = task.getResult().getDocuments();
                        if (documentos.size() >= 4) {
                            List<String> servicioIds = new ArrayList<>();
                            for (DocumentSnapshot document : documentos) {
                                servicioIds.add(document.getId());
                            }
                            Collections.shuffle(servicioIds);
                            List<String> idsAleatorios = servicioIds.subList(0, 4);
                            cargarDetallesServicios(idsAleatorios);
                        } else {
                            Toast.makeText(SearchScreen.this, "No hay suficientes servicios en la base de datos.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SearchScreen.this, "Error al obtener servicios: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarDetallesOfertas(List<String> servicioIds) {
        serviciosConOfertas.clear();
        for (String servicioId : servicioIds) {
            db.collection("Servicios").document(servicioId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String nombreServicio = documentSnapshot.getString("nombre");
                            String horarioServicio = documentSnapshot.getString("horario");
                            String imageUrlServicio = documentSnapshot.getString("imageUrl");
                            String categoria = documentSnapshot.getString("categoria");
                            String profesional = documentSnapshot.getString("profesional");
                            String ubicacion = documentSnapshot.getString("ubicacion");

                            ArrayList<String> diasDisponibles = (ArrayList<String>) documentSnapshot.get("diasDisponibles");
                            ArrayList<String> ofertas = (ArrayList<String>) documentSnapshot.get("ofertas");

                            // Concatenar las ofertas en un solo string con saltos de línea
                            StringBuilder ofertasStringBuilder = new StringBuilder();
                            for (String oferta : ofertas) {
                                ofertasStringBuilder.append(oferta).append("\n");
                            }
                            String ofertasString = ofertasStringBuilder.toString().trim(); // Eliminar el último salto de línea

                            serviciosConOfertas.add(new Servicio(nombreServicio, ofertasString, horarioServicio, imageUrlServicio, categoria, diasDisponibles, profesional, ubicacion));
                            if (serviciosConOfertas.size() == servicioIds.size()) {
                                servicioOfertasPagerAdapter = new ServicioPagerAdapter(SearchScreen.this, serviciosConOfertas);
                                viewPagerOfertas.setAdapter(servicioOfertasPagerAdapter);
                            }
                        } else {
                            Toast.makeText(SearchScreen.this, "Error al obtener detalles del servicio.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void cargarDetallesServicios(List<String> servicioIds) {
        servicios.clear();
        for (String servicioId : servicioIds) {
            db.collection("Servicios").document(servicioId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String nombreServicio = documentSnapshot.getString("nombre");
                            String descripcionServicio = documentSnapshot.getString("descripcion");
                            String horarioServicio = documentSnapshot.getString("horario");
                            String imageUrlServicio = documentSnapshot.getString("imageUrl");
                            String categoria = documentSnapshot.getString("categoria");
                            String profesional = documentSnapshot.getString("profesional");
                            String ubicacion = documentSnapshot.getString("ubicacion");

                            ArrayList<String> diasDisponibles = (ArrayList<String>) documentSnapshot.get("dias");

                            servicios.add(new Servicio(nombreServicio, descripcionServicio, horarioServicio, imageUrlServicio, categoria, diasDisponibles, profesional, ubicacion));

                            if (servicios.size() == servicioIds.size()) {
                                servicioPagerAdapter = new ServicioPagerAdapter(SearchScreen.this, servicios);
                                viewPager.setAdapter(servicioPagerAdapter);
                            }
                        } else {
                            Toast.makeText(SearchScreen.this, "Error al cargar datos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void cargarServiciosPorCiudad(String ciudad) {
        db.collection("Servicios")
                .whereEqualTo("ciudad", ciudad)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> servicioIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            servicioIds.add(document.getId());
                        }
                        cargarDetallesServicios(servicioIds);
                    } else {
                        Log.d("SearchScreen", "Error al cargar servicios", task.getException());
                    }
                });
    }

    // Método para cargar los servicios basados en la actividad seleccionada
    private void cargarServiciosPorActividad(String actividad) {
        db.collection("Servicios")
                .whereArrayContains("actividades", actividad)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        servicios.clear(); // Limpiar la lista antes de cargar nuevos servicios

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombreServicio = document.getString("nombre");
                            String descripcionServicio = document.getString("descripcion");
                            String horarioServicio = document.getString("horario");
                            String imageUrlServicio = document.getString("imageUrl");
                            String categoria = document.getString("categoria");
                            String profesional = document.getString("profesional");
                            String ubicacion = document.getString("ubicacion");

                            // Obtener la lista de días disponibles
                            ArrayList<String> diasDisponibles = (ArrayList<String>) document.get("dias");

                            // Agregar detalles del servicio al ArrayList
                            servicios.add(new Servicio(nombreServicio, descripcionServicio, horarioServicio, imageUrlServicio, categoria, diasDisponibles, profesional, ubicacion));
                        }

                        // Configurar el adaptador del ViewPager con los servicios filtrados
                        servicioPagerAdapter = new ServicioPagerAdapter(SearchScreen.this, servicios);
                        viewPager.setAdapter(servicioPagerAdapter);

                        if (servicios.isEmpty()) {
                            Toast.makeText(SearchScreen.this, "No se encontraron servicios para la actividad seleccionada.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SearchScreen.this, "Error al cargar servicios: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarUbicaciones(ArrayAdapter<String> adapter) {
        db.collection("Servicios")
                .orderBy("ciudad")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ubicacionesSet.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String ubicacion = document.getString("ciudad");
                            if (ubicacion != null) {
                                ubicacionesSet.add(ubicacion);
                            }
                        }

                        adapter.addAll(ubicacionesSet);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("SearchScreen", "Error al cargar ubicaciones", task.getException());
                    }
                });
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minuteOfHour);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String horaSeleccionada = sdf.format(calendar.getTime());
            etClock.setText(horaSeleccionada);

            String actividadSeleccionada = etSearch.getText().toString();
            String ciudadSeleccionada = etUbi.getText().toString();

            if (!horaSeleccionada.isEmpty() && !actividadSeleccionada.isEmpty() && !ciudadSeleccionada.isEmpty()) {
                cargarServiciosPorActividadYHoraYCiudad(actividadSeleccionada, horaSeleccionada, ciudadSeleccionada);
            } else if (!horaSeleccionada.isEmpty() && !actividadSeleccionada.isEmpty()) {
                cargarServiciosPorActividadYHora(actividadSeleccionada, horaSeleccionada);
            } else if (!ciudadSeleccionada.isEmpty()) {
                cargarServiciosPorCiudadYHora(ciudadSeleccionada, horaSeleccionada);
            } else {
                cargarServiciosPorHora(horaSeleccionada);
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void cargarServiciosPorHora(String horaSeleccionada) {
        db.collection("Servicios").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> servicioIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ArrayList<String> diasDisponibles = (ArrayList<String>) document.get("dias");
                            if (diasDisponibles != null) {
                                for (String dia : diasDisponibles) {
                                    if (isHoraEnRango(dia, horaSeleccionada)) {
                                        servicioIds.add(document.getId());
                                        break;
                                    }
                                }
                            }
                        }
                        cargarDetallesServicios(servicioIds);
                    } else {
                        Log.d("SearchScreen", "Error al cargar servicios", task.getException());
                    }
                });
    }

    // Método para cargar servicios basados en ciudad y hora seleccionada
    private void cargarServiciosPorCiudadYHora(String ciudad, String horaSeleccionada) {
        db.collection("Servicios")
                .whereEqualTo("ciudad", ciudad)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> servicioIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ArrayList<String> diasDisponibles = (ArrayList<String>) document.get("dias");
                            if (diasDisponibles != null) {
                                for (String dia : diasDisponibles) {
                                    if (isHoraEnRango(dia, horaSeleccionada)) {
                                        servicioIds.add(document.getId());
                                        break;
                                    }
                                }
                            }
                        }
                        cargarDetallesServicios(servicioIds);
                    } else {
                        Log.d("SearchScreen", "Error al cargar servicios", task.getException());
                    }
                });
    }

    private void cargarServiciosPorActividadYHora(String actividad, String horaSeleccionada) {
        db.collection("Servicios")
                .whereArrayContains("actividades", actividad)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> servicioIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ArrayList<String> diasDisponibles = (ArrayList<String>) document.get("dias");
                            if (diasDisponibles != null) {
                                for (String dia : diasDisponibles) {
                                    if (isHoraEnRango(dia, horaSeleccionada)) {
                                        servicioIds.add(document.getId());
                                        break;
                                    }
                                }
                            }
                        }
                        cargarDetallesServicios(servicioIds);
                    } else {
                        Log.d("SearchScreen", "Error al cargar servicios", task.getException());
                    }
                });
    }


    private boolean isHoraEnRango(String dia, String horaSeleccionada) {
        try {
            String[] partes = dia.split(" ");
            if (partes.length < 2) {
                return false; // No tiene el formato esperado
            }

            String rangoHorario = partes[1].replace("–", "-"); // Reemplazar el guion medio con guion para manejar diferentes formatos
            String[] rango = rangoHorario.split("-");
            if (rango.length < 2) {
                return false; // No tiene el formato esperado
            }

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String horaInicio = rango[0].trim();
            String horaFin = rango[1].trim();

            Calendar horaSeleccionadaCal = Calendar.getInstance();
            horaSeleccionadaCal.setTime(sdf.parse(horaSeleccionada));

            Calendar horaInicioCal = Calendar.getInstance();
            horaInicioCal.setTime(sdf.parse(horaInicio));

            Calendar horaFinCal = Calendar.getInstance();
            horaFinCal.setTime(sdf.parse(horaFin));

            return horaSeleccionadaCal.compareTo(horaInicioCal) >= 0 && horaSeleccionadaCal.compareTo(horaFinCal) <= 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void cargarActividades(ArrayAdapter<String> adapter) {
        db.collection("Actividades")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        nombresSet.clear();
                        List<String> actividadesList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String service1 = document.getString("servicio_1");
                            String service2 = document.getString("servicio_2");
                            String service3 = document.getString("servicio_3");
                            if (service1 != null) {
                                nombresSet.add(service1);
                                actividadesList.add(service1);
                            }

                            if (service2 != null) {
                                nombresSet.add(service2);
                                actividadesList.add(service2);
                            }

                            if (service3 != null) {
                                nombresSet.add(service3);
                                actividadesList.add(service3);
                            }
                        }

                        adapter.addAll(actividadesList);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("SearchScreen", "Error al cargar nombres", task.getException());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, iniciar MapsActivity
                iniciarMapsActivity(1);
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                iniciarMapsActivity(0);
            }
        }
    }

    private void verificarYActivarUbicacion() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // La ubicación no está activada, pero abrimos MapsActivity de todos modos
            iniciarMapsActivity(0); // 0 indica que el GPS no está activo
        } else {
            // La ubicación está activada, iniciar MapsActivity
            iniciarMapsActivity(1); // 1 indica que la ubicación está activa
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            // Verificar si el usuario activó la ubicación después de la solicitud
            verificarYActivarUbicacion();
        }
    }

    public void irMapa(View view) {
        // Verificar y solicitar permisos de ubicación si no están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos de ubicación
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Si el permiso ya está concedido, iniciar MapsActivity directamente
            iniciarMapsActivity(1);
        }
    }

    private void iniciarMapsActivity(int resultadoPermiso) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("PERMISSION_RESULT", resultadoPermiso);
        startActivity(intent);
    }

    public void irHome(View view) {
        Intent cargarHomeScreen = new Intent(this, HomeScreen.class);
        cargarHomeScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarHomeScreen);
        finish();
    }

    public void irCitas(View view) {
        Intent cargarCitas = new Intent(this, AppointmentsScreen.class);
        cargarCitas.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarCitas);
        finish();
    }

    public void irAccount(View view) {
        Intent cargarAccount = new Intent(this, AccountScreen.class);
        cargarAccount.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarAccount);
        finish();
    }

    @Override
    public void onBackPressed() {
    }

    public void eliminarFiltros(View view) {
        boolean textoBorrado = false;

        if (!TextUtils.isEmpty(etSearch.getText())) {
            etSearch.setText("");
            textoBorrado = true;
        }
        if (!TextUtils.isEmpty(etClock.getText())) {
            etClock.setText("");
            textoBorrado = true;
        }
        if (!TextUtils.isEmpty(etUbi.getText())) {
            etUbi.setText("");
            textoBorrado = true;
        }

        if (textoBorrado) {
            cargarIdsAleatorios();
        }
    }
}
