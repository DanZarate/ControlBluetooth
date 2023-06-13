package com.example.controlbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import it.beppi.knoblibrary.Knob;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    // Obtén una instancia de la base de datos
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    // Obtén una referencia a un nodo en la base de datos
     DatabaseReference myRef = database.getReference("Knob");
    // Obtén una referencia al nodo específico
    DatabaseReference myRef2 = database.getReference("MQ");
    DatabaseReference myRef3 = database.getReference("nombre_del_nodo");


    String miVariable = "Posicion, 0";
    String miVariable2 = "0";
    String valorLeido = "0";
    String valorLeido2 = "0";

    String valorLeido3 = "0";

    String valorMensaje = "";

    private boolean isNotificationCreated = false; // Variable para controlar el estado de la notificación


    public int contador = 0;
    //1)
    Button IdDesconectar, IdTema, IdCD, IdMinimo, IdMaximo, IdConexion;
    TextView IdBufferIn, Posicion, idDispositivo;

    ConstraintLayout IdFondo;

    Knob knob;

    String readMessage;
    String dataInPrint;

    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    //-------------------------------------------
    private static final String TAG = "MY_APP_DEBUG_TAG";

    //Notificacion
    private PendingIntent pendingIntent;
    private final static  String CHANNEL_ID = "NOTIFICACION";
    private final static int NOTIFICACION_ID = 0;//Diferente para cada notificacion

    SharedPreferences sharedPreferences;
    String value;
    String nombre;
    int activar;

    Boolean modif=false;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        idDispositivo = (TextView) findViewById(R.id.idConectado);
        IdBufferIn = (TextView) findViewById(R.id.idBufferIn);
        IdConexion = (Button) findViewById(R.id.idConexion);



        // Obtén el objeto SharedPreferences
        sharedPreferences = getSharedPreferences("MiArchivoPref", Context.MODE_PRIVATE);
        value = sharedPreferences.getString("DireccionBT", "");
        nombre = sharedPreferences.getString("NombreBT", "");
        activar = sharedPreferences.getInt("ActivarBt",0);

        String[] parts = nombre.split("\n");
        String resultado = parts[0];



        Drawable icon = getResources().getDrawable(R.drawable.baseline_bluetooth_audio_24);
        Drawable icon2 = getResources().getDrawable(R.drawable.ic_bluetooth2_disabled_black_24dp);
        if(activar==1) {
            idDispositivo.setText("Conectado a: "+resultado);
            IdConexion.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }
        else if(activar==2){
            idDispositivo.setText("Conectado a la Red");
            IdConexion.setCompoundDrawablesWithIntrinsicBounds(icon2, null, null, null);
        }
        //myRef.setValue(miVariable);
        //myRef2.setValue(miVariable2);



        IdFondo = findViewById(R.id.IdFondo);

        //2)
        //Enlaza los controles con sus respectivas vistas
        IdDesconectar = (Button) findViewById(R.id.IdDesconectar);
        Posicion = (TextView) findViewById(R.id.Posicion);

        IdTema = (Button) findViewById(R.id.IdTema);
        IdMinimo = (Button) findViewById(R.id.idMinimo);
        IdMaximo = (Button) findViewById(R.id.idMaximo);
        IdCD = (Button) findViewById(R.id.IdCD);

        knob = (Knob) findViewById(R.id.knob);//Knob
        //knob.setMinAngle(0); // Establece el ángulo mínimo de rotación en 270 grados
        //knob.setMaxAngle(270); // Establece el ángulo máximo de rotación en 0 grados

        // Agrega un listener para leer el valor de la variable en la base de datos
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Obtén el valor específico
                valorLeido = dataSnapshot.getValue(String.class);
                valorLeido = valorLeido.substring(valorLeido.indexOf(",") + 1);
                // Haz algo con el valor leído
                System.out.println("Valor leído: " + valorLeido);
                if(activar!=1 && modif == false) {
                    //knob.setState(39-Integer.parseInt(valorLeido));//Knob
                    knob.setState(Integer.parseInt(valorLeido));//Knob
                    modif = true;
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Maneja el error
            }
        });


        // Agrega un listener para leer el valor de la variable en la base de datos
        myRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Obtén el valor específico
                valorLeido3 = dataSnapshot.getValue(String.class);
                valorLeido3 = valorLeido3.substring(valorLeido3.indexOf(",") + 1);
                // Haz algo con el valor leído
                System.out.println("Valor leído3: " + valorLeido3);


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Maneja el error
            }
        });

        // Agrega un listener para leer el valor de la variable en la base de datos

        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Obtén el valor específico
                valorLeido2 = dataSnapshot.getValue(String.class);
               //valorLeido2 = valorLeido2.substring(valorLeido2.indexOf(",") + 1);
                // Haz algo con el valor leído
                System.out.println("Valor leído2: " + valorLeido2);
                if(Integer.parseInt(valorLeido2) == 1)
                {

                    String nuevoValor = "Posicion," + String.valueOf(0);
                    myRef.setValue(nuevoValor);
                    Posicion.setText("FUGA!");
                    IdBufferIn.setText("Fuga de Gas Detectada");
                    Posicion.setTextColor(Color.parseColor("#AC1515"));
                    Posicion.invalidate();
                    //knob.setState(39);//No hacer
                    knob.setEnabled(false);
                    knob.setState(39);
                    createNotification();
                }
                else {
                    Posicion.setText("");
                    IdBufferIn.setText("Esperando Ordenes");
                    knob.setEnabled(true);
                    isNotificationCreated = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Maneja el error
            }
        });

        bluetoothIn = new  Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    DataStringIN.append(readMessage);
                    int endOfLineIndex = DataStringIN.indexOf("#");//#

                    if (endOfLineIndex > 0) {
                        dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        IdBufferIn.setText(dataInPrint);//<-<- PARTE A MODIFICAR >->->
                        DataStringIN.delete(0, DataStringIN.length());
                        createNotification();
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();

        knob.setOnStateChanged(new Knob.OnStateChanged(){
            @SuppressLint("ResourceAsColor")
            @Override
            public void onState(int state){
                if(Integer.parseInt(valorLeido2) == 1)
                {
                    String nuevoValor = "Posicion," + String.valueOf(39);
                    myRef.setValue(nuevoValor);
                    //knob.setState(39);//No hacer
                }
                else {
                    //hacer algo
                    // Editar el valor de la variable en la base de datos
                    //String nuevoValor = "Posicion," + String.valueOf(39 - state);
                    String nuevoValor = "Posicion," + String.valueOf(state);
                    myRef.setValue(nuevoValor);

                    activar = sharedPreferences.getInt("ActivarBt",0);
                    if(activar==1) { //Solo si esta en modo bt
                        //MyConexionBT.write(String.valueOf(39-state));
                        MyConexionBT.write(String.valueOf(state));
                        MyConexionBT.write("\n");
                    }
                    if (state == 39) {
                        Posicion.setText("CLOSED");
                        Posicion.setTextColor(Color.parseColor("#AC1515"));
                        Posicion.invalidate();
                    } else if (state == 0) {
                        Posicion.setText("MIN");
                        Posicion.setTextColor(Color.parseColor("#039BE5"));
                        Posicion.invalidate();
                    } else if (state == 13) {
                        Posicion.setText("MAX");
                        Posicion.setTextColor(Color.parseColor("#F4511E"));
                        Posicion.invalidate();
                    } else {
                        Posicion.setText("ON");
                        Posicion.setTextColor(Color.parseColor("#1DDC0F"));
                        Posicion.invalidate();
                    }
                }

            }
        });

        // Configuracion onClick listeners para los botones
        // para indicar que se realizara cuando se detecte
        // el evento de Click

         IdConexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                activar = sharedPreferences.getInt("ActivarBt",0);
               if(activar==1) {
                   IdConexion.setCompoundDrawablesWithIntrinsicBounds(icon2, null, null, null);
                   idDispositivo.setText("Conectado a la Red");
                   SharedPreferences.Editor editor = sharedPreferences.edit();
                   editor.putInt("ActivarBt", 2);
                   // Puedes utilizar otros métodos como putFloat(), putLong(), etc., según tus necesidades.
                   editor.apply();
                   //activar = false;
               }
                else if(activar==2){
                   IdConexion.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                   idDispositivo.setText("Conectado a: "+resultado);
                   SharedPreferences.Editor editor = sharedPreferences.edit();
                   editor.putInt("ActivarBt", 1);
                   // Puedes utilizar otros métodos como putFloat(), putLong(), etc., según tus necesidades.
                   editor.apply();
                   //activar = true;
                }
            }
        });

        IdCD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MyConexionBT.write("c");
                if(Integer.parseInt(valorLeido2) != 1) {
                    knob.setState(39);//Knob
                    Posicion.setText("CLOSED");
                }
                //Posicion.setTextColor(0xAC1515);
                //Posicion.invalidate();

            }
        });

        IdMinimo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MyConexionBT.write("i");
                if(Integer.parseInt(valorLeido2) != 1) {
                    knob.setState(0);//Knob
                    Posicion.setText("MIN");
                }
                //Posicion.setTextColor(0x039BE5);
                //Posicion.invalidate();
            }
        });

        IdMaximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MyConexionBT.write("m");
                if(Integer.parseInt(valorLeido2) != 1) {
                    knob.setState(13);//Knob
                    Posicion.setText("MAX");
                }
                //Posicion.setTextColor(0xF4511E);
                //Posicion.invalidate();
            }
        });

        IdTema.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(contador == 0)
                {
                    //IdCD.setBackgroundResource(R.drawable.bk_boton_plano);
                    //IdTema.setBackgroundResource(R.drawable.bk_boton_plano);
                    IdDesconectar.setBackgroundResource(R.drawable.bk_boton_plano);
                    IdFondo.setBackgroundResource(R.drawable.cocina);
                    contador ++;
                }
                else
                {
                    IdFondo.setBackgroundResource(R.drawable.cocina2);
                    //IdCD.setBackgroundResource(R.drawable.boton_deshabilitado);
                    IdDesconectar.setBackgroundResource(R.drawable.boton_deshabilitado);
                    contador --;
                }
            }
        });

        IdDesconectar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("DireccionBT", null);
                editor.putInt("ActivarBt", 0);
                // Puedes utilizar otros métodos como putFloat(), putLong(), etc., según tus necesidades.
                editor.apply();

                //address=null;

                if (btSocket!=null)
                {
                    try {btSocket.close();}
                    catch (IOException e)
                    { Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();;}
                }
                finish();
            }
        });



    }

    //Clase evento de notificación
    private void createNotification() {
        if (isNotificationCreated) {
            return; // La notificación ya existe, no es necesario crearla nuevamente
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            builder.setSmallIcon(R.drawable.ic_baseline_notification_important_24);
            builder.setContentTitle("ALERTA! FUGA DE GAS");
            builder.setContentText(dataInPrint);
            builder.setColor(Color.BLUE);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setLights(Color.MAGENTA, 1000, 1000);

            // Mostrar la notificación
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICACION_ID, builder.build());
            isNotificationCreated = true;
        }


    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Consigue la direccion MAC desde DeviceListActivity via intent
        Intent intent = getIntent();
        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG,"Error al crear el flujo de Entrada",e);
            }
            try
            {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG,"Error al crear el flujo de Salida",e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];//Numero de bits 1024
            int bytes;//Numero de bytes devueltos por read()

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    //Lectura del InputStream.
                    bytes = mmInStream.read(buffer);

                    //Cambiado
                    readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();


                    /*Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, bytes, -1,
                    buffer);*/
                } catch (IOException e) {
                    // Log.d(TAG, "La transmición de entrada se desconecto",e);
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }



}
