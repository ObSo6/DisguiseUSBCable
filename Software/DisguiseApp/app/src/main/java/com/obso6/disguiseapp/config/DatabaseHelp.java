package com.obso6.disguiseapp.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelp extends SQLiteOpenHelper {
    private static final String DATABASE = "script.db";// 数据库名称
    private static final int VENSION = 1;

    public DatabaseHelp(Context context) {
        // TODO Auto-generated method stub
        super(context, DATABASE, null, VENSION);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        String sql1 = "create table script(id varchar(4) primary key,name varchar(16),content varchar(500), introduce varchar(16))";
        arg0.execSQL(sql1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
    }
}
