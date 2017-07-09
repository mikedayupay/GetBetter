package com.dlsu.getbetter.getbetter.interfaces;

import com.dlsu.getbetter.getbetter.objects.AttachmentList;
import com.dlsu.getbetter.getbetter.objects.CaseRecordList;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by mikedayupay on 29/05/2017.
 * GetBetter 2016
 */

public interface GetBetterClient {

    @GET("get_better/download_data.php")
    Call<CaseRecordList> getCaseRecords(

    );

    @GET("get_better/download_selected_data.php")
    Call<AttachmentList> getAttachmentList(
            @Query("case_record_id") Long caseRecordId
    );

    @Streaming
    @GET
    Call<ResponseBody> downloadAttachmentFile(
            @Url String fileUrl
    );
}
