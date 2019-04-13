package com.example.gpstest.DbDebugEnvironment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.amap.api.maps.model.LatLng;
import java.util.ArrayList;

public class DbOperator {
    private SQLiteDbHelper dbHelper;

    public DbOperator(Context context) {
        dbHelper = new SQLiteDbHelper(context);
    }

    public ArrayList<LatLng> getLocationList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select * from " + Note.TABLE;
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        Cursor cursor = db.rawQuery(sql, null);
        double longitude, latitude;
        while (cursor.moveToNext()) {
            longitude = cursor.getDouble(cursor.getColumnIndex(Note.KEY_longitude));
            latitude = cursor.getDouble(cursor.getColumnIndex(Note.KEY_latitude));
            LatLng oneline = new LatLng(latitude, longitude);
            list.add(oneline);
        }
        cursor.close();
        db.close();
        return list;
    }
}
