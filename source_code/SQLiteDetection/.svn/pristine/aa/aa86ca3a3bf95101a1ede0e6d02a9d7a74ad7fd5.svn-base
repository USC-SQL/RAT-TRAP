package usc.sql.inter.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Context {
	public void m1(SQLiteDatabase database,String sql)
	{
		
		for(int i=0;i<2;i++)
		{
			m2(database,sql);
		}
		database.beginTransaction();
		for(int i=0;i<2;i++)
		{
			m2(database,sql);
		}
		database.endTransaction();
	}


	private void m2(SQLiteDatabase database, String sql) {
		database.execSQL(sql);
		
		for(int i=0;i<2;i++)
		{
			database.execSQL(sql);
		}
	}
}
