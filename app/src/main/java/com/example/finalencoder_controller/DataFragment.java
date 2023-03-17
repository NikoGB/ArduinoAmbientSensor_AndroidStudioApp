package com.example.finalencoder_controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import com.example.finalencoder_controller.databinding.FragmentDataViewerBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.DecimalFormat;

public class DataFragment extends Fragment {

    String rsTime = "";
    int truie = 0;
    private FragmentDataViewerBinding binding;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDataViewerBinding.inflate(inflater, container, false);
        binding.editTextTime2.setShowSoftInputOnFocus(false);
        binding.editTextTime2.setOnClickListener((view) -> {

                    InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    // si estamos seleccionando un dia, creamos un nuevo fragmento de seleccion de dia
                    DatePickerFragment dPick = new DatePickerFragment();
                    dPick.onDPick = () -> {
                        DecimalFormat df = new DecimalFormat("00");
                        rsTime = dPick.year + "/" + df.format(dPick.month) + "/" + df.format(dPick.day);
                        binding.editTextTime2.setText(rsTime);
                        SetDateToDisplay(dPick.year + "/" + dPick.month + "/" + dPick.day);
                    };
                    // mostramos el fragmento de seleccion de dia que acabamos de crear
                    dPick.show(ControlCenter.getInstance().mainActivity.getSupportFragmentManager(), "select date");

                }
        );

        binding.editTextTime2.setOnFocusChangeListener((view, b) -> {
                    if (b) {
                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                        // si estamos seleccionando un dia, creamos un nuevo fragmento de seleccion de dia
                        DatePickerFragment dPick = new DatePickerFragment();
                        dPick.onDPick = () -> {
                            DecimalFormat df = new DecimalFormat("00");
                            rsTime = dPick.year + "/" + df.format(dPick.month) + "/" + df.format(dPick.day);
                            binding.editTextTime2.setText(rsTime);
                            SetDateToDisplay(dPick.year + "/" + dPick.month + "/" + dPick.day);
                        };
                        // mostramos el fragmento de seleccion de dia que acabamos de crear
                        dPick.show(ControlCenter.getInstance().mainActivity.getSupportFragmentManager(), "select date");
                    }
                }
        );

        return binding.getRoot();
    }

    String[] sDates;
    void SetDateToDisplay(String sDate) { //ONLY DATE(YEAR...DAYS) NOT TIME (HOURS...SECONS)

        if(!binding.temperatureGraphView.getSeries().isEmpty()){
            binding.temperatureGraphView.removeAllSeries();
            binding.humidityGraphView.removeAllSeries();
            binding.co2GraphView.removeAllSeries();
        }

        String data = ControlCenter.getInstance().getData("data_");
        if(data == ""){ ControlCenter.getInstance().mainActivity.makeSnackB("No se encontraron datos"); return; }
        int st = data.indexOf(sDate);
        if(st < 0){ ControlCenter.getInstance().mainActivity.makeSnackB("No se encontraron datos"); return; }

        int ed = data.indexOf("-", data.lastIndexOf(sDate));
        SetGraphs(data.substring(st, ed));
    }

    void SetGraphs(String datos) {
        String[][] dats = new String[4][];
        if (datos != "") {
            String[] splits = datos.split("-");
            dats[0] = new String[splits.length];
            dats[1] = new String[splits.length];
            dats[2] = new String[splits.length];
            dats[3] = new String[splits.length];

            for (int i = 0; i < splits.length; i++) {
                String[] sp = splits[i].split(";");
                dats[0][i] = sp[0];
                dats[1][i] = sp[1];
                dats[2][i] = sp[2];
                dats[3][i] = sp[3];
            }
        } else {
            return;
        }
        sDates = dats[0];

        // label formatter para el grafico
        LabelFormatter labfor = new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {

                if (isValueX) {

                    int idx = (int)value;
                    if(idx >= sDates.length){ return "End"; }

                    return sDates[idx].split("T")[1];
                } else {
                    return "" + value;
                }
            }

            @Override
            public void setViewport(Viewport viewport) {

            }
        };

        binding.temperatureGraphView.getGridLabelRenderer().setLabelFormatter(labfor);
        binding.humidityGraphView.getGridLabelRenderer().setLabelFormatter(labfor);
        binding.co2GraphView.getGridLabelRenderer().setLabelFormatter(labfor);

        amountDataX = new double[]{ 1, 1, 1};
        for (int i = 0; i < 3; i++) {

            LineGraphSeries<DataPoint> mDataSeries = new LineGraphSeries<>();
            PointsGraphSeries<DataPoint> mPointDataSeries = new PointsGraphSeries<>();

            // declara un double para almacenar la ultima posicion de x
            double dataGraphLastX = 0d;
            // divide los datos en una array basandose en el formato de !distance1\n!distance2...

            // para cada dato en datoS
            for (String dat : dats[i + 1]) {
                // verifica que los datos no sean vacios
                if (dat.equals("")) {
                    continue;
                }

                // transforma el valor del dato (distancia) a un float
                float dist = Float.parseFloat(dat);
                // agrega los los datos al grafico
                mPointDataSeries.appendData(new DataPoint(dataGraphLastX, dist), false, 36000);
                mDataSeries.appendData(new DataPoint(dataGraphLastX, dist), false, 36000);
                // agrega 1 a la ultima posicion de X
                dataGraphLastX += 1d;
            }
            int idx = i;
            // define la forma de los datos en el grafico
            mPointDataSeries.setCustomShape(new PointsGraphSeries.CustomShape() {
                @Override
                public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                    if (((int) dataPoint.getX() % (int) amountDataX[idx]) != 0 || dataPoint.getX() == mPointDataSeries.getHighestValueX()) {
                        return;
                    }
                    paint.setColor(getResources().getColor(android.R.color.holo_red_dark));
                    paint.setStrokeWidth(2);
                    paint.setTextSize(36);
                    DecimalFormat df = new DecimalFormat("#.##");
                    String str = df.format(dataPoint.getY());
                    canvas.drawText(str, x - (18 * (str.length() - 1) - 9), y - 30, paint);
                }
            });

            mPointDataSeries.appendData(new DataPoint(dataGraphLastX + 1, mPointDataSeries.getHighestValueY() + 5), false, 360000);
            mDataSeries.setDrawDataPoints(true);
            mDataSeries.setDataPointsRadius(6);

            GraphView dataGraph = (i == 0 ? binding.temperatureGraphView : i == 1 ? binding.humidityGraphView : binding.co2GraphView);

            // agrega los datos al grafico
            dataGraph.addSeries(mDataSeries);
            dataGraph.addSeries(mPointDataSeries);
            // define el tamaño de los datos
            dataGraph.getViewport().setXAxisBoundsManual(true);
            dataGraph.getViewport().setOnXAxisBoundsChangedListener(new Viewport.OnXAxisBoundsChangedListener() {
                @Override
                public void onXAxisBoundsChanged(double minX, double maxX, Reason reason) {
                    dataGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
                    /*
                    if ((amountDataX[idx] = maxX - minX) > 4) {
                        dataGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
                    } else {
                        dataGraph.getGridLabelRenderer().setNumHorizontalLabels((int) Math.ceil(amountDataX[idx]) + 1);
                    }
                    amountDataX[idx] = (int) Math.floor(Math.max(1, amountDataX[idx] / 8));*/
                }
            });

            // define el tamaño del grafico y el tamaño de los datos
            dataGraph.getViewport().setMinX(mDataSeries.getLowestValueX());
            dataGraph.getViewport().setMaxX(mDataSeries.getHighestValueX() - 1);
            //dataGraph.getViewport().scrollToEnd();

            dataGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
            dataGraph.getGridLabelRenderer().setLabelVerticalWidth(100);

            dataGraph.getViewport().setScalable(true);
            dataGraph.getViewport().setScrollable(true);
            dataGraph.setNestedScrollingEnabled(true);

            // asigna el label para el formateo de los datos
            //dataGraph.getGridLabelRenderer().setLabelFormatter(labfor);
            dataGraph.getGridLabelRenderer().setVerticalAxisTitle(i == 0 ? "Temperatura(C°)" : i == 1 ? "Humedad(%)" : "CO2(PPM)");
            dataGraph.getGridLabelRenderer().setHorizontalAxisTitle(" \nHora de muestreo");
        }

    }

    double[] amountDataX = { 1, 1, 1};
    public static class DateType {

        public int year() {
            return data[0];
        }

        public int month() {
            return data[1];
        }

        public int day() {
            return data[2];
        }

        public int hour() {
            return data[3];
        }

        public int minute() {
            return data[4];
        }

        public int second() {
            return data[5];
        }

        public int[] data;

        // constructor para crear un DateType con la fecha actual
        public DateType(String dat) {
            String[] aInfo = dat.split("T");
            String[] dInfo = aInfo[0].split("/");
            String[] tInfo = aInfo[1].split(":");

            data = new int[]{Integer.parseInt(dInfo[0]), Integer.parseInt(dInfo[1]), Integer.parseInt(dInfo[2]),
                    Integer.parseInt(tInfo[0]), Integer.parseInt(tInfo[1]), Integer.parseInt(tInfo[2])};
        }

        // funcion para transformar el DateType a un string
        @Override
        public String toString() {
            DecimalFormat dF = new DecimalFormat("00");
            return year() + "/" + dF.format(month()) + "/" + dF.format(day()) + "\n" + dF.format(hour()) + ":" + dF.format(minute()) + ":" + dF.format(second());
        }

        // funcion para transformar el DateType a un string para parsear
        public String toParse() {
            return year() + "/" + month() + "/" + day() + "T" + hour() + ":" + minute() + ":" + second();
        }

        // funcion para comparar dos DateType
        public int compare(int[] toComp) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == toComp[i]) {
                    continue;
                } else if (data[i] > toComp[i]) {
                    return 1;
                } else {
                    return -1;
                }
            }

            return 0;
        }

        public int secDiff(int[] toComp) {
            int secs = toComp[3] * 3600 + toComp[4] * 60 + toComp[5];
            return secs;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
