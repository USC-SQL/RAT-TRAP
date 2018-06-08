package usc.sql.inter.testcase;

import java.util.HashSet;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;

public class ComplexFlow {
	public void m1(SQLiteDatabase database,String sql)
	{
		boolean end = false;
		database.beginTransaction();
		
		Set<Integer> t = new HashSet<>();
		t.add(1);
		if(t.size()<2)
		{
			database.endTransaction();
			end = true;
		}

		for(int i=0;i<2;i++)
		{
			database.execSQL(sql);
		}
		
		if(!end)
			database.endTransaction();


		
	}
}
