package usc.sql.testcase;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class LoopBranch {
	SQLiteDatabase db;
	SQLiteOpenHelper h ;
	int o;
	static double ddd;
	public void test1(SQLiteDatabase database,String sql)
	{
/*		StringBuilder s = new StringBuilder("Start");
		s.append(System.currentTimeMillis());
		System.out.println(s);
		int j =3;
		if(j<2)		
			System.out.println(s);		
		else
			System.out.println(s);
		for(int i = 0;i<3;i++)
		{
			if(i<2)
			{*/
		
		
		
				
				DB dd = new DB();
				test2(dd.db);
				dd.db.execSQL(sql);
				
		//	}
			
	//	}
	}
	
	public SQLiteDatabase test2(SQLiteDatabase db)
	{
		boolean ass = false;
		SQLiteDatabase newdb = null;
		int i = 0;
		if(i>3)
		{
			newdb = db;
			ass = true;
		}

		for(;i<3;i++)
		{
			if(ass)
				newdb.isOpen();

		}
		if(i<2)
			return db;
		else
			return db;

	}
	
	public SQLiteDatabase test3()
	{
		SQLiteDatabase db = h.getWritableDatabase();
		test2(db);
		return db;
	}
	 class DB
	{
		public  SQLiteDatabase db;
	}
}
