package valet.digikom.com.valetparking.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import valet.digikom.com.valetparking.dao.CheckoutDao;
import valet.digikom.com.valetparking.dao.TokenDao;


/**
 * Created by DIGIKOM-EX4 on 1/23/2017.
 */

public class EntryCheckoutReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Entry Checkout", "Entry checkout receiver called");
        CheckoutDao checkoutDao = CheckoutDao.getInstance(context);
        TokenDao.getToken(checkoutDao);
    }
}
