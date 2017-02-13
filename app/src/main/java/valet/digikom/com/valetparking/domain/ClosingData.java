package valet.digikom.com.valetparking.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by DIGIKOM-EX4 on 2/13/2017.
 */

public class ClosingData {

    @SerializedName("data")
    List<Data> dataList;

    public List<Data> getDataList() {
        return dataList;
    }

    public static class Data {
        @SerializedName("type")
        private final String type = "ad_checkout_finish_fine";
        @SerializedName("id")
        private String id;
        @SerializedName("attributes")
        private Attr attributes;

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public Attr getAttributes() {
            return attributes;
        }

        public static class Attr {
            @SerializedName("created_at")
            private String createdAt;
            @SerializedName("deleted_at")
            private String deletedAt;
            @SerializedName("updated_at")
            private String updatedAt;
            @SerializedName("vthd_cbms_logo")
            private String logoMobil;
            @SerializedName("vthd_id")
            private String valetHeaderId;
            @SerializedName("vthd_vfsd_name")
            private String valetTypeName;
            @SerializedName("vthd_vfsd_type")
            private String valetType;
            @SerializedName("vthd_transact_id")
            private String transactionId;
            @SerializedName("vthd_vfsd_fee")
            private String valetFee;

            public String getCreatedAt() {
                return createdAt;
            }

            public String getDeletedAt() {
                return deletedAt;
            }

            public String getUpdatedAt() {
                return updatedAt;
            }

            public String getLogoMobil() {
                return logoMobil;
            }

            public String getValetHeaderId() {
                return valetHeaderId;
            }

            public String getValetTypeName() {
                return valetTypeName;
            }

            public String getValetType() {
                return valetType;
            }

            public String getTransactionId() {
                return transactionId;
            }

            public String getValetFee() {
                return valetFee;
            }
        }
    }
}
