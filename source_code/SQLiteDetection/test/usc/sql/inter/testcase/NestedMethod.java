package usc.sql.inter.testcase;

import android.database.sqlite.SQLiteDatabase;

public class NestedMethod {
	public void m1(SQLiteDatabase database,String sql)
	{
		database.beginTransaction();
		for(int i=0;i<2;i++)
		{
			m2(database,sql);
		}
		database.endTransaction();
		for(int i=0;i<2;i++)
		{
			m2(database,sql);
		}
	}


	private void m2(SQLiteDatabase database, String sql) {
		m3(database,sql);
		
	}
	

	public void m3(SQLiteDatabase database, String sql) {
		database.execSQL(sql);
		
	}
}
