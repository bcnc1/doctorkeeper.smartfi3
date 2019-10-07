package com.thinoo.drcamlink2.view.patient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.thinoo.drcamlink2.R;
import java.util.HashMap;
import java.util.List;

import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.patientSearchDisplayExtraOption;


public class PatientDialogAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<HashMap<String,String>> items;
    private TextView patient_name;
    private TextView patient_chartNumber;

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
            patient_name.setText(patientInfo.get("name"));
            patient_chartNumber.setText(patientInfo.get("chartNumber"));
        }
        if(getCount()==0)   patient_name.setText("결과 없음");

        return view;
    }
}
