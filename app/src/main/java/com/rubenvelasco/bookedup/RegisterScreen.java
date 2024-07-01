package com.rubenvelasco.bookedup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterScreen extends AppCompatActivity {
    private FirebaseFirestore firestore;

    private EditText etUsuario, etEmail, etPass, etPhone;
    private ImageView ivShowPassword;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // Inicializar firestore

        etUsuario = findViewById(R.id.etUsuario);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        etPhone = findViewById(R.id.etPhone);
        ivShowPassword = findViewById(R.id.ivShowPassword);

        ivShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivShowPassword.setImageResource(R.drawable.ic_eye); // Cambia este recurso a tu ícono de ojo
                } else {
                    etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivShowPassword.setImageResource(R.drawable.ic_eye_closed); // Cambia este recurso a tu ícono de ojo cerrado
                }
                isPasswordVisible = !isPasswordVisible;
                etPass.setSelection(etPass.getText().length());
            }
        });

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

    public void registrar(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString().trim();
        String username = etUsuario.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (TextUtils.isEmpty(username) || password.length() < 5) {
                Toast.makeText(this, "Usuario: debe tener al menos 5 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Por favor, ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                Toast.makeText(this, "Contraseña: debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(phone) || !Patterns.PHONE.matcher(phone).matches()) {
                Toast.makeText(this, "Por favor, ingrese un número de teléfono válido", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            if (currentUser != null) {
                                String userId = currentUser.getUid();

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference userRef = db.collection("Usuarios").document(userId);

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("email", email);
                                userData.put("password", password);
                                userData.put("phone", phone);

                                userRef.set(userData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(RegisterScreen.this, "Cuenta creada.", Toast.LENGTH_SHORT).show();
                                                Log.d("ID", "Id Usuario: " + userId);
                                                irIniciarSesion(view);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error añadiendo documento", e);
                                            }
                                        });
                            }
                        } else {
                            Log.e(TAG, "Error de registro: " + task.getException().getMessage());
                            Toast.makeText(RegisterScreen.this, "Error de registro.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void irIniciarSesion(View view) {
        Intent cargarLogin = new Intent(this, LoginScreen.class);
        cargarLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarLogin);
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}
