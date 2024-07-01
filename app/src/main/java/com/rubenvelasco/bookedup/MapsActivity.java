package com.rubenvelasco.bookedup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient ubicacionUsuario;
    private int permisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtener el resultado de permiso enviado desde SearchScreen
        Bundle informacion = getIntent().getExtras();
        if (informacion != null) permisos = informacion.getInt("PERMISSION_RESULT", 0);

        db = FirebaseFirestore.getInstance();
        ubicacionUsuario = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        cargarUbicaciones();
    }

    private void cargarUbicaciones() {
        db.collection("Servicios")
                .orderBy("ubicacion")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<LatLng> ubicaciones = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombre = document.getString("nombre");
                            String descripcion = document.getString("descripcion");
                            String ubicacion = document.getString("ubicacion");

                            if (nombre != null && descripcion != null && ubicacion != null) {
                                LatLng location = LocationHelper.getLocationFromAddress(this, ubicacion);
                                if (location != null) {
                                    MarkerOptions markerOptions = new MarkerOptions().position(location).title(nombre);
                                    googleMap.addMarker(markerOptions).setTag(descripcion);
                                    ubicaciones.add(location);
                                }
                            }
                        }

                        if (!ubicaciones.isEmpty()) {
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (LatLng ubicacion : ubicaciones) {
                                builder.include(ubicacion);
                            }
                            LatLngBounds bounds = builder.build();
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        } else {
                            Toast.makeText(this, "No hay ubicaciones válidas para mostrar", Toast.LENGTH_SHORT).show();
                        }

                        if (permisos == 1) {
                            obtenerUbicacionActual();
                        }

                        // Configurar listener para los marcadores
                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                String title = marker.getTitle();
                                String descripcion = (String) marker.getTag();
                                if (descripcion != null) {
                                    mostrarInformacionMarcador(title, descripcion);
                                    abrirGoogleMaps(marker.getPosition(), title);
                                }
                                return false;
                            }
                        });

                    } else {
                        Toast.makeText(this, "Error al cargar ubicaciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarInformacionMarcador(String nombre, String descripcion) {
        // Mostrar información del marcador (nombre y descripción)
        Toast.makeText(this, nombre, Toast.LENGTH_LONG).show();
    }

    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            ubicacionUsuario.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Ubicación Actual")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                    } else {
                        Toast.makeText(MapsActivity.this, "Active la ubicación", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para abrir Google Maps con indicaciones
    private void abrirGoogleMaps(LatLng destino, String nombreDestino) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No se han concedido los permisos de ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        ubicacionUsuario.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng origen = new LatLng(location.getLatitude(), location.getLongitude());
                    String uri = "https://www.google.com/maps/dir/?api=1&origin=" + origen.latitude + "," + origen.longitude +
                            "&destination=" + destino.latitude + "," + destino.longitude + "&travelmode=driving";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                } else {
                    Toast.makeText(MapsActivity.this, "Active la ubicación", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Manejar el resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
