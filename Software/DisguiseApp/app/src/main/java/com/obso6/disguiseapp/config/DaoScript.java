package com.obso6.disguiseapp.config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

//脚本的curd
public class DaoScript {
    //DatabaseHelp实例
    private final com.obso6.disguiseapp.config.DatabaseHelp mDatabaseHelp;
    //数据库实例
    private SQLiteDatabase db;
    public DaoScript(Context context) {
        mDatabaseHelp = new DatabaseHelp(context);
    }
    //添加数据
    public long addScript(ScriptTable scriptTable) {
        db = mDatabaseHelp.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", scriptTable.getId());
        cv.put("name", scriptTable.getName());
        cv.put("content", scriptTable.getContent());
        cv.put("introduce", scriptTable.getIntroduce());
        long result=db.insert("script", null, cv);
        db.close();
        return result;
    }
    //查询数据
    public List<ScriptTable> quereyScript() {
        List<ScriptTable> listScript = new ArrayList<>();
        db = mDatabaseHelp.getReadableDatabase();
        //sql
        Cursor cursor = db.rawQuery("select * from script", null);
        int cu_id, cu_name, cu_content, cu_introduce;
        //遍历
        while (cursor.moveToNext()) {
            ScriptTable scripttable = new ScriptTable();
            cu_id = cursor.getColumnIndex("id");
            cu_name = cursor.getColumnIndex("name");
            cu_content = cursor.getColumnIndex("content");
            cu_introduce = cursor.getColumnIndex("introduce");
            scripttable.setId(cursor.getString(cu_id));
            scripttable.setName(cursor.getString(cu_name));
            scripttable.setContent(cursor.getString(cu_content));
            scripttable.setIntroduce(cursor.getString(cu_introduce));
            // 添加到集合
            listScript.add(scripttable);
        }
        db.close();
        for(int i=0; i<listScript.size(); i++){
            System.out.println(listScript.get(i).getId()+" "+listScript.get(i).getName()+" "+listScript.get(i).getContent()+" "+listScript.get(i).getIntroduce());
        }
        return listScript;
    }
    //查询所有数据size
    public int quereyScriptSize() {
        List<ScriptTable> listScript = new ArrayList<>();
        db = mDatabaseHelp.getReadableDatabase();
        //sql
        Cursor cursor = db.rawQuery("select * from script", null);
        int cu_id, cu_name, cu_version, cu_author, cu_content, cu_introduce;
        //遍历
        while (cursor.moveToNext()) {
            ScriptTable scripttable = new ScriptTable();
            cu_id = cursor.getColumnIndex("id");
            cu_name = cursor.getColumnIndex("name");
            cu_content = cursor.getColumnIndex("content");
            cu_introduce = cursor.getColumnIndex("introduce");
            scripttable.setId(cursor.getString(cu_id));
            scripttable.setName(cursor.getString(cu_name));
            scripttable.setContent(cursor.getString(cu_content));
            scripttable.setIntroduce(cursor.getString(cu_introduce));
            // 添加到集合
            listScript.add(scripttable);
            db.close();
        }
        return listScript.size();
    }

    //查询单行数据
    public ScriptTable quereyScriptRaw(ScriptTable scripttable) {
        db = mDatabaseHelp.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from script where id=?",
                new String[] { scripttable.getId() });
        int cu_id, cu_name, cu_content, cu_introduce;
        while (cursor.moveToNext()) {
            cu_id = cursor.getColumnIndex("id");
            cu_name = cursor.getColumnIndex("name");
            cu_content = cursor.getColumnIndex("content");
            cu_introduce = cursor.getColumnIndex("introduce");
            scripttable.setId(cursor.getString(cu_id));
            scripttable.setName(cursor.getString(cu_name));
            scripttable.setContent(cursor.getString(cu_content));
            scripttable.setIntroduce(cursor.getString(cu_introduce));
            db.close();
        }
        return scripttable;
    }

    //删除数据
    public int deleteScript(String id){
        db = mDatabaseHelp.getReadableDatabase();
        int result=db.delete("script", "id=?", new String[]{id});
        db.close();
        return result;
    }

    //编辑数据
    public long editScript(ScriptTable scripttable) {
        db = mDatabaseHelp.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", scripttable.getName());
        cv.put("content", scripttable.getContent());
        cv.put("introduce", scripttable.getIntroduce());
        long result=db.update("script",cv,"id="+scripttable.getId(),null);
        db.close();
        return result;
    }
}
