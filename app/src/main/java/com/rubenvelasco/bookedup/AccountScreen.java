package com.rubenvelasco.bookedup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class AccountScreen extends AppCompatActivity {

    EditText etCorreo;

    TextView tvNombre;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView selectedImageView;

    private static final int IMAGE_SIZE = 310;
    private static final int CORNER_RADIUS = 150;

    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        tvNombre = findViewById(R.id.tvNombre);
        etCorreo = findViewById(R.id.etCorreo2);
        etCorreo.setEnabled(false);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            etCorreo.setText(email);
            consultarNombreUsuario(email);
            cargarImagenDePerfil();
        }
        selectedImageView = findViewById(R.id.imageView8);
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

    private void consultarNombreUsuario(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String username = documentSnapshot.getString("username");
                            if (username.isEmpty()) {
                                // Si el nombre es nulo o está vacío, establece un nombre predeterminado
                                username = "Usuario";
                            } else {
                                // Convertir la primera letra a mayúscula
                                username = username.substring(0, 1).toUpperCase() + username.substring(1);
                            }
                            tvNombre.setText(username);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Manejar errores de consulta
                    }
                });
    }

    public void eliminarCuenta(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Estás seguro de que quieres eliminar tu cuenta?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        eliminarDatosDeUsuarioEnDatabase();
                                    } else {
                                        // Manejar el error al eliminar la cuenta
                                    }
                                }
                            });
                } else {
                    // Manejar el caso en que el usuario actual sea nulo
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Método para eliminar datos del usuario de Firebase Database
    private void eliminarDatosDeUsuarioEnDatabase() {
        String userId = currentUser.getUid();

        // Obtener la referencia al documento del usuario en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Usuarios").document(userId);

        // Eliminar el documento del usuario de Firestore
        userRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                        // Documento de usuario eliminado correctamente en Firestore
                        // Aquí puedes redirigir al usuario a otra pantalla o actualizar la interfaz de usuario
                        Intent intent = new Intent(AccountScreen.this, LoginScreen.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Ha ocurrido un error al eliminar el documento del usuario en Firestore
                        // Aquí puedes mostrar un mensaje de error al usuario
                    }
                });
    }

    public void cerrarSesion(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Estás seguro de que deseas cerrar sesión?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();

                SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();

                // Redirigir al usuario a la pantalla de inicio de sesión
                Intent intent = new Intent(AccountScreen.this, LoginScreen.class);
                startActivity(intent);
                finish(); // Finalizar esta actividad para evitar problemas de navegación
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    public void selectImageFromGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Load the selected image into ImageView using Picasso
                Picasso.get().load(selectedImageUri).resize(IMAGE_SIZE, IMAGE_SIZE).centerCrop().into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        // Create RoundedBitmapDrawable from Bitmap
                        RoundedBitmapDrawable roundedBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundedBitmapDrawable.setCornerRadius(CORNER_RADIUS); // Set corner radius
                        roundedBitmapDrawable.setAntiAlias(true);

                        // Set RoundedBitmapDrawable as ImageView image
                        selectedImageView.setImageDrawable(roundedBitmapDrawable);

                        // Subir la imagen seleccionada a Firebase Storage
                        uploadImageToFirebaseStorage(selectedImageUri);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        // Handle any errors
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // Handle when the image is being loaded
                    }
                });
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Obtén el UID del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Genera un nombre de archivo único para la imagen de perfil
        String profileImagePath = "profile_images/" + userId + ".jpg";

        // Crea una referencia al archivo en Firebase Storage
        StorageReference profileImageRef = storageRef.child(profileImagePath);

        // Sube la imagen al Storage
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // La imagen se ha subido exitosamente
                        // Aquí puedes realizar acciones adicionales si es necesario
                        // Por ejemplo, obtener la URL de descarga de la imagen
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Guardar la URL de descarga de la imagen en Firestore o en tu base de datos
                                // Por ejemplo, actualiza el campo de imagen de perfil del usuario en Firestore
                                updateProfileImage(downloadUri.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Ha ocurrido un error al subir la imagen
                        // Aquí puedes manejar el error de acuerdo a tus necesidades
                    }
                });
    }

    private void updateProfileImage(String imageUrl) {
        // Actualizar el campo de imagen de perfil del usuario en Firestore o en tu base de datos
        // Por ejemplo, puedes obtener la referencia al documento del usuario y actualizar el campo de imagen de perfil
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Usuarios").document(userId);
            userRef.update("profileImageUrl", imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // La URL de la imagen de perfil se ha guardado correctamente en Firestore
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Ha ocurrido un error al guardar la URL de la imagen de perfil en Firestore
                        }
                    });
        }
    }

    private void cargarImagenDePerfil() {
        // Obtén el UID del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Obtén la referencia al documento del usuario en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Usuarios").document(userId);

        // Obtiene la URL de la imagen de perfil del usuario desde Firestore
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Verifica si el usuario tiene una imagen de perfil guardada
                    if (documentSnapshot.contains("profileImageUrl")) {
                        // Obtiene la URL de la imagen de perfil
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        // Carga la imagen de perfil en el ImageView usando Picasso
                        Picasso.get().load(profileImageUrl)
                                .resize(IMAGE_SIZE, IMAGE_SIZE)
                                .centerCrop()
                                .placeholder(R.drawable.perfil) // Agrega el placeholder aquí
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        // Create RoundedBitmapDrawable from Bitmap
                                        RoundedBitmapDrawable roundedBitmapDrawable =
                                                RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                        roundedBitmapDrawable.setCornerRadius(CORNER_RADIUS); // Set corner radius
                                        roundedBitmapDrawable.setAntiAlias(true);

                                        // Set RoundedBitmapDrawable as ImageView image
                                        selectedImageView.setImageDrawable(roundedBitmapDrawable);
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        // Handle any errors
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                        // Handle when the image is being loaded
                                    }
                                });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Manejar errores de obtención de datos de Firestore
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

    public void irHome(View view) {
        Intent cargarHomeScreen = new Intent(this, HomeScreen.class);
        cargarHomeScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cargarHomeScreen);
        finish();
    }

    @Override
    public void onBackPressed() {}
}