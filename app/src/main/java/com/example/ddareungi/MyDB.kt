package com.example.ddareungi

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.ddareungi.dataClass.Bookmark

class MyDB(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        val DB_NAME = "UserDB"
        val DB_VERSION = 1
        val TABLE_NAME = "users"
        val RENTAL_OFFICE = "OfficeName"
        var CHECKED = "bookmarked"
    }

    override fun onCreate(p0: SQLiteDatabase?) {//테이블 생성
        val createTable =
            "CREATE TABLE $TABLE_NAME" +
                    "($RENTAL_OFFICE TEXT PRIMARY KEY," + "$CHECKED INTEGER)"
        p0?.execSQL(createTable)//select를 사용하지 않는경우 execSQL 사용
        //TODO("not implemented") //To ch ange body of created functions use File | Settings | File Templates.
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun addUser(rental: Bookmark): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(RENTAL_OFFICE, rental.rentalOffice)
        values.put(CHECKED, rental.bookmarked)
        val success = db.insert(TABLE_NAME, null, values)
        db.close()
        return (Integer.parseInt("$success") != -1)//새롭게 insert된 Row Id를 반환
    }

    fun deleteUser(rental: Bookmark): Boolean {
        val db = this.writableDatabase
        var st: Array<String>
        st = arrayOf(rental.delete)
        val success = db.delete(TABLE_NAME, RENTAL_OFFICE + "=?", st)
        db.close()
        return (Integer.parseInt("$success") != -1)
    }



    fun getAllUser(): ArrayList<Bookmark> {
        var bookmarked: Int = 0;
        var rentalOffice: String = ""
        var bookmarkArray: ArrayList<Bookmark> = ArrayList()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME"//query문을 저장
        val cursor = db.rawQuery(selectALLQuery, null)//인자로 받은 query문 실행
        if (cursor != null) {
            if (cursor.moveToFirst()) {//cursor를 가장 첫번째 행으로 옮긴다
                do {
                    rentalOffice = cursor.getString(cursor.getColumnIndex(RENTAL_OFFICE))
                    bookmarked = cursor.getInt(cursor.getColumnIndex(CHECKED))
                bookmarkArray.add(Bookmark("$rentalOffice",0, bookmarked, ""))
            } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return bookmarkArray
    }

    fun findOffice(rentalOffice: String): Int {
        var search = 0
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $RENTAL_OFFICE = '$rentalOffice'"//query문을 저장
        val cursor = db.rawQuery(selectALLQuery, null)
        search = cursor.getCount()
        return search
    }

    fun findOfficeWithRow(row:Int):String{
        var rental = ""
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME"//query문을 저장
        val cursor = db.rawQuery(selectALLQuery, null)//인자로 받은 query문 실행
        cursor.moveToPosition(row)
        rental = cursor.getString(cursor.getColumnIndex(RENTAL_OFFICE))

        return rental
    }
}