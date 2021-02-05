package com.narga.landmarkhunter.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "point_of_interest")
public class PointOfInterest {
    @PrimaryKey
    @NonNull
    private String _id;
    private String name;
    private String address;
    private String date;
    private Double latitude;
    private Double longitude;
    @ColumnInfo(name = "image_path")
    private String imagePath;

    public PointOfInterest(@NonNull String id, String name, String address, String date, Double latitude, Double longitude, String imagePath) {
        this._id = id;
        this.name = name;
        this.address = address;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imagePath = imagePath;
    }

    public void setId(@NonNull String s) {
        _id = s;
    }

    @NonNull
    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDate() {
        return date;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setImagePath(String s) {
        imagePath = s;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(obj.getClass() != this.getClass())
            return false;

        PointOfInterest poi = (PointOfInterest) obj;
        return poi.getId().equals(this._id) && poi.getImagePath().equals(this.imagePath);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this._id.hashCode();
        result = 37 * result + ((this.imagePath != null) ? this.imagePath.hashCode() : 0);

        return result;
    }
}
