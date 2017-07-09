package com.dlsu.getbetter.getbetter.objects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikedayupay on 29/05/2017.
 * GetBetter 2016
 */

public class CaseRecordList {

    @SerializedName("case_records")
    private ArrayList<CaseRecord> caseRecords;


    public ArrayList<CaseRecord> getCaseRecords() {
        return caseRecords;
    }
}
