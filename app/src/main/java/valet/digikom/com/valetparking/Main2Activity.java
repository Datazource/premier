package valet.digikom.com.valetparking;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import valet.digikom.com.valetparking.adapter.ListCheckinAdapter;
import valet.digikom.com.valetparking.dao.CarDao;
import valet.digikom.com.valetparking.dao.CheckinDao;
import valet.digikom.com.valetparking.dao.ColorDao;
import valet.digikom.com.valetparking.dao.DefectDao;
import valet.digikom.com.valetparking.dao.DropDao;
import valet.digikom.com.valetparking.dao.EntryDao;
import valet.digikom.com.valetparking.dao.ItemsDao;
import valet.digikom.com.valetparking.dao.TokenDao;
import valet.digikom.com.valetparking.domain.Checkin;
import valet.digikom.com.valetparking.domain.EntryCheckin;
import valet.digikom.com.valetparking.domain.EntryCheckinResponse;
import valet.digikom.com.valetparking.util.ValetDbHelper;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ListCheckinAdapter.OnItemCheckinListener {

    RecyclerView listCheckin;
    ListCheckinAdapter adapter;
    ArrayList<Checkin> checkins = new ArrayList<>();
    List<EntryCheckinResponse> responseList = new ArrayList<>();
    TextView textEmpty;
    TextView textTotalCheckin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ValetDbHelper dbHelper = new ValetDbHelper(this);
        textTotalCheckin = (TextView) findViewById(R.id.text_total_checkin);
        listCheckin = (RecyclerView) findViewById(R.id.list_checkin);
        textEmpty = (TextView) findViewById(R.id.text_empty);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listCheckin.setHasFixedSize(true);
        listCheckin.setLayoutManager(layoutManager);
        adapter = new ListCheckinAdapter(checkins, responseList,this, this);
        listCheckin.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_entry);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Main2Activity.this, AddCarActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // load checkin on database
        new LoadCheckinTask().execute();

        DefectDao defectDao = DefectDao.getInstance(dbHelper);
        TokenDao.getToken(defectDao);
        TokenDao.getToken(ItemsDao.getInstance(dbHelper));
        TokenDao.getToken(CarDao.getInstance(dbHelper));
        TokenDao.getToken(ColorDao.getInstance(dbHelper));
        TokenDao.getToken(DropDao.getInstance(dbHelper));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemCheckinClick(int id) {
        Intent intent = new Intent(this, ParkedCarDetailActivity.class);
        intent.putExtra(EntryCheckinResponse.ID_ENTRY_CHECKIN, id);
        startActivity(intent);
    }

    private class LoadCheckin extends AsyncTask<String, Void, List<Checkin>> {

        @Override
        protected List<Checkin> doInBackground(String... strings) {
            CheckinDao dao = CheckinDao.newInstance(new ValetDbHelper(Main2Activity.this),Main2Activity.this);
            List<Checkin> checkinList = null;
            try {
                checkinList = dao.getAllListCheckIn();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return checkinList;
        }

        @Override
        protected void onPostExecute(List<Checkin> checkinsx) {
            if (checkinsx != null && !checkinsx.isEmpty()) {
                checkins.clear();
                checkins.addAll(checkinsx);
                adapter.notifyDataSetChanged();
                textEmpty.setVisibility(View.GONE);
                textTotalCheckin.setText(getResources().getString(R.string.total_checkin) + " " + checkinsx.size());
            }else {
                textTotalCheckin.setVisibility(View.INVISIBLE);
                textEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    private class LoadCheckinTask extends AsyncTask<Void, Void, List<EntryCheckinResponse>> {

        @Override
        protected List<EntryCheckinResponse> doInBackground(Void... voids) {
            EntryDao entryDao = EntryDao.getInstance(Main2Activity.this);
            return entryDao.fetchAllCheckinResponse();
        }

        @Override
        protected void onPostExecute(List<EntryCheckinResponse> entryCheckinResponses) {
            super.onPostExecute(entryCheckinResponses);
            if (entryCheckinResponses != null && !entryCheckinResponses.isEmpty()) {
                responseList.clear();
                responseList.addAll(entryCheckinResponses);
                adapter.notifyDataSetChanged();
                textEmpty.setVisibility(View.GONE);
                textTotalCheckin.setText(getResources().getString(R.string.total_checkin) + " " + entryCheckinResponses.size());
            }else {
                textTotalCheckin.setVisibility(View.INVISIBLE);
                textEmpty.setVisibility(View.VISIBLE);
            }
        }
    }
}
