package valet.digikom.com.valetparking;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import me.relex.circleindicator.CircleIndicator;
import valet.digikom.com.valetparking.adapter.PagerCheckinAdapter;
import valet.digikom.com.valetparking.domain.Checkin;
import valet.digikom.com.valetparking.fragments.ReviewFragment;
import valet.digikom.com.valetparking.fragments.StepOneFragmet;
import valet.digikom.com.valetparking.fragments.StepThreeFragment;
import valet.digikom.com.valetparking.fragments.StepTwoFragment;

public class AddCarActivity extends ActionBarActivity implements StepOneFragmet.OnRegsitrationValid, StepTwoFragment.OnDefectSelectedListener,
                StepThreeFragment.OnStuffSelectedListener{

    private ViewPager mPager;
    private Button mNextButton;
    private Button mPrevButton;
    CircleIndicator indicator;
    private PagerCheckinAdapter checkinAdapter;
    int position = -1;
    int totalPages = -1;
    boolean isCanScroll;
    Checkin checkin = new Checkin();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(4);
        checkinAdapter = new PagerCheckinAdapter(getSupportFragmentManager());
        checkinAdapter.addFragment(StepOneFragmet.newInstance(null, null), "" );
        checkinAdapter.addFragment(StepTwoFragment.newInstance(null, null), "");
        checkinAdapter.addFragment(StepThreeFragment.newInstance(null, null), "");
        checkinAdapter.addFragment(valet.digikom.com.valetparking.fragments.ReviewFragment.newInstance(null, null, checkin), "");
        mPager.setAdapter(checkinAdapter);

        indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                StepOneFragmet oneFragmet = (StepOneFragmet) checkinAdapter.getItem(0);
                if (position == 1) {
                        if (!oneFragmet.isFormValid()) {
                            mPager.setCurrentItem(0);
                        }else {
                            oneFragmet.setCheckIn();
                        }
                }
            }

            @Override
            public void onPageSelected(int position) {
                updateBottomBar();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING){
                    if (mPager.getCurrentItem() == 0) {
                        StepOneFragmet oneFragmet = (StepOneFragmet) checkinAdapter.getItem(0);
                        isCanScroll = oneFragmet.isFormValid();
                    }
                }
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position == totalPages) {

                } else {
                    mPager.setCurrentItem(position + 1);
                }
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(position - 1);
            }
        });

        updateBottomBar();
    }

    private void updateBottomBar() {
        position = mPager.getCurrentItem();
        totalPages = checkinAdapter.getCount()-1;

        if (position == totalPages) {
            mNextButton.setText(R.string.finish);
            mNextButton.setBackgroundResource(R.drawable.finish_background);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        }else {
            mNextButton.setText(R.string.next);
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
            mNextButton.setTextColor(Color.BLACK);
        }

        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void setCheckin(String dropPoint, String platNo, String carType, String merk, String email, String warna) {
        ReviewFragment reviewFragment = ReviewFragment.reviewFragment;
        reviewFragment.setCheckin(dropPoint, platNo, carType, merk, email, warna);
    }

    @Override
    public void onDefectSelected(String defect) {
        checkin.getDefects().add(defect);
    }

    @Override
    public void onDefectUnselected(String defect) {
        checkin.getDefects().remove(defect);
    }

    @Override
    public void onStuffSelected(String stuff) {
        checkin.getStuffs().add(stuff);
    }

    @Override
    public void onStuffUnselected(String stuff) {
        checkin.getStuffs().remove(stuff);
    }
}
