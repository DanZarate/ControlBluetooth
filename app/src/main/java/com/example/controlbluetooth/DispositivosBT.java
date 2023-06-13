package com.example.controlbluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DispositivosBT extends AppCompatActivity {

    //1)
    // Depuración de LOGCAT
    private static final String TAG = "DispositivosBT"; //<-<- PARTE A MODIFICAR >->->
    // Declaracion de ListView
    ListView IdLista;
    // String que se enviara a la actividad principal, mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Declaracion de campos
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    String value = "";
    String nombre = "";
    int activar = 0;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_bt);

        // Obtén el objeto SharedPreferences
        sharedPreferences = getSharedPreferences("MiArchivoPref", Context.MODE_PRIVATE);
        value = sharedPreferences.getString("DireccionBT", "");
        nombre = sharedPreferences.getString("NombreBT", "");
        activar = sharedPreferences.getInt("ActivarBt",0);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        //---------------------------------
        VerificarEstadoBT();

        // Recupera el valor de SharedPreferences
       //
       if (value.equals("")|| activar==0){


           // Inicializa la array que contendra la lista de los dispositivos bluetooth vinculados
           mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.nombre_dispositivos);//<-<- PARTE A MODIFICAR >->->
           // Presenta los disposisitivos vinculados en el ListView
           IdLista = (ListView) findViewById(R.id.IdLista);
           IdLista.setAdapter(mPairedDevicesArrayAdapter);
           IdLista.setOnItemClickListener(mDeviceClickListener);
           // Obtiene el adaptador local Bluetooth adapter
           mBtAdapter = BluetoothAdapter.getDefaultAdapter();

           //------------------- EN CASO DE ERROR -------------------------------------
           //SI OBTIENES UN ERROR EN LA LINEA (BluetoothDevice device : pairedDevices)
           //CAMBIA LA SIGUIENTE LINEA POR
           Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
           //------------------------------------------------------------------------------

           // Obtiene un conjunto de dispositivos actualmente emparejados y agregua a 'pairedDevices'
           //Set pairedDevices = mBtAdapter.getBondedDevices();


           // Adiciona un dispositivos previo emparejado al array
           if (pairedDevices.size() > 0) {
               for (BluetoothDevice device : pairedDevices) { //EN CASO DE ERROR LEER LA ANTERIOR EXPLICACION
                   mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
               }
           }
           SharedPreferences.Editor editor = sharedPreferences.edit();
           editor.putInt("ActivarBt", 1);
           // Puedes utilizar otros métodos como putFloat(), putLong(), etc., según tus necesidades.
           editor.apply();
       }

        else if (value != ("") || activar!=0)
        {
            String address = value;
            // Realiza un intent para iniciar la siguiente actividad
            // mientras toma un EXTRA_DEVICE_ADDRESS que es la dirección MAC.
            Intent i = new Intent(DispositivosBT.this, MainActivity.class);//<-<- PARTE A MODIFICAR >->->
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    }

    // Configura un (on-click) para la lista
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            // Obtener la dirección MAC del dispositivo, que son los últimos 17 caracteres en la vista
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("DireccionBT", address);
            editor.putString("NombreBT", info);
            // Puedes utilizar otros métodos como putFloat(), putLong(), etc., según tus necesidades.
            editor.apply();

            // Realiza un intent para iniciar la siguiente actividad
            // mientras toma un EXTRA_DEVICE_ADDRESS que es la dirección MAC.
            Intent i = new Intent(DispositivosBT.this, MainActivity.class);//<-<- PARTE A MODIFICAR >->->
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    };

    private void VerificarEstadoBT() {
        // Comprueba que el dispositivo tiene Bluetooth y que está encendido.
        mBtAdapter= BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {
                //Solicita al usuario que active Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}

