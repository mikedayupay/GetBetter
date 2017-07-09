package com.dlsu.getbetter.getbetter.objects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 30/05/2017.
 * GetBetter 2016
 */

public class AttachmentList {

    @SerializedName("case_attachments")
    private ArrayList<Attachment> attachments;

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }
}
