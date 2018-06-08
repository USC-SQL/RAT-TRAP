package usc.sql.testcase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.database.sqlite.SQLiteDatabase;

public class Exception {
	
	public void test1(SQLiteDatabase database,String sql) throws FileNotFoundException
	{
		Lock l = new ReentrantLock();
		l.lock();
		SQLiteDatabase dbcopy = null;
		
		PrintWriter pw = new PrintWriter(new File("ABC"));
			if(database.inTransaction())
			{
				database.setTransactionSuccessful();
				database.endTransaction();
			}
		
			List<Integer> a = new ArrayList<>();
			try
			{
				
				database.compileStatement("abc");
				Iterator<Integer> t = a.iterator();
				/*
					while(true)
					{
						if(!t.hasNext())
						{
							database.setTransactionSuccessful();
							database.endTransaction();
							database.close();
							return;
							
						}
						database.compileStatement("abc");
						database.compileStatement("abc");
						syn();
						database.compileStatement("abc");
						database.compileStatement("abc");
						
					}
					*/
				
				
				database.beginTransaction();
				dbcopy = database;
				while(true)
					{
						if(!t.hasNext())
						{
							database.setTransactionSuccessful();
							database.endTransaction();
							dbcopy = null;
							database.close();
							return;
							
						}
						database.compileStatement("abc");
						database.compileStatement("abc");
						syn();
						database.compileStatement("abc");
						database.compileStatement("abc");
						
					}
					
				

			}
			finally
			{
				if(dbcopy!=null)
					dbcopy.endTransaction();
				
			}
			//database.isDatabaseIntegrityOk();
		
	}
	private void syn() {
		// TODO Auto-generated method stub
		
	}
	public void test2(SQLiteDatabase database,String sql)
	{

		


			database.isOpen();




	}
}
