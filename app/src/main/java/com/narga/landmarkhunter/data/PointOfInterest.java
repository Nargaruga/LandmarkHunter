package com.narga.landmarkhunter.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "point_of_interest")
public class PointOfInterest {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    @ColumnInfo(name = "address")
    private String address;
    private String date;
    private double latitude;
    private double longitude;
    @ColumnInfo(name = "image_path")
    private String imagePath;

    public PointOfInterest(@NonNull String id, String name, String address, String date, double latitude, double longitude, String imagePath) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imagePath = imagePath;
    }

    public void setId(@NonNull String s) {
        id = s;
    }

    @NonNull
    public String getId() {
        return id;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setImagePath(String s){
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
        return this.id.equals(poi.getId());
    }
}
