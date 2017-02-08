package com.dlsu.getbetter.getbetter.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 15/04/2016.
 * GetBetter 2016
 */
public class CaseRecordUploadAdapter extends RecyclerView.Adapter<CaseRecordUploadAdapter.ViewHolder> {

    private ArrayList<CaseRecord> caseRecordsList;
    private int selectedItem = 0;
    private OnItemClickListener mItemClickListener;

    public CaseRecordUploadAdapter(ArrayList<CaseRecord> objects) {
        this.caseRecordsList = objects;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.case_record_item_checkbox, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.controlNumber.setText(caseRecordsList.get(position).getCaseRecordControlNumber());
        holder.patientName.setText(caseRecordsList.get(position).getPatientName());
        holder.complaint.setText(caseRecordsList.get(position).getCaseRecordComplaint());
        holder.complaint.setText(caseRecordsList.get(position).getCaseRecordStatus());
        holder.itemView.setSelected(selectedItem == position);
    }

    @Override
    public int getItemCount() {
        return caseRecordsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView controlNumber;
        TextView patientName;
        TextView complaint;
        TextView caseStatus;
        TextView dateUpdated;

        ViewHolder(View inflate) {
            super(inflate);

            this.controlNumber = (TextView)inflate.findViewById(R.id.upload_control_number);
            this.patientName = (TextView)inflate.findViewById(R.id.upload_caserecord_patient_name);
            this.complaint = (TextView)inflate.findViewById(R.id.upload_caserecord_chief_complaint);
            this.caseStatus = (TextView)inflate.findViewById(R.id.upload_caserecord_status);
            inflate.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            mItemClickListener.onItemClick(view, getAdapterPosition());
            notifyItemChanged(selectedItem);
            selectedItem = getAdapterPosition();
            notifyItemChanged(selectedItem);
        }
    }


    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
