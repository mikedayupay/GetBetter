package com.dlsu.getbetter.getbetter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 14/05/2016.
 * GetBetter 2016
 */
public class CaseRecordDownloadAdapter extends RecyclerView.Adapter<CaseRecordDownloadAdapter.ViewHolder>{

    private ArrayList<CaseRecord> caseRecordsList;
    private OnItemClickListener mItemClickListener;
    private int selectedItem = 0;



    public CaseRecordDownloadAdapter(ArrayList<CaseRecord> objects) {
        this.caseRecordsList = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.case_record_item_download_checkbox, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.controlNumber.setText(String.valueOf(caseRecordsList.get(position).getCaseRecordControlNumber()));
        holder.patientName.setText(caseRecordsList.get(position).getPatientName());
        holder.complaint.setText(caseRecordsList.get(position).getCaseRecordComplaint());
//        holder.healthCenter.setText(caseRecordsList.get(position).getHealthCenter());
        holder.caseStatus.setText(caseRecordsList.get(position).getCaseRecordStatus());
        holder.itemView.setSelected(selectedItem == position);


    }

    @Override
    public int getItemCount() {
        return  caseRecordsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView controlNumber;
        TextView patientName;
        TextView complaint;
        TextView healthCenter;
        TextView caseStatus;
        TextView dateUpdated;

        ViewHolder(View inflate) {
            super(inflate);
            controlNumber = (TextView)inflate.findViewById(R.id.download_control_number);
            patientName = (TextView)inflate.findViewById(R.id.download_patient_name);
            complaint = (TextView)inflate.findViewById(R.id.download_chief_complaint);
            caseStatus = (TextView)inflate.findViewById(R.id.download_status);
            inflate.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mItemClickListener != null) {
                mItemClickListener.onItemClick(view, getAdapterPosition());
                notifyItemChanged(selectedItem);
                selectedItem = getAdapterPosition();
                notifyItemChanged(selectedItem);
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
