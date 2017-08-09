package valet.digikom.com.valetparking.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import valet.digikom.com.valetparking.dao.EntryCheckinContainerDao;
import valet.digikom.com.valetparking.dao.EntryDao;
import valet.digikom.com.valetparking.dao.FinishCheckoutDao;
import valet.digikom.com.valetparking.dao.TokenDao;
import valet.digikom.com.valetparking.domain.CheckinList;
import valet.digikom.com.valetparking.domain.EntryCheckinResponse;
import valet.digikom.com.valetparking.fragments.ParkedCarFragment;
import valet.digikom.com.valetparking.util.SyncingCheckin;

/**
 * Created by DIGIKOM-EX4 on 3/21/2017.
 */

public class DownloadCurrentLobbyService extends IntentService {

    private static final String TAG = DownloadCurrentLobbyService.class.getSimpleName();
    public static final String ACTION_DOWNLOAD = "premier.valet.download.current.lobby";

    public DownloadCurrentLobbyService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Service Download data current lobby called");
        if (ACTION_DOWNLOAD.equals(intent.getAction())) {
            TokenDao.getToken(new ProcessRequest() {
                @Override
                public void process(String token) {
                    ApiEndpoint apiEndpoint = ApiClient.createService(ApiEndpoint.class, null);
                    Call<CheckinList> call = apiEndpoint.getCurrentCheckinList(500, token);
                    call.enqueue(new Callback<CheckinList>() {
                        @Override
                        public void onResponse(Call<CheckinList> call, Response<CheckinList> response) {
                            if (response != null && response.body() != null) {
                                List<EntryCheckinResponse.Data> downloadedCheckinList = response.body().getCheckinResponseList();

                                EntryDao.getInstance(DownloadCurrentLobbyService.this)
                                        .insertListCheckin(downloadedCheckinList);

                                //Update fakeVthdId to remoteVthdId in post checkout table that remains fakeVthdId
                                FinishCheckoutDao.getInstance(DownloadCurrentLobbyService.this)
                                        .updateCheckoutVthdId(downloadedCheckinList);

                                reloadParkedCarsList();
                            }
                        }

                        @Override
                        public void onFailure(Call<CheckinList> call, Throwable t) {

                        }
                    });
                }
            }, this);

            startDownloadCheckoutService();
        }
    }

    private void startDownloadCheckoutService() {
        Intent intent = new Intent(this, DownloadCheckoutService.class);
        intent.setAction(DownloadCheckoutService.ACTION);
        startService(intent);
    }

    private void reloadParkedCarsList() {
        Intent RTReturn = new Intent(ParkedCarFragment.RECEIVE_CURRENT_LOBBY_DATA);
        LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
    }
}
