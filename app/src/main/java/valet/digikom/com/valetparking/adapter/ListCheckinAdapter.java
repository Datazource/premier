package valet.digikom.com.valetparking.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import valet.digikom.com.valetparking.R;
import valet.digikom.com.valetparking.dao.EntryDao;
import valet.digikom.com.valetparking.dao.ReprintDao;
import valet.digikom.com.valetparking.domain.Checkin;
import valet.digikom.com.valetparking.domain.EntryCheckinResponse;

/**
 * Created by DIGIKOM-EX4 on 1/5/2017.
 */

public class ListCheckinAdapter extends RecyclerView.Adapter<ListCheckinAdapter.ViewHolder>{

    List<Checkin> checkinList;
    Context context;
    List<EntryCheckinResponse> responsesList;
    OnItemCheckinListener onItemCheckinListener;
    EntryDao entryDao;

    public ListCheckinAdapter(List<Checkin> checkinList, List<EntryCheckinResponse> responseList, Context context, OnItemCheckinListener onItemCheckinListener) {
        this.checkinList = checkinList;
        this.context = context;
        this.responsesList = responseList;
        this.onItemCheckinListener = onItemCheckinListener;
        entryDao = EntryDao.getInstance(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_checkin,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return responsesList.get(position).getData().getAttribute().getId();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        EntryCheckinResponse response = responsesList.get(position);
        String logoMobil = response.getData().getAttribute().getLogoMobil();

        int id = response.getData().getAttribute().getId();
        if (entryDao.isSynced(id)) {
            holder.imgSync.setVisibility(View.GONE);
        }else {
            holder.imgSync.setVisibility(View.VISIBLE);
        }

        Glide.with(context)
                .load(logoMobil)
                .centerCrop()
                .placeholder(R.drawable.car_icon)
                .crossFade()
                .into(holder.imgCar);

        String platNo = response.getData().getAttribute().getNoTiket() + " - " + response.getData().getAttribute().getPlatNo();
        String checkinTime = response.getData().getAttribute().getCheckinTime();

        holder.textPlatNo.setText(platNo);
        holder.textRunnerName.setText(checkinTime);

        holder.layoutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemCheckinListener.onItemCheckinClick((int) getItemId(position));
            }
        });

        holder.textPlatNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemCheckinListener.onItemCheckinClick((int)getItemId(position));
            }
        });
        holder.textOptions.setVisibility(View.VISIBLE);
        holder.textOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, holder.textOptions);
                popupMenu.inflate(R.menu.menu_reprint);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.reprint:
                                rePrintCurrentCheckin(getNomorTiket(position));
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private String getNomorTiket(int position) {
        return responsesList.get(position).getData().getAttribute().getNoTiket().trim();
    }

    private void rePrintCurrentCheckin(String noTiket) {
        if (noTiket!=null) {
            ReprintDao reprintDao = ReprintDao.getInstance(context);
            int status = reprintDao.rePrint(noTiket);

            switch (status) {
                case 1:
                    Toast.makeText(context,"Reprint succeed: " + noTiket, Toast.LENGTH_SHORT).show();
                    reprintDao.removeReprintData(noTiket);
                    break;
                case 0:
                    new MaterialDialog.Builder(context)
                            .title("Reprint Aborted")
                            .content("Either you already reprinted or using different device")
                            .positiveText("Oke")
                            .build()
                            .show();
                    break;
                case -1:
                    new MaterialDialog.Builder(context)
                            .title("Reprint Error")
                            .content("Please check the printer and try again.")
                            .positiveText("Oke")
                            .build()
                            .show();
                    break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return responsesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutContainer;
        TextView textPlatNo;
        TextView textRunnerName;
        TextView textIdCheckin;
        TextView textOptions;
        CircleImageView imgCar;
        ImageView imgSync;

        public ViewHolder(View view) {
            super(view);
            textPlatNo = (TextView) view.findViewById(R.id.text_plat_no);
            textRunnerName = (TextView) view.findViewById(R.id.text_runner);
            textIdCheckin = (TextView) view.findViewById(R.id.id_checkin);
            textOptions = (TextView) view.findViewById(R.id.textViewOptions);
            layoutContainer = (LinearLayout) view.findViewById(R.id.container_checkin);
            imgCar = (CircleImageView) view.findViewById(R.id.img_car);
            imgSync = (ImageView) view.findViewById(R.id.img_sync);
        }
    }

    public interface OnItemCheckinListener {
        void onItemCheckinClick(int id);
    }
}
