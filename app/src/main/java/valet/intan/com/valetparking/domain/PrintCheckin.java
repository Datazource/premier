package valet.intan.com.valetparking.domain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.widget.Toast;
import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import valet.intan.com.valetparking.AddCarActivity;
import valet.intan.com.valetparking.R;
import valet.intan.com.valetparking.dao.TokenDao;
import valet.intan.com.valetparking.service.ApiClient;
import valet.intan.com.valetparking.service.ApiEndpoint;
import valet.intan.com.valetparking.service.ProcessRequest;
import valet.intan.com.valetparking.util.MakeCurrencyString;
import valet.intan.com.valetparking.util.PrefManager;

/**
 * Created by DIGIKOM-EX4 on 1/25/2017.
 */

public class PrintCheckin implements ReceiveListener {

    private Context context;
    private EntryCheckinResponse response;
    private Printer mPrinter;
    PrefManager prefManager;
    private Bitmap bitmapDefect;
    private Bitmap bitmapSign;
    private List<AdditionalItems> itemsList;
    private AddCarActivity addCarActivity;
    private Disclaimer.Data mDisclaimerData;

    public PrintCheckin(Context context, EntryCheckinResponse response, Bitmap  bmpDefect, Bitmap bmpSign, List<AdditionalItems> items) {
        this.context = context;
        addCarActivity = (AddCarActivity) context;
        this.response = response;
        prefManager = PrefManager.getInstance(context);
        itemsList = items;
        this.bitmapSign = scaleBitmap(bmpSign, 100,100);
        this.bitmapSign = createBorder(bitmapSign,2);
        if (bmpDefect == null) {
            this.bitmapDefect = scaleBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.car_vector_update_72),300,300);
        }else {
            this.bitmapDefect = scaleBitmap(bmpDefect, 300, 300);
        }
        this.bitmapDefect = combineImages(bitmapDefect, bitmapSign);
        initializeObject();
    }

    public void print() {
        TokenDao.getToken(new ProcessRequest() {
            @Override
            public void process(String token) {
                ApiEndpoint apiEndpoint = ApiClient.createService(ApiEndpoint.class, null);
                Call<Disclaimer> call = apiEndpoint.getDisclaimer(token);
                call.enqueue(new Callback<Disclaimer>() {
                    @Override
                    public void onResponse(Call<Disclaimer> call, Response<Disclaimer> response) {
                        if (response != null && response.body() != null) {
                            mDisclaimerData = response.body().getDataList().get(0);
                            if (buildPrintCheckin(mDisclaimerData)){
                                Toast.makeText(context, "Print success", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, "Print failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Disclaimer> call, Throwable t) {

                    }
                });
            }
        }, context);
    }

    private boolean runPrintCheckinData(Disclaimer.Data data) {
        if (!initializeObject()) {
            return false;
        }

        if (!buildPrintCheckin(data)) {
            return false;
        }

        if (!prindData()) {
            finalizeObject();
            return false;
        }
        return true;

    }

    private boolean prindData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();
        if (isPrintable(status)) {
            try {
                mPrinter.sendData(Printer.PARAM_DEFAULT);
            } catch (Epos2Exception e) {
                try {
                    mPrinter.disconnect();
                }
                catch (Exception ex) {
                    // Do nothing
                }
                return false;
            }
        } else {
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }
        return true;
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        else {
            ;//print available
        }

        return true;
    }

    private boolean initializeObject() {
        try {
            mPrinter = PrintManager.getPrinterInstance(context);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "Printer", context);
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
            addCarActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == Epos2CallbackCode.CODE_SUCCESS) {
                        Toast.makeText(context, "Print success", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "Print failed", Toast.LENGTH_SHORT).show();
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            disconnectPrinter();
                        }
                    }).start();
                }
            });
    }


    private boolean connectPrinter() {
        boolean isBeginTransaction = false;
        String target = prefManager.getPrinterMacAddress();
        if (mPrinter == null) {
            return false;
        }

        if (target == null) {
            return false;
        }
        try {
            mPrinter.connect(target,15000);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "connect", context);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
            //ShowMsg.showException(e, "beginTransaction", context);
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }
        mPrinter.clearCommandBuffer();
        mPrinter.setReceiveEventListener(null);
        mPrinter = null;
    }

    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {

        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {

        }

        finalizeObject();
    }

    private boolean buildPrintCheckin(Disclaimer.Data data) {

        if (mPrinter == null) {
            return false;
        }

        try {
            String noTransaksi = response.getData().getAttribute().getIdTransaksi();
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String dropPoint = response.getData().getAttribute().getDropPoint();
            String site = response.getData().getAttribute().getSiteName();
            String platNo = response.getData().getAttribute().getPlatNo();
            String valetType = response.getData().getAttribute().getValetType();
            Bitmap logoData = BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo_1);
            Bitmap logoExlusive = BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo_exclusive);

            StringBuilder sb = new StringBuilder();

            if ("exclusive".equals(valetType.toLowerCase())) {
                logoData = combineImages(logoExlusive, logoData);
            }

            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);

            mPrinter.addFeedLine(1);
            /*
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addText(site);

            mPrinter.addFeedLine(2);

            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addTextSize(2, 2);
            mPrinter.addText(noTransaksi);

            mPrinter.addFeedLine(2);
            */
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addTextSize(1, 1);
            sb.append(" No. Tiket     : " + noTransaksi + "\n");
            sb.append(" No. Plat      : " + platNo + "\n");
            sb.append(" Valet         : " + valetType + "\n");
            sb.append(" Checkin       : " + date + "\n");
            sb.append(" Tipe Mobil    : " + response.getData().getAttribute().getCar() + "\n");
            if (response.getData().getAttribute().getColor() != null) {
                sb.append(" Warna         : " + response.getData().getAttribute().getColor() + "\n");
            }
            sb.append(" Drop Point    : " + dropPoint + "\n");
            sb.append(" Harga         : " + MakeCurrencyString.fromInt(response.getData().getAttribute().getFee()));

            mPrinter.addText(sb.toString());
            sb.delete(0, sb.length());
            mPrinter.addFeedLine(1);
            mPrinter.addText("------------------------------------------\n");
            //mPrinter.addHLine(0, 100, Printer.LINE_THIN);

            mPrinter.addFeedLine(1);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addTextSize(1,1);
            mPrinter.addText(data.getAttrib().getDscDesc());

            mPrinter.addFeedLine(1);
            mPrinter.addCut(Printer.CUT_FEED);


            /*
            ------------------- Receipt for keyguard
             */
            if ("exclusive".equals(valetType.toLowerCase())) {
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                mPrinter.addImage(logoData, 0, 0,
                        logoData.getWidth(),
                        logoData.getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_MONO,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_AUTO);

                mPrinter.addFeedLine(1);

            }

            /*
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addText(site);

            mPrinter.addFeedLine(2);

            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addTextSize(2, 2);
            mPrinter.addText(noTransaksi);
            */
            //mPrinter.addFeedLine(1);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addTextSize(1, 1);
            sb.append(" No. Tiket     : " + noTransaksi + "\n");
            sb.append(" No. Plat      : " + platNo + "\n");
            sb.append(" Valet         : " + valetType + "\n");
            sb.append(" Checkin       : " + date + "\n");
            sb.append(" Tipe Mobil    : " + response.getData().getAttribute().getCar() + "\n");
            if (response.getData().getAttribute().getColor() != null) {
                sb.append(" Warna         : " + response.getData().getAttribute().getColor() + "\n");
            }
            sb.append(" Drop Point    : " + dropPoint + "\n");
            //sb.append(" Harga         :  " + MakeCurrencyString.fromInt(response.getData().getAttribute().getFee()));

            mPrinter.addText(sb.toString());
            sb.delete(0, sb.length());

            // image defect
            if (bitmapDefect != null) {
                mPrinter.addFeedLine(1);
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                mPrinter.addText("CHECK MOBIL");
                mPrinter.addFeedLine(1);
                mPrinter.addImage(bitmapDefect, 0, 0,
                        bitmapDefect.getWidth(),
                        bitmapDefect.getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_MONO,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_DEFLATE);
            }


            mPrinter.addFeedLine(1);

            if (itemsList != null && !itemsList.isEmpty()) {
                mPrinter.addTextAlign(Printer.ALIGN_LEFT);
                sb.append("Barang Berharga: ");
                mPrinter.addFeedLine(1);
                int loop = 1;
                for (AdditionalItems i : itemsList) {
                    sb.append(i.getAttributes().getName());
                    if (loop < itemsList.size()){
                        sb.append(", ");
                    }
                    loop++;
                }
                mPrinter.addText(sb.toString());
                sb.delete(0, sb.length());
                mPrinter.addFeedLine(1);
            }

            // ----------------- tanda tangan
            /*
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addText("Ttd");
            mPrinter.addFeedLine(1);
            mPrinter.addImage(bitmapSign, 0, 0,
                    bitmapSign.getWidth(),
                    bitmapSign.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);
            */
            mPrinter.addFeedLine(1);
            mPrinter.addCut(Printer.CUT_FEED);

            /*
            ---------- receipt to put on dashboard
             */
            if ("exclusive".equals(valetType.toLowerCase())) {
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                mPrinter.addImage(logoData, 0, 0,
                        logoData.getWidth(),
                        logoData.getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_MONO,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_AUTO);

                mPrinter.addFeedLine(1);

            }
            /*
            mPrinter.addFeedLine(1);

            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addText(site);

            mPrinter.addFeedLine(2);

            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addTextSize(2, 2);
            mPrinter.addText(noTransaksi);
            */
            //mPrinter.addFeedLine(1);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addTextSize(1, 1);
            sb.append(" No. Tiket     : " + noTransaksi + "\n");
            sb.append(" No. Plat      : " + platNo + "\n");
            sb.append(" Valet         : " + valetType + "\n");
            sb.append(" Checkin       : " + date + "\n");
            sb.append(" Tipe Mobil    : " + response.getData().getAttribute().getCar() + "\n");
            if (response.getData().getAttribute().getColor() != null) {
                sb.append(" Warna         : " + response.getData().getAttribute().getColor() + "\n");
            }
            sb.append(" Drop Point    : " + dropPoint + "\n");
            //sb.append(" Harga         :  " + MakeCurrencyString.fromInt(response.getData().getAttribute().getFee()));

            mPrinter.addText(sb.toString());
            sb.delete(0, sb.length());
            mPrinter.addFeedLine(2);

            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addTextSize(2,1);
            mPrinter.addText("DASHBOARD RECEIPT");

            mPrinter.addFeedLine(1);
            mPrinter.addCut(Printer.CUT_FEED);
        } catch (Epos2Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Bitmap scaleBitmap(Bitmap b, int x, int y) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, b.getWidth(), b.getHeight()), new RectF(0, 0, x, y), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
    }

    public static Bitmap createBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;

        int width, height = 0;

        if(c.getWidth() > s.getWidth()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = s.getWidth() + s.getWidth();
            height = c.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
    /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";

    OutputStream os = null;
    try {
      os = new FileOutputStream(loc + tmpImg);
      cs.compress(CompressFormat.PNG, 100, os);
    } catch(IOException e) {
      Log.e("combineImages", "problem combining images", e);
    }*/

        return cs;
    }
}
