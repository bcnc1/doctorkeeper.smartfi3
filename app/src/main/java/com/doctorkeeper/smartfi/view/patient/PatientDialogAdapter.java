package com.doctorkeeper.smartfi.view.patient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.doctorkeeper.smartfi.R;
import com.doctorkeeper.smartfi.network.BlabAPI;
import com.doctorkeeper.smartfi.network.MadamfiveAPI;
import com.doctorkeeper.smartfi.util.SmartFiPreference;

import java.util.HashMap;
import java.util.List;

//import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.patientSearchDisplayExtraOption;


public class PatientDialogAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<HashMap<String,String>> items;
    private TextView patient_name;
    private TextView patient_chartNumber;
    private Boolean patientSearchDisplayExtraOption;

    public PatientDialogAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(List<HashMap<String,String>> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (items==null) {
            return 0;
        }
        return items.size();
    }

    @Override
    public HashMap<String,String> getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = inflater.inflate(R.layout.patient_item, viewGroup, false);

        patient_name = (TextView) view.findViewById(R.id.patient_name);
        patient_chartNumber = (TextView) view.findViewById(R.id.patient_chartNumber);

        patientSearchDisplayExtraOption = SmartFiPreference.getSfDisplayExtraOpt(BlabAPI.getActivity());
        if(patientSearchDisplayExtraOption){
            HashMap<String, String> patientInfo = getItem(i);
            patient_name.setText(patientInfo.get("name"));
            if(patientInfo.get("birthDate") == null){
                patient_chartNumber.setText("");
            }else{
                patient_chartNumber.setText(patientInfo.get("birthDate"));
            }
        }else {
            HashMap<String, String> patientInfo = getItem(i);
            String name = patientInfo.get("name");
            if(name.equals("null")){
                name = "";
            }
            patient_name.setText(name);

            String chartNumber = patientInfo.get("chartNumber");

            if(chartNumber.equals("null")){
                chartNumber = "";
            }

            patient_chartNumber.setText(chartNumber);
        }
        if(getCount()==0)   patient_name.setText("결과 없음");

        return view;
    }
}
