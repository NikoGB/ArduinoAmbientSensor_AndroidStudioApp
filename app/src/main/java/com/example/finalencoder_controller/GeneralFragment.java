package com.example.finalencoder_controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.finalencoder_controller.databinding.FragmentGeneralBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.DecimalFormat;

public class GeneralFragment extends Fragment {

    private FragmentGeneralBinding binding;


    String rsTime;
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        binding = FragmentGeneralBinding.inflate(inflater, container, false);
        binding.connectionImageView.setOnClickListener( view -> { ControlCenter.getInstance().mainContent.navigateViewPag(2); } );

        // funcion del boton de actualizar el tiempo manda el comando CONFIG;TIEMPOMUESTREO;tiempo; al dispositivo
        binding.actualizarTiempoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ControlCenter.getInstance().connectedDevice){
                    ControlCenter.getInstance().mainActivity.makeSnackB("Debes estar conectado para usar esta funcion");
                    return;
                }
                int tiempoMuestreo = Integer.parseInt(String.valueOf(binding.editTextMuestreo.getText()));
                if(tiempoMuestreo<100){
                    ControlCenter.getInstance().mainActivity.makeSnackB("El tiempo de muestreo no puede ser menor a 100 ms");
                    return;
                }
                ControlCenter.getInstance().connectionFrag.sendCommand("CONFIG;TIEMPOMUESTREO;"+tiempoMuestreo+";",()->{ ControlCenter.getInstance().scanInterval = tiempoMuestreo; },10000);
            }
        });

        // funcion del boton de reiniciar el tiempo manda el comando RESET; al dispositivo
        binding.reiniciarTiempoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ControlCenter.getInstance().connectedDevice){
                    ControlCenter.getInstance().mainActivity.makeSnackB("Debes estar conectado para usar esta funcion");
                    return;
                }
                ControlCenter.getInstance().connectionFrag.sendCommand("RESET;",()->{
                    binding.editTextMuestreo.setText("");
                    ControlCenter.getInstance().scanInterval = 100;
                }  ,10000);
            }
        });


        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    // funcion que se llama cuando se recibe un dato del dispositivo
    public void showCapturedData(String sDate, float temp, float humi, float co){
        //CHANGE COLOR DEPENDING ON VALUE
        binding.lastDateTextView.setText(sDate.split("T")[0] + " " + sDate.split("T")[1]);
        binding.temperatureTextView1.setText(temp + "°");

        binding.temperatureTextView1.setBackgroundTintList(ColorStateList.valueOf(Color.HSVToColor(new float[]{36f * (45 - temp) / 10, 1, 1 })));;

        binding.humidityTextView1.setText(humi + "%");
        binding.humidityTextView1.setTextColor(ColorStateList.valueOf(Color.HSVToColor(new float[]{0, 1, 0})));

        binding.humidityTextView1.setBackgroundTintList(ColorStateList.valueOf(Color.HSVToColor(new float[]{190f, Math.min(0.05f + humi * 0.01f, 1), 1 })));

        //234 es azul 20 es verde
        binding.co2TextView1.setText(co + "P");
    }

    // funcion que se llama cuando se conecta un dispositivo 
    public void deviceConnected(String name){
        binding.connectionImageView.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.green));
        binding.connectionTextView.setText("Conectado");
        binding.connectionDeviceNameTextView.setText(name);
    }

    // funcion que se llama cuando se desconecta un dispositivo
    public void deviceDisconnected(){
        binding.connectionImageView.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.red));
        binding.connectionTextView.setText("Desconectado");
        binding.connectionDeviceNameTextView.setText("ninguno");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
