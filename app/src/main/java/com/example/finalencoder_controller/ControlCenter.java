package com.example.finalencoder_controller;

import android.content.Context;
import android.util.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class ControlCenter {

    // singleton para tener acceso global a los datos de la app
    private static volatile ControlCenter current = null;

    // funcion para obtener la instancia del singleton
    public static ControlCenter getInstance() {
        if (current == null) {
            synchronized (ControlCenter.class) {
                if (current == null) {
                    current = new ControlCenter();
                }
            }
        }
        return current;
    }

    // singleton classes, para tener acceso global a los datos de la app
    public MainActivity mainActivity = null;
    public MainContentFragment mainContent = null;
    public GeneralFragment generalFrag = null;
    public DataFragment dataFrag = null;
    public ConnectionFragment connectionFrag = null;
    public DebuggingConsoleFragment debugFrag = null;


    // variables para guardar los datos de la app
    public String lastSentMsg = "", lastReceivedMsg = "";
    public String logMessages = "";
    public boolean connectedDevice = false, bluetoothConnected = false;

    public float scanInterval = 5;
    public float tempData, humiData, coData;
    public DataFragment.DateType lastCheck;

    // funcion para desconectar el dispositivo
    public void setBluetoothConnected(boolean state) {
        bluetoothConnected = state;
        if (!state) {
            mainActivity.makeSnackB("Se desconecto el Bluetooth");
        }

        if (!state && connectedDevice) {
            connectionFrag.disconnectDevice();
        }
    }

    // funcion que se llama cuando se conecta el dispositivo y se piden los datos (data.txt y schedules.txt)
    // se guardan los datos en el archivo correspondiente (data_ y schedules_)
    // @param state: estado de la conexion
    // @param name: nombre del dispositivo conectado
    public void setDeviceConnected(boolean state, String name) {
        connectedDevice = state;
        if (state) {
            generalFrag.deviceConnected(name);
            askForDataPoints(()->{ saveData(lastReceivedMsg.replace("\n", "-"), "data_", true); }, ()-> connectionFrag.disconnectDevice());

        } else {
            lastSentMsg = "";
            lastReceivedMsg = "";
            generalFrag.deviceDisconnected();
        }
    }

    // funcion para obtener los datos del archivo data.txt t los guarda en el archivo data_
    // @param onSucces: funcion que se ejecuta cuando se termina de guardar los datos
    // @param onFail: funcion que se ejecuta cuando no se puede guardar los datos
    public void askForDataPoints(Runnable onSucces, Runnable onFail) {
        String lDate = getData("data_");
        if(!lDate.isEmpty() && lDate.contains("-")){
            int idx = lDate.lastIndexOf("-", lDate.length() - 3);
            lDate = lDate.substring(idx + 1, lDate.indexOf(";", idx));
        }else{
            lDate = "-";
        }

        connectionFrag.requestInfo("SCAN;GET;"+lDate, onSucces, onFail ,100000);
    }

    // funcion que verifica la accion que esta realizando el dispositivo
    // @param msg: mensaje recibido del dispositivo
    void checkOperation(String msg) {
        if (msg.charAt(0) == '!') {
            msg = msg.substring(1);

            String[] str = msg.split(";");
            lastCheck = new DataFragment.DateType(str[0]);
            tempData = Float.parseFloat(str[1]);
            humiData = Float.parseFloat(str[2]);
            coData = Float.parseFloat(str[3]);

            generalFrag.showCapturedData(str[0], tempData, humiData, coData);
        }

    }

    // funcion que se llama cuando se recibe un mensaje del dispositivo y lo muestra en la consola de debug
    // @param msg: mensaje recibido del dispositivo
    public void setReceivedMessage(String msg) {
        logMessages += ">" + msg + "\n";
        lastReceivedMsg = msg;
        if (debugFrag != null && !debugFrag.isDestroyed()) {
            debugFrag.updateConsoleLog(lastReceivedMsg, true);
        }

        checkOperation(msg);
    }

    // funcion que se llama cuando se envia un mensaje al dispositivo y lo muestra en la consola de debug
    // @param msg: mensaje enviado al dispositivo
    public void setSentMessage(String msg) {
        logMessages += msg + "\n";
        lastSentMsg = msg;
        if (debugFrag != null && !debugFrag.isDestroyed()) {
            debugFrag.updateConsoleLog(lastSentMsg, true);
        }
    }

    /**
     * @param dInfo  datos a eliminar
     * @param onFile en el archivo
     * @return un booleano que indica si se elimino o no los datos
     */
    public boolean deleteData(String dInfo, String onFile) {
        String actD = getData(onFile);
        if (actD == null || actD.equals("") || !actD.contains(dInfo)) {
            return false;
        }
        actD = actD.replace(dInfo, "");
        saveData(actD, onFile, false);
        return true;
    }

    /**
     * funcion que guarda los datos en el almacenamiento externo (SD del telefono)
     *
     * @param data:    datos a guardar
     * @param nFile:   nombre del archivo
     * @param devName: nombre del dispositivo
     */
    public void saveDataOnStorage(String data, String nFile, String devName) {
        File dir = new File(mainActivity.getExternalStorage(), "Experimentos_Distancia");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File t = new File(dir, "DataDispositvo_" + devName);
        if (!t.exists()) {
            t.mkdir();
        }

        try {
            File fos = new File(t, nFile + ".txt");
            FileWriter writer = new FileWriter(fos, false);
            writer.write(data);
            writer.flush();
            writer.close();
            mainActivity.makeSnackB("Exportado correctamente en: " + t.getPath());
        } catch (IOException e) {
            mainActivity.makeSnackB("Intento " + t.getPath());
            mainActivity.makeSnackB("No se ha podido guardar (" + e + ")");
        }
    }

    /**
     * funcion que guarda los datos en el almacenamiento interno de la app
     *
     * @param sInfo:  datos a guardar
     * @param onFile: nombre del archivo
     * @param append: bool que determina si agregar los datos o sobreescribir
     */
    public void saveData(String sInfo, String onFile, boolean append) {
        File t = new File(mainActivity.getFilesDir(), connectionFrag.devName);
        if (!t.exists()) {
            t.mkdir();
        }
        try {
            File fos = new File(t, onFile + connectionFrag.devName + ".txt");
            FileWriter writer = new FileWriter(fos, append);
            writer.write(sInfo);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            mainActivity.makeSnackB("No se ha podido guardar (" + e + ")");
        }


    }

    /**
     * funcion que recupera los datos del dispositivo conectado
     *
     * @param fName: nombre del archivo
     */
    public String getData(String fName) {
        return getData(fName, connectionFrag.devName);
    }

    /**
     * funcion que recupera los datos del dispositivo conectado
     *
     * @param fName:   nombre del archivo
     * @param devName: nombre del dispositivo
     */
    public String getData(String fName, String devName) {
        File t = new File(mainActivity.getFilesDir(), devName);
        if (!t.exists()) {
            t.mkdir();
            return "";
        }

        String read = "";
        try {
            File fos = new File(t, fName + devName + ".txt");
            if (!fos.exists()) {
                return "";
            }
            FileInputStream reader = new FileInputStream(fos);
            BufferedReader bfr = new BufferedReader(new InputStreamReader(reader));
            String out = "";
            while ((out = bfr.readLine()) != null) {
                read += out;
            }
            reader.close();
            bfr.close();
        } catch (IOException e) {
            mainActivity.makeSnackB("No se ha podido leer (" + e + ")");
        }
        return read;
    }
}

