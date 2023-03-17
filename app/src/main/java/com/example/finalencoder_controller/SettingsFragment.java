package com.example.finalencoder_controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.finalencoder_controller.databinding.FragmentAdjustSamplingBinding;


public class SettingsFragment extends Fragment {

    private FragmentAdjustSamplingBinding binding;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        binding = FragmentAdjustSamplingBinding.inflate(inflater, container, false);
        binding.actTempTextViewer.setText(ControlCenter.getInstance().tempData + "°");
        binding.actHumiTextView.setText(ControlCenter.getInstance().humiData + "%");
        binding.actCO2TextView.setText(ControlCenter.getInstance().coData + "p");
        binding.adjustButton.setEnabled(ControlCenter.getInstance().bluetoothConnected);

        binding.adjustButton.setOnClickListener((view)-> SendAdjust());

        return binding.getRoot();
    }

    void SendAdjust(){
        String msg = "ADJUST;";
        msg += binding.temperatureEditText.getText().toString()  + ";";
        msg += binding.humidityEditText.getText().toString()  + ";";
        msg += binding.co2EditText.getText().toString()  + ";";

        ControlCenter.getInstance().connectionFrag.sendCommand(msg, ()->{ ControlCenter.getInstance().mainActivity.makeSnackB("Datos ajustados correctamente"); }, ()->{ ControlCenter.getInstance().mainActivity.makeSnackB("No fue posible ajustar los datos"); } ,10000);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
