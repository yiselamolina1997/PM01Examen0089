package com.example.pm01examen0089;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pm01examen0089.Config.SQLiteConnection;
import com.example.pm01examen0089.Config.Transacciones;
import com.example.pm01examen0089.Models.Contactos;

import java.util.ArrayList;

public class ActivityContactos extends AppCompatActivity {

    SQLiteConnection conexion;
    private GestureDetector gestureDetector;
    Button btnCompartir, btnEliminar, btnActualizar, btnVerImagen;
    ListView listContactos;
    ArrayList<Contactos> listaContactos;
    ArrayList<String> arrayContactos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);

        // Initialize the GestureDetector
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                handleDoubleTap(e);
                return true;
            }
        });

        try {
            //Establecer conexión a base de datos
            conexion = new SQLiteConnection(this, Transacciones.namedb, null, 1);
            btnCompartir = findViewById(R.id.btn_compartir);
            btnEliminar = findViewById(R.id.btn_eliminar);
            btnActualizar = findViewById(R.id.btn_actualizar);
            btnVerImagen = findViewById(R.id.btn_verImagen);
            listContactos = findViewById(R.id.list_contactos);

            getContacts();
            ArrayAdapter adp = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayContactos);
            listContactos.setAdapter(adp);

            listContactos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    btnVerImagen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showContactImagePopup(listaContactos.get(i).getImagen());
                        }
                    });
                    btnCompartir.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String itemPerson = listaContactos.get(i).getNombre() + " | " + listaContactos.get(i).getCodigo() + " " + listaContactos.get(i).getTelefono();
                            shareContact(itemPerson);
                        }
                    });
                    btnEliminar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteContact(listaContactos.get(i).getId());
                        }
                    });
                    btnActualizar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ActivityContactos.this, ActivityEditar.class);
                            Integer id = listaContactos.get(i).getId();
                            String nombre = listaContactos.get(i).getNombre();
                            String telefono = listaContactos.get(i).getTelefono();
                            String nota = listaContactos.get(i).getNota();
                            intent.putExtra("id", id);
                            intent.putExtra("nombres", nombre);
                            intent.putExtra("pais", listaContactos.get(i).getPais());
                            intent.putExtra("telefono", telefono);
                            intent.putExtra("nota", nota);
                            intent.putExtra("imagen", listaContactos.get(i).getImagen());
                            startActivity(intent);
                        }
                    });
                }
            });
            listContactos.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
            listContactos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showCallConfirmationDialog(listaContactos.get(i).getTelefono(), listaContactos.get(i).getNombre());
                    return true;
                }
            });
        } catch (Exception ex) {
            ex.toString();
        }
    }

    private void getContacts() {
        SQLiteDatabase db = conexion.getReadableDatabase();
        Contactos contacto = null;
        listaContactos = new ArrayList<>();

        Cursor cursor = db.rawQuery(Transacciones.SelectTablePersonas, null);
        while (cursor.moveToNext()) {
            contacto = new Contactos();
            contacto.setId(cursor.getInt(0));
            contacto.setNombre(cursor.getString(1));
            contacto.setPais(cursor.getString(2));
            contacto.setCodigo(cursor.getString(3));
            contacto.setTelefono(cursor.getString(4));
            contacto.setNota(cursor.getString(5));
            contacto.setImagen(cursor.getBlob(6));

            listaContactos.add(contacto);
        }
        cursor.close();
        fillList();
    }

    private void fillList() {
        arrayContactos = new ArrayList<>();
        for (int i = 0; i < listaContactos.size(); i++) {
            arrayContactos.add(listaContactos.get(i).getNombre() + " | " + listaContactos.get(i).getCodigo() + " " + listaContactos.get(i).getTelefono());
        }
    }

    public void showContactImagePopup(byte[] imageBlob) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.contact_image_popup);

        ImageView imageView = dialog.findViewById(R.id.contactImageView);

        Bitmap contactImage = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
        imageView.setImageBitmap(contactImage);

        dialog.show();
    }

    public void closePopup(View view) {
        recreate();
    }

    private void shareContact(String contactInfo) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, contactInfo);
        shareIntent.setType("text/plain");

        Intent chooser = Intent.createChooser(shareIntent, "Compartir");

        startActivity(chooser);
    }

    private void deleteContact(int contactId) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar");
        builder.setMessage("¿Desea eliminar el contacto?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SQLiteDatabase db = conexion.getWritableDatabase();
                String whereClause = "id = ?";
                String[] whereArgs = {String.valueOf(contactId)};
                int deletedRows = db.delete(Transacciones.tablaContactos, whereClause, whereArgs);

                if (deletedRows > 0) {
                    recreate();
                } else {
                    Toast.makeText(getApplicationContext(), "¡No pudo eliminar el contacto!", Toast.LENGTH_LONG).show();
                }
                db.close();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recreate();
            }
        });

        builder.create().show();
    }

    private void handleDoubleTap(MotionEvent e) {
        // You can get the position of the tapped item from the MotionEvent
        int position = listContactos.pointToPosition((int) e.getX(), (int) e.getY());

        // Check if a valid item was tapped
        if (position != AdapterView.INVALID_POSITION) {
            showCallConfirmationDialog(listaContactos.get(position).getTelefono(), listaContactos.get(position).getNombre());
        }
    }

    private void showCallConfirmationDialog(final String phoneNumber, final String nombre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Llamar a : " + nombre);
        builder.setMessage("¿Desea llamar a este contacto?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User confirmed, initiate the phone call
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }
}
