package com.example.pm01examen0089;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pm01examen0089.Config.SQLiteConnection;
import com.example.pm01examen0089.Config.Transacciones;
import com.example.pm01examen0089.Models.Paises;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ActivityEditar extends AppCompatActivity {

    SQLiteConnection conexion;
    static final int PETICION_ACCESO_CAMARA = 101;
    static final int PETICION_TOMA_FOTOGRAFIA = 102;
    EditText nombre, telefono, nota;
    Spinner paises;
    ImageButton imagenPerfil, btnAddPais;
    Button guardar, contactos;
    String paisSeleccionado, codigoSeleccionado;
    Bitmap imageBitmap = null;
    byte[] imagenPerfilByteArray;
    ArrayList<Paises> listaPaises;
    ArrayList<String> arregloPaises;
    ArrayAdapter<CharSequence> adaptador;
    int contactoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        Intent intent = getIntent();
        contactoId = intent.getIntExtra("id", 0);
        String nombreIntent = intent.getStringExtra("nombres");
        String paisIntent = intent.getStringExtra("pais");
        String telefonoIntent = intent.getStringExtra("telefono");
        String notaIntent = intent.getStringExtra("nota");
        byte[] imagenIntent = intent.getByteArrayExtra("imagen");

        conexion = new SQLiteConnection(this, Transacciones.namedb, null, 1);
        nombre = findViewById(R.id.txt_actualizarNombre);
        telefono = findViewById(R.id.txt_actualizarTelefono);
        nota = findViewById(R.id.txt_actualizarNota);
        paises = findViewById(R.id.cmb_actualizarPaises);
        imagenPerfil = findViewById(R.id.img_actualizarPerfil);
        btnAddPais = findViewById(R.id.btn_addactualizarAddPais);
        guardar = findViewById(R.id.btn_actualizarContacto);

        obtenerPaises();

        adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arregloPaises);
        paises.setAdapter(adaptador);

        nombre.setText(nombreIntent);
        telefono.setText(telefonoIntent);
        nota.setText(notaIntent);

        paises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                paisSeleccionado = listaPaises.get(i).getPais();
                codigoSeleccionado = listaPaises.get(i).getCodigo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        imagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarPermisos();
            }
        });

        btnAddPais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoAgregarPais();
            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarContacto(contactoId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosContacto(contactoId);
    }

    @SuppressLint("Range")
    private void cargarDatosContacto(int id) {
        try {
            SQLiteDatabase db = conexion.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + Transacciones.tablaContactos + " WHERE " + Transacciones.id + "=?", new String[]{String.valueOf(id)});
            if (cursor.moveToFirst()) {
                nombre.setText(cursor.getString(cursor.getColumnIndex(Transacciones.nombres)));
                telefono.setText(cursor.getString(cursor.getColumnIndex(Transacciones.telefono)));
                nota.setText(cursor.getString(cursor.getColumnIndex(Transacciones.nota)));
                imagenPerfilByteArray = cursor.getBlob(cursor.getColumnIndex(Transacciones.imagen));
                if (imagenPerfilByteArray != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imagenPerfilByteArray, 0, imagenPerfilByteArray.length);
                    imagenPerfil.setImageBitmap(bitmap);
                }
                // Set the spinner to the correct country
                String pais = cursor.getString(cursor.getColumnIndex(Transacciones.pais));
                for (int i = 0; i < listaPaises.size(); i++) {
                    if (listaPaises.get(i).getPais().equals(pais)) {
                        paises.setSelection(i);
                        break;
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseError", "Error al cargar datos: " + e.getMessage());
        }
    }

    private void obtenerPaises() {
        try {
            SQLiteDatabase db = conexion.getReadableDatabase();
            Paises pais = null;
            listaPaises = new ArrayList<>();
            Cursor cursor = db.rawQuery(Transacciones.SelectTablePais, null);

            while (cursor.moveToNext()) {
                pais = new Paises();
                pais.setId(cursor.getInt(0));
                pais.setPais(cursor.getString(1));
                pais.setCodigo(cursor.getString(2));

                listaPaises.add(pais);
            }
            cursor.close();
            llenarCombo();
        } catch (Exception ex) {
            Log.e("DatabaseError", ex.toString());
        }
    }

    private void llenarCombo() {
        arregloPaises = new ArrayList<>();
        for (int i = 0; i < listaPaises.size(); i++) {
            arregloPaises.add(listaPaises.get(i).getPais() + " - " + listaPaises.get(i).getCodigo());
        }
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PETICION_ACCESO_CAMARA);
        } else {
            tomarFoto();
        }
    }

    private void tomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PETICION_TOMA_FOTOGRAFIA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PETICION_ACCESO_CAMARA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            } else {
                Toast.makeText(getApplicationContext(), "¡Permiso Denegado!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PETICION_TOMA_FOTOGRAFIA && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                imagenPerfil.setImageBitmap(imageBitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imagenPerfilByteArray = stream.toByteArray();

            } catch (Exception ex) {
                Log.e("CameraError", ex.toString());
            }
        }
    }

    public void mostrarDialogoAgregarPais() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar País");

        View dialogView = getLayoutInflater().inflate(R.layout.add_pais_dialog, null);
        builder.setView(dialogView);

        final EditText paisEditText = dialogView.findViewById(R.id.editPais);
        final EditText codigoAreaEditText = dialogView.findViewById(R.id.editAreaCode);

        builder.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pais = paisEditText.getText().toString();
                String codigo = "(" + codigoAreaEditText.getText().toString() + ")";

                agregarPais(pais, codigo);
                actualizarSpinner();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void agregarPais(String pais, String codigo) {
        try {
            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put(Transacciones.pais, pais);
            valores.put(Transacciones.codigoArea, codigo);

            long resultado = db.insert(Transacciones.tablaPaises, null, valores);

            if (resultado != -1) {
                Log.d("DatabaseSuccess", "País agregado correctamente con ID: " + resultado);
                Toast.makeText(this, "País agregado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("DatabaseError", "Error al agregar país");
                Toast.makeText(this, "Error al agregar país", Toast.LENGTH_SHORT).show();
            }

            db.close();
        } catch (Exception e) {
            Log.e("DatabaseError", "Error al agregar país: " + e.getMessage());
            Toast.makeText(this, "Error al agregar país", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarSpinner() {
        paises.setAdapter(null);
        obtenerPaises();
        adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arregloPaises);
        paises.setAdapter(adaptador);
    }

    private void actualizarContacto(int id) {
        try {
            SQLiteDatabase db = conexion.getWritableDatabase();
            ContentValues valores = new ContentValues();

            valores.put(Transacciones.nombres, nombre.getText().toString());
            valores.put(Transacciones.telefono, telefono.getText().toString());
            valores.put(Transacciones.nota, nota.getText().toString());
            valores.put(Transacciones.imagen, imagenPerfilByteArray);

            int resultado = db.update(Transacciones.tablaContactos, valores, Transacciones.id + "=?", new String[]{String.valueOf(id)});

            if (resultado != -1) {
                Log.d("DatabaseSuccess", "Contacto actualizado correctamente");
                Toast.makeText(this, "Contacto actualizado correctamente", Toast.LENGTH_SHORT).show();
                db.close();
                finish(); // Cerrar esta actividad y volver a la actividad anterior
            } else {
                Log.e("DatabaseError", "Error al actualizar contacto");
                Toast.makeText(this, "Error al actualizar contacto", Toast.LENGTH_SHORT).show();
                db.close();
            }

        } catch (Exception e) {
            Log.e("DatabaseError", "Error al actualizar contacto: " + e.getMessage());
            Toast.makeText(this, "Error al actualizar contacto", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(ActivityEditar.this, ActivityContactos.class);
        startActivity(intent);
    }

}
