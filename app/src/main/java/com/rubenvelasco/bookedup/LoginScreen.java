package com.rubenvelasco.bookedup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private ImageView ivShowPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private boolean isPasswordVisible = false;

    private FirebaseFirestore db;
    private static final int RC_SIGN_IN = 123;
    TextView tvRegistrate;

    private CheckBox recuerdame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        mAuth = FirebaseAuth.getInstance();
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        ivShowPassword = findViewById(R.id.ivShowPassword);
        tvRegistrate = findViewById(R.id.tvRegistrate);
        recuerdame = findViewById(R.id.recuerdame);

        firestore = FirebaseFirestore.getInstance();
        db = FirebaseFirestore.getInstance();

        ivShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    etContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivShowPassword.setImageResource(R.drawable.ic_eye); // Cambia este recurso a tu ícono de ojo
                } else {
                    etContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivShowPassword.setImageResource(R.drawable.ic_eye_closed); // Cambia este recurso a tu ícono de ojo cerrado
                }
                isPasswordVisible = !isPasswordVisible;
                etContrasena.setSelection(etContrasena.getText().length());
            }
        });

        // Verifica si el usuario ya ha marcado "Recuérdame" anteriormente
        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean isRemembered = preferences.getBoolean("isRemembered", false);
        if (isRemembered) {
            Intent cargarHome = new Intent(LoginScreen.this, HomeScreen.class);
            startActivity(cargarHome);
            finish();
        }

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

    public void iniciarSesion(View view) {

        String email = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Introduce Correo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Introduce Contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(LoginScreen.this, "Inicio de sesión completado.", Toast.LENGTH_SHORT).show();

                            // Obtén la instancia del usuario actual
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            if (currentUser != null) {
                                String userId = currentUser.getUid();

                                // Accede al documento del usuario en la colección "Usuarios"
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference usuarioRef = db.collection("Usuarios").document(userId);

                                // Verifica si el documento del usuario existe
                                usuarioRef.get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {

                                                        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.putBoolean("isRemembered", recuerdame.isChecked());
                                                        editor.apply();

                                                        // El usuario está en la colección "Usuarios"
                                                        Log.d("ID", "Id Usuario: " + userId);

                                                        String username = document.getString("username");
                                                        Toast.makeText(LoginScreen.this, "Inicio de sesión exitoso: " + username, Toast.LENGTH_SHORT).show();
                                                        // Puedes realizar acciones adicionales aquí si es necesario

                                                        Intent cargarHome = new Intent(LoginScreen.this, HomeScreen.class);
                                                        startActivity(cargarHome);
                                                        finish();

                                                    } else {
                                                        // El usuario no está en la colección "Usuarios"
                                                        Log.d("ID", "Usuario no encontrado en la colección");
                                                        Toast.makeText(LoginScreen.this, "Error al iniciar sesión.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Log.d("ID", "Error al acceder al documento del usuario: " + task.getException());
                                                }
                                            }
                                        });

                                usuarioRef.update("password", password)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Campo de última contraseña actualizado correctamente");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error al actualizar el campo de última contraseña", e);
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(LoginScreen.this, "Error de inicio de sesión.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void continuarConGoogle(View view) {
        FirebaseAuth.getInstance().signOut(); // Cerrar sesión antes de iniciar un nuevo flujo

        // Inicia el flujo de inicio de sesión con Google
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Inicio de sesión exitoso
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Toast.makeText(this, "Inicio de sesión exitoso: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                    // Guardar datos en Firestore si es necesario
                    guardarDatosEnFirestore(user);

                    // Guardar el estado de la casilla "Recuérdame"
                    SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isRemembered", recuerdame.isChecked());
                    editor.apply();

                    startActivity(new Intent(this, HomeScreen.class));
                    finish();
                }
            } else {
                // Inicio de sesión fallido
                Toast.makeText(this, "Inicio de sesión fallido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarDatosEnFirestore(FirebaseUser user) {
        String userId = user.getUid();
        DocumentReference usuarioRef = firestore.collection("Usuarios").document(userId);

        // Consultar si el documento del usuario ya existe en Firestore
        usuarioRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // El usuario ya está registrado en Firestore, no se actualizan los datos
                    Log.i("Datos", "El usuario ya existe en Firestore");
                } else {
                    // Guardar los datos del usuario en Firestore
                    Map<String, Object> usuarioMap = new HashMap<>();
                    usuarioMap.put("username", user.getDisplayName());
                    usuarioMap.put("email", user.getEmail());

                    firestore.collection("Usuarios").document(userId).set(usuarioMap)
                            .addOnSuccessListener(aVoid -> Log.i("Datos", "Datos guardados en Firestore"))
                            .addOnFailureListener(e -> Log.i("Datos", "Error al guardar datos en Firestore"));
                }
            } else {
                Log.i("Datos", "Error al verificar la existencia del usuario en Firestore", task.getException());
            }
        });
    }

    public void irRegistrarse(View view) {
        Intent cargarRegister = new Intent(this, RegisterScreen.class);
        cargarRegister.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarRegister);
        finish();
    }

    public void reestablecerContrasenia(View view) {

        String email = etCorreo.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Introduce un correo para reestablecer tu contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginScreen.this, "Se ha enviado un correo para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginScreen.this, "Error al enviar el correo para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
    }
}
