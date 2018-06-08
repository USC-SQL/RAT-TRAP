package usc.sql.inter.testcase;

import android.database.sqlite.SQLiteDatabase;

public class NestedLoop {
	public void m1(SQLiteDatabase database,String sql)
	{
		for(int i=0;i<2;i++)
		{
			database.execSQL(sql);
			for(int j=0;j<2;j++)
			{
				database.execSQL(sql);
				m2(database,sql);
			}
		}
	}
	public void m2(SQLiteDatabase database,String sql)
	{
		database.execSQL(sql);
		m3(database);
		for(int i=0;i<2;i++)
		{
			database.execSQL(sql);
		}
		database.endTransaction();
	}
	public void m3(SQLiteDatabase database)
	{
		database.beginTransaction();
	}
}
