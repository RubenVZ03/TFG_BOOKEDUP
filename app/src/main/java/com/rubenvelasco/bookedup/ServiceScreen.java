package com.rubenvelasco.bookedup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class ServiceScreen extends AppCompatActivity {

    private TextView nombreTextView;
    private TextView descripcionTextView;
    private TextView horarioTextView;
    private ImageView imagen;
    private LinearLayout linearLayout;
    private List<String> diasDisponibles = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String nombre, descripcion, horario, profesional, ubicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nombreTextView = findViewById(R.id.titulo);
        descripcionTextView = findViewById(R.id.descripcionServicio);
        horarioTextView = findViewById(R.id.horarioServicio);
        imagen = findViewById(R.id.imagen);
        linearLayout = findViewById(R.id.botones);

        Bundle extras = getIntent().getExtras();
        nombre = extras.getString("nombre");
        descripcion = extras.getString("descripcion");
        horario = extras.getString("horario");
        String imageUrl = extras.getString("imageUrl");
        profesional = extras.getString("profesional");
        ubicacion = extras.getString("ubicacion");
        diasDisponibles = extras.getStringArrayList("dias");

        nombreTextView.setText(nombre);
        descripcionTextView.setText(descripcion);
        horarioTextView.setText(horario);

        // Cargar imagen utilizando Picasso y redimensionarla con esquinas redondeadas sin margen
        int radius = 110; // El radio de las esquinas en píxeles
        int margin = 0; // Sin margen
        Transformation transformation = new RoundedCornersTransformation(radius, margin);

        Picasso.get()
                .load(imageUrl)
                .fit()  // Ajustar la imagen al tamaño del ImageView
                .transform(transformation)
                .into(imagen);

        if (extras.getString("categoria").equals("1")) {
            agregarBotonesBarberia();
        } else if (extras.getString("categoria").equals("2")) {
            agregarBotonesTatto();
        } else if (extras.getString("categoria").equals("3")) {
            agregarBotonesPiercing();
        }
        BaseActivity.getInstance(this).startCheckingInternetConnection();
    }

    private void cuadroDialogo(String servicio) {
        final Calendar fechaActual = Calendar.getInstance();
        final Calendar fecha = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(ServiceScreen.this, R.style.CustomDatePickerDialog, (view, year, month, dayOfMonth) -> {
            fecha.set(year, month, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(ServiceScreen.this, R.style.CustomTimePickerDialog, (view1, hourOfDay, minute) -> {
                fecha.set(Calendar.HOUR_OF_DAY, hourOfDay);
                fecha.set(Calendar.MINUTE, minute);

                // Obtener la hora actual
                Calendar horaActual = Calendar.getInstance();

                // Verificar si la hora seleccionada es mayor o igual a la hora actual
                if (fecha.compareTo(horaActual) >= 0) {
                    // FORMATO
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE HH:mm", new Locale("es", "ES"));
                    SimpleDateFormat sdfDia = new SimpleDateFormat("dd", new Locale("es", "ES"));
                    SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", new Locale("es", "ES"));
                    SimpleDateFormat sdfMes = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
                    SimpleDateFormat sdfAño = new SimpleDateFormat("yyyy", new Locale("es", "ES"));
                    SimpleDateFormat sdfDiaCita = new SimpleDateFormat("EEEE", new Locale("es", "ES"));

                    String fechayhora = sdf.format(fecha.getTime());
                    String horaCita = sdfHora.format(fecha.getTime());
                    String diaCita = sdfDia.format(fecha.getTime());
                    String mesCita = sdfMes.format(fecha.getTime());
                    String anioCita = sdfAño.format(fecha.getTime());
                    String diaCitaForm = sdfDiaCita.format(fecha.getTime());
                    String mesCitaFormat = mesCita.substring(0, 1).toUpperCase() + mesCita.substring(1);


                    Date timestamp = fecha.getTime();  // Obtener el timestamp como la fecha seleccionada

                    if (diasDisponibles != null) {
                        for (String dia : diasDisponibles) {
                            if (isCitaValida(dia, horaCita, diaCitaForm)) {
                                Toast.makeText(ServiceScreen.this, "Cita reservada para: " + fechayhora, Toast.LENGTH_SHORT).show();
                                createAppointment(diaCita, horaCita, mesCitaFormat, anioCita, timestamp, servicio);
                                break;
                            } else {
                                Toast.makeText(ServiceScreen.this, "Horario no disponible", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }

                } else {
                    Toast.makeText(ServiceScreen.this, "Por favor, selecciona una hora futura", Toast.LENGTH_SHORT).show();
                }
            }, fechaActual.get(Calendar.HOUR_OF_DAY), fechaActual.get(Calendar.MINUTE), true);

            timePickerDialog.show();
        }, fechaActual.get(Calendar.YEAR), fechaActual.get(Calendar.MONTH), fechaActual.get(Calendar.DATE));

        // Deshabilitar fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private boolean isCitaValida(String dia, String horaSeleccionada, String diaCita) {
        try {
            String[] partes = dia.split(" ");
            if (partes.length < 2) {
                return false; // No tiene el formato esperado
            }

            String rangoHorario = partes[1].replace("–", "-");
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

            // Verificar que el día de la cita coincide con uno de los días disponibles
            Log.i("Pruebas", diaCita);
            Log.i("Pruebas", diasDisponibles.toString());

            // Verificar si el día de la cita coincide con uno de los días disponibles
            for (String diaDisponible : diasDisponibles) {
                String[] partesDisponible = diaDisponible.split(" ");
                if (partesDisponible[0].trim().equals(diaCita)) {
                    // El día de la cita está disponible, verificar el horario
                    if (horaSeleccionadaCal.compareTo(horaInicioCal) >= 0 && horaSeleccionadaCal.compareTo(horaFinCal) <= 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            return false; // El día de la cita no está disponible
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
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

    private void agregarBotonesBarberia() {
        String[] nombresBotones = {"Corte \nCompleto", "Corte \nClásico", "Recortar \nBarba"};
        String[] precios = {"10€", "8€", "3€"};
        for (int i = 0; i < nombresBotones.length; i++) {
            Button boton = new Button(this);
            boton.setText(nombresBotones[i] + "\n" + precios[i]);
            boton.setTextColor(Color.WHITE); // Cambiar color de texto
            boton.setBackground(getDrawable(R.drawable.btn_personalizado)); // Establecer fondo personalizado
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(convertirDpToPx(5), 0, convertirDpToPx(5), convertirDpToPx(5)); // Agregar margen inferior
            boton.setLayoutParams(params);
            String servicio = nombresBotones[i];
            boton.setOnClickListener(v -> cuadroDialogo(servicio));
            linearLayout.addView(boton);
        }
    }

    private void agregarBotonesTatto() {
        String[] nombresBotones = {"Tatuaje \nNegro", "Tatuaje \nColor", "Eliminar \nTatuaje"};
        String[] precios = {"25€ – 300€", "30€ – 350€", "200€ – 400€"};
        for (int i = 0; i < nombresBotones.length; i++) {
            Button boton = new Button(this);
            boton.setText(nombresBotones[i] + "\n" + precios[i]);
            boton.setTextColor(Color.WHITE); // Cambiar color de texto
            boton.setBackground(getDrawable(R.drawable.btn_personalizado));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(convertirDpToPx(5), 0, convertirDpToPx(5), convertirDpToPx(5)); // Agregar margen inferior
            boton.setLayoutParams(params);
            String servicio = nombresBotones[i];
            boton.setOnClickListener(v -> cuadroDialogo(servicio));
            linearLayout.addView(boton);
        }
    }

    private void agregarBotonesPiercing() {
        String[] nombresBotones = {"Piercing \nDermal", "Piercing \nCartílago", "Eliminar \nPiercing"};
        String[] precios = {"18€ - 45€", "18€ - 45€", "30€ - 100€"};
        for (int i = 0; i < nombresBotones.length; i++) {
            Button boton = new Button(this);
            boton.setText(nombresBotones[i] + "\n" + precios[i]);
            boton.setTextColor(Color.WHITE); // Cambiar color de texto
            boton.setBackground(getDrawable(R.drawable.btn_personalizado));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(convertirDpToPx(5), 0, convertirDpToPx(5), convertirDpToPx(5)); // Agregar margen inferior
            boton.setLayoutParams(params);
            String servicio = nombresBotones[i];
            boton.setOnClickListener(v -> cuadroDialogo(servicio));
            linearLayout.addView(boton);
        }
    }

    private void createAppointment(String dia, String hora, String mes, String anio, Date timestamp, String servicio) {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Usuarios").document(userId);
        DocumentReference citasCollectionRef = db.collection("Citas").document();

        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("dia", dia);
        appointmentData.put("hora", hora);
        appointmentData.put("mes", mes);
        appointmentData.put("profesional", profesional);
        appointmentData.put("servicio", servicio);
        appointmentData.put("ubicacion", ubicacion);
        appointmentData.put("nombre", nombre);
        appointmentData.put("timestamp", timestamp);
        appointmentData.put("estado", "Pendiente");

        WriteBatch batch = db.batch();
        batch.set(citasCollectionRef, appointmentData);

        batch.update(userRef, "citas", FieldValue.arrayUnion(citasCollectionRef));

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ServiceScreen.this, "Cita creada con éxito", Toast.LENGTH_SHORT).show();
                    // Iniciar la nueva actividad para mostrar los detalles de la cita
                    Intent intent = new Intent(ServiceScreen.this, AppointmentData.class);
                    intent.putExtra("fechayhora", dia + " " + mes + " " + anio + ", " + hora);
                    startActivity(intent);
                } else {
                    Toast.makeText(ServiceScreen.this, "Error al crear la cita", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int convertirDpToPx(int dp) {
        float densidad = getResources().getDisplayMetrics().density;
        return Math.round(dp * densidad);
    }
}