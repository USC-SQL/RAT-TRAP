package usc.sql.inter.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Mix {

	public void m1(SQLiteDatabase database,String sql)
	{
		database.beginTransaction();
		for(int i=0;i<2;i++)
		{
			database.execSQL(sql);
		}
		m2(database,sql);
		for(int i=0;i<2;i++)
		{
			m3(database,sql);
		}
	}


	private void m2(SQLiteDatabase database, String sql) {
		database.execSQL(sql);
		database.endTransaction();
		
	}
	

	private void m3(SQLiteDatabase database, String sql) {
		database.execSQL(sql);
		
	}
}
