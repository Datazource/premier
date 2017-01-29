package valet.digikom.com.valetparking;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import cn.pedant.SweetAlert.SweetAlertDialog;
import valet.digikom.com.valetparking.adapter.DropPointAdapter;
import valet.digikom.com.valetparking.dao.CallDao;
import valet.digikom.com.valetparking.dao.DropDao;
import valet.digikom.com.valetparking.dao.EntryDao;
import valet.digikom.com.valetparking.dao.TokenDao;
import valet.digikom.com.valetparking.domain.AddCarCall;
import valet.digikom.com.valetparking.domain.AddCarCallBody;
import valet.digikom.com.valetparking.domain.DropPointMaster;
import valet.digikom.com.valetparking.domain.EntryCheckinResponse;
import valet.digikom.com.valetparking.util.ValetDbHelper;

public class ParkedCarDetailActivity extends AppCompatActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    FloatingActionButton fab;
    EntryCheckinResponse entry;
    EntryDao entryDao;
    TextView txtPlatNo;
    TextView txtLokasiParkir;
    TextView txtNoTrans;
    TextView txtDropPoint;
    TextView txtCheckin;
    TextView txtFee;
    TextView txtEta;
    Button btnSetEta;
    EditText inputDropTo;
    DropPointMaster dropPointMaster;
    ImageButton btnDropPoint;
    ValetDbHelper dbHelper;
    private String arrivedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parked_car_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtPlatNo = (TextView) findViewById(R.id.text_plat_no);
        txtLokasiParkir = (TextView) findViewById(R.id.text_lokasi_parkir);
        txtNoTrans = (TextView) findViewById(R.id.text_no_transaksi);
        txtDropPoint = (TextView) findViewById(R.id.text_drop_point);
        txtCheckin = (TextView) findViewById(R.id.text_chekin_time);
        txtFee = (TextView) findViewById(R.id.text_fee);
        txtEta = (TextView) findViewById(R.id.text_arrived_time);
        inputDropTo = (EditText) findViewById(R.id.input_drop_point);
        btnDropPoint = (ImageButton) findViewById(R.id.btn_drop);
        btnSetEta = (Button) findViewById(R.id.btn_set_time);
        btnSetEta.setOnClickListener(this);

        btnDropPoint.setOnClickListener(this);
        dbHelper = new ValetDbHelper(this);
        new FetchDropPointTask().execute();
        entryDao = EntryDao.getInstance(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dropPointMaster == null) {
                    inputDropTo.setError("Please fill drop point.");
                    return;
                }

                showConfirmDialog();
            }
        });

        if (getIntent() != null) {
            int id = getIntent().getIntExtra(EntryCheckinResponse.ID_ENTRY_CHECKIN, 0);
            new LoadEntryTask().execute(id);
        }
    }

    private void init() {
        if (entry != null) {
            EntryCheckinResponse.Attribute attr = entry.getData().getAttribute();
            String platNo = attr.getPlatNo() + " - " + attr.getCar() + " (" + attr.getColor() + ")";
            String lokasiParkir = attr.getAreaParkir() + " " + attr.getBlokParkir() + " " + attr.getSektorParkir();

            txtPlatNo.setText(platNo);
            txtLokasiParkir.setText(lokasiParkir);
            txtNoTrans.setText(attr.getIdTransaksi());
            txtDropPoint.setText(attr.getDropPoint());
            txtCheckin.setText(attr.getCheckinTime());
            txtFee.setText(String.valueOf(attr.getFee()));
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnSetEta) {
            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            TimePickerDialog tpd = TimePickerDialog.newInstance(this, hourOfDay, minute, second, true);
            tpd.show(getFragmentManager(),"timepicker");
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        arrivedTime = sdf.format(calendar.getTime());

        String time = "" + hourOfDay + ":" + String.format("%02d", minute);
        txtEta.setText(time);
    }

    private class LoadEntryTask extends AsyncTask<Integer, Void, EntryCheckinResponse> {

        @Override
        protected EntryCheckinResponse doInBackground(Integer... integers) {
            return entryDao.getEntryByIdResponse(integers[0]);
        }

        @Override
        protected void onPostExecute(EntryCheckinResponse response) {
            super.onPostExecute(response);
            entry = response;
            init();
        }
    }

    private class FetchDropPointTask extends AsyncTask<Void, Void, List<DropPointMaster>> {

        @Override
        protected List<DropPointMaster> doInBackground(Void... voids) {
            return DropDao.getInstance(dbHelper).fetchAllDropPoints();
        }

        @Override
        protected void onPostExecute(List<DropPointMaster> dropPointMasters) {
            if (!dropPointMasters.isEmpty()){
                final DropPointAdapter adapter = new DropPointAdapter(ParkedCarDetailActivity.this, dropPointMasters);
                btnDropPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogPlus dialogPlus  = DialogPlus.newDialog(ParkedCarDetailActivity.this)
                                .setContentHolder(new GridHolder(2))
                                .setAdapter(adapter)
                                .setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                                        DropPointMaster dropPoint = (DropPointMaster) item;
                                        inputDropTo.setText(dropPoint.getAttrib().getDropName());
                                        dropPointMaster = dropPoint;
                                        dialog.dismiss();
                                    }
                                })
                                .setGravity(Gravity.CENTER)
                                .setExpanded(false)
                                .create();
                        dialogPlus.show();
                    }
                });
            }
        }
    }

    private void showConfirmDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Request Call")
                .setContentText("Call Car?")
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        call();
                        sweetAlertDialog.setTitleText("success!")
                                .setContentText("Registration success")
                                .setConfirmText("OK")
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                })
                .setCancelText("Cancel")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                    }
                })
                .showCancelButton(true)
                .show();
    }

    private void call() {
        AddCarCallBody addCarCallBody = new AddCarCallBody();
        AddCarCall addCarCall = new AddCarCall();
        AddCarCall.Attribute attribute = new AddCarCall.Attribute();

        attribute.setIdDropTo(String.valueOf(dropPointMaster.getAttrib().getDropId()));
        attribute.setArrivedTime(arrivedTime);

        addCarCall.setAttribute(attribute);
        addCarCallBody.setAddCarCall(addCarCall);

        int id = entry.getData().getAttribute().getId();
        CallDao callDao = CallDao.getInstance(ParkedCarDetailActivity.this);
        callDao.setId(id);
        callDao.setAddCarCallBody(addCarCallBody);
        callDao.setCalledCarById(id, EntryCheckinResponse.FLAG_CALL);

        TokenDao.getToken(callDao, this);

        startActivity(new Intent(ParkedCarDetailActivity.this, Main2Activity.class));
        finish();
    }

}
