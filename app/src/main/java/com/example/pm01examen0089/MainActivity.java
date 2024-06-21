package com.example.pm01examen0089;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pm01examen0089.Config.SQLiteConnection;
import com.example.pm01examen0089.Config.Transacciones;
import com.example.pm01examen0089.Models.Paises;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    SQLiteConnection conexion;
    static final int peticion_acceso_camera = 101;
    static final int peticion_toma_fotografia = 102;
    String currentPhotoPath;
    EditText etNombre, etTelefono, etNota;
    Spinner spPaises;
    ImageButton ibImagenPerfil, ibAddPais;
    Button btnGuardar, btnContactos;
    String selectedPais, selectedCodigo;
    Bitmap bitmapImagen = null;
    byte[] byteArrayImagenPerfil;
    ArrayList<Paises> listadoPaises;
    ArrayList<String> arrayPaises;
    ArrayAdapter<CharSequence> adp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conexion = new SQLiteConnection(this, Transacciones.namedb, null, 1);
        etNombre = findViewById(R.id.txt_nombre);
        etTelefono = findViewById(R.id.txt_telefono);
        etNota = findViewById(R.id.txt_nota);
        spPaises = findViewById(R.id.cmb_paises);
        ibImagenPerfil = findViewById(R.id.img_perfil);
        ibAddPais = findViewById(R.id.btn_addPais);
        btnGuardar = findViewById(R.id.btn_Guardar);
        btnContactos = findViewById(R.id.btn_Contactos);

        getPaises();

        adp = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayPaises);
        spPaises.setAdapter(adp);

        spPaises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPais = listadoPaises.get(i).getPais();
                selectedCodigo = listadoPaises.get(i).getCodigo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ibImagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });

        View.OnClickListener buttonClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class<?> actividad = null;
                if (view.getId() == R.id.btn_Contactos) {
                    actividad = ActivityContactos.class;
                }
                if (actividad != null) {
                    moveActivity(actividad);
                }
            }
        };

        btnContactos.setOnClickListener(buttonClick);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etNombre.getText().toString().trim().isEmpty() || etTelefono.getText().toString().trim().isEmpty() || etNota.getText().toString().trim().isEmpty()) {
                    etNombre.setError("Debe escribir un nombre");
                    etTelefono.setError("Debe escribir un telefono");
                    etNota.setError("Debe escribir una nota");
                } else if (bitmapImagen == null) {
                    Toast.makeText(getApplicationContext(), "Tomar foto del contacto!", Toast.LENGTH_LONG).show();
                } else if (spPaises.getSelectedItemPosition() == 0) {
                    Toast.makeText(getApplicationContext(), "Seleccionar un pais de la lista!", Toast.LENGTH_LONG).show();
                } else {
                    addContact();
                }
            }
        });
    }

    private void addContact() {
        try {

            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put(Transacciones.nombres, etNombre.getText().toString());
            valores.put(Transacciones.pais, selectedPais);
            valores.put(Transacciones.codigo, selectedCodigo);
            valores.put(Transacciones.telefono, etTelefono.getText().toString());
            valores.put(Transacciones.nota, etNota.getText().toString());
            valores.put(Transacciones.imagen, byteArrayImagenPerfil);

            Long result = db.insert(Transacciones.tablaContactos, Transacciones.id, valores);

            Toast.makeText(this, getString(R.string.respuesta), Toast.LENGTH_SHORT).show();
            db.close();
            recreate();
            etNombre.setText("");
            etTelefono.setText("");
            etNota.setText("");
            spPaises.setSelection(0);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.errorIngreso), Toast.LENGTH_SHORT).show();
        }
    }

    private void getPaises() {
        try {
            SQLiteDatabase db = conexion.getReadableDatabase();
            Paises pais = null;
            listadoPaises = new ArrayList<>();
            db.rawQuery(Transacciones.SelectTablePais, null);

            Cursor cursor = db.rawQuery(Transacciones.SelectTablePais, null);
            while (cursor.moveToNext()) {
                pais = new Paises();
                pais.setId(cursor.getInt(0));
                pais.setPais(cursor.getString(1));
                pais.setCodigo(cursor.getString(2));

                listadoPaises.add(pais);
            }
            cursor.close();
            fillCombo();
        } catch (Exception ex) {
            ex.toString();
        }
    }

    private void fillCombo() {
        arrayPaises = new ArrayList<>();
        for (int i = 0; i < listadoPaises.size(); i++) {
            arrayPaises.add(listadoPaises.get(i).getId() + " - " +
                    listadoPaises.get(i).getPais() + " - " +
                    listadoPaises.get(i).getCodigo());
        }
    }

    private void moveActivity(Class<?> actividad) {
        Intent intent = new Intent(getApplicationContext(), actividad);
        startActivity(intent);
    }

    private void permisos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, peticion_acceso_camera);
        } else {
            tomarFoto();
        }
    }

    private void tomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, peticion_toma_fotografia);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == peticion_acceso_camera) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            } else {
                Toast.makeText(getApplicationContext(), "¡Acceso Denegado!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == peticion_toma_fotografia && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                bitmapImagen = (Bitmap) extras.get("data");
                ibImagenPerfil.setImageBitmap(bitmapImagen);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapImagen.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArrayImagenPerfil = stream.toByteArray();

            } catch (Exception ex) {
                ex.toString();
            }
        }
    }

    public void showAddPaisDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Pais");

        View dialogView = getLayoutInflater().inflate(R.layout.add_pais_dialog, null);
        builder.setView(dialogView);

        final EditText paisEditText = dialogView.findViewById(R.id.editPais);
        final EditText areaCodeEditText = dialogView.findViewById(R.id.editAreaCode);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pais = paisEditText.getText().toString();
                String areaCode = "(" + areaCodeEditText.getText().toString() + ")";

                addPais(pais, areaCode);
                updateSpinner();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void addPais(String pais, String codigo) {
        try {
            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put(Transacciones.pais, pais);
            valores.put(Transacciones.codigoArea, codigo);

            long result = db.insert(Transacciones.tablaPaises, null, valores);

            if (result != -1) {
                Log.d("DatabaseSuccess", "Inserted data with row ID: " + result);
                Toast.makeText(this, getString(R.string.respuesta), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("DatabaseError", "Error inserting data");
                Toast.makeText(this, getString(R.string.errorIngreso), Toast.LENGTH_SHORT).show();
            }

            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DatabaseError", "Error inserting data: " + e.getMessage());
            Toast.makeText(this, getString(R.string.errorIngreso), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSpinner() {
        // Step 1: Retrieve the updated data from the database
        spPaises.setAdapter(null);
        getPaises();
        adp = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayPaises);
        spPaises.setAdapter(adp);
    }
}
