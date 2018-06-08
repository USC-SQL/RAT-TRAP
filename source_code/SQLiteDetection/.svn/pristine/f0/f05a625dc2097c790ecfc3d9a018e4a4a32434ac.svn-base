package sql.string;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
class DataPoint{
	Set<Long> tidpool=new HashSet<Long>();
	double time=0;
	double current=0;
	double voltage=0;
}
class DPComparator implements Comparator<DataPoint> { 
	 

	@Override
	public int compare(DataPoint arg0, DataPoint arg1) {
		// TODO Auto-generated method stub
		if(arg0.time-arg1.time>0)
			return 1;
		if(arg0.time-arg1.time<0)
			return -1;
		return 0;
	} 

} 
public class EnergyDataBase {
	long StartTime=0;
	private ArrayList<DataPoint> datapoints=new ArrayList<DataPoint>();
	private int search(double time)
	{
		if(time<datapoints.get(0).time)
			return 0;
		if(time > datapoints.get(datapoints.size()-1).time)
			return datapoints.size()-1;
		int midpoint=0;
		int start=0;
		int end=datapoints.size()-1;
		//System.out.println(time);
		while(start!=end)
		{
			midpoint=(start+end)/2;
			DataPoint cur=datapoints.get(midpoint);
			DataPoint next=datapoints.get(midpoint+1);
			if(cur.time<=time && next.time> time)
				return midpoint;
			if(cur.time>time)
			{
				end=midpoint-1;
			}
			else{
				start=midpoint+1;
			}
				
		}
		return start;
	}
	public EnergyDataBase(String path)
	{
		FileReader fin=null;
		int i=0;
		double max_time=0;
		double min_time=Double.MAX_VALUE;
		Pattern Timepattern=Pattern.compile("^Unixtime: ([0-9]+)");
		Pattern cntPattern=Pattern.compile("^Sample count :([0-9]+)");
		Pattern SamplePattern=Pattern.compile("^#([0-9]+) ([0-9\\.]+) millisec ([0-9]+) uA ([0-9\\.]+) V");


		try {
			fin = new FileReader(path);
			BufferedReader bufRead = new BufferedReader(fin);
			String line=null;
			String[] feilds;
			try {
				line=bufRead.readLine();
				int nnn=0;
				double energy=0;
				//System.out.println(line);
				int count=0;
				DataPoint cup=new DataPoint();
				long total=0;
				while(line!=null)
				{
					line=bufRead.readLine();
					//System.out.println(line);
					if(line==null)
						break;
					if(line.startsWith("Unixtime"))
					{
						String[] tmp=line.split(" ");
						this.StartTime=Long.parseLong(tmp[1]);
					}
					if(line.startsWith("Sample count :"))
					{
						String[] tmp=line.split(":");
						total=Long.parseLong(tmp[1]);
					}
					if(line.startsWith("#"))
					{
						String[] tmp=line.split(" ");
						//System.out.println(tmp[0]);
						DataPoint p=new DataPoint();
						p.time=Double.parseDouble(tmp[1])+this.StartTime;
						p.current=Double.parseDouble(tmp[3]);
						p.voltage=Double.parseDouble(tmp[5]);
						String indexstr=tmp[0].replace("#", "");
						int index=Integer.parseInt(indexstr);
						if(datapoints.size()!=index)
						{
							System.err.println("Cramped energy data, wrong index!");
						}
						else
						{
							datapoints.add(p);
						}
					}
				}
				if(datapoints.size()!=total)
				{
					
				}

				//System.out.println(max_time);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Data was logged in the sheet only\n");
		}
	}
	public void AddTid(long start, long end,long tid)
	{
		int startindex=(int)(start-this.StartTime)*5;
		int endindex=(int)(start-this.StartTime)*5;
		for(int i=startindex;i<=endindex;i++)
		{
			DataPoint p=datapoints.get(i);
			p.tidpool.add(tid);
			
		}
	}
	public double DebugQueryEnergy(long start, long end, long last)
	{
		int startindex=(int)(start-this.StartTime)*5;
		int endindex=(int)(start-this.StartTime)*5;
		double totalenergy=0;
		if(end-start>5)
		{
			for(int i=startindex;i<=endindex;i++)
			{
				DataPoint p=datapoints.get(i);
				totalenergy+=p.current*p.voltage*0.001*0.2;
				
			}
			return totalenergy;
		}
		else
		{
			DataPoint p1=datapoints.get(startindex);
			DataPoint p2=datapoints.get(endindex);
			double averagepower=(p1.current*p1.voltage+p2.current*p2.voltage)/2;
			double dlast=(double)last/1000000000.0;
			System.out.println(dlast+" "+averagepower);
			totalenergy=dlast*averagepower;
		}
		return totalenergy;
	}
	public double QueryLongEnergy(long start, long end)
	{
		int startindex=(int)(start-this.StartTime)*5;
		int endindex=(int)(end-this.StartTime)*5;
		double totalenergy=0;
			for(int i=startindex;i<=endindex;i++)
			{
				DataPoint p=datapoints.get(i);
				totalenergy+=((p.current*p.voltage*0.0002));//each sample is 0.0002 seconds 

				
			}
			return totalenergy;
	
	}
	public double QueryMilliEnergy(long start, long end)
	{
		int startindex=(int)(start-this.StartTime)*5;
		int endindex=(int)(end-this.StartTime)*5;
		double totalenergy=0;
			for(int i=startindex;i<=endindex;i++)
			{
				DataPoint p=datapoints.get(i);
				double threadnum=(double)p.tidpool.size();
				if(threadnum==0.0)
					threadnum=1.0;
				totalenergy+=((p.current*p.voltage*0.0002)/threadnum);//each sample is 0.0002 seconds 

				
			}
			return totalenergy;
	
	}
	public double QueryEnergy(long start, long end, long nanolast)
	{
		int startindex=(int)(start-this.StartTime)*5;
		int endindex=(int)(end-this.StartTime)*5;
		double totalenergy=0;
		if(end-start>5)
		{
			for(int i=startindex;i<=endindex;i++)
			{
				DataPoint p=datapoints.get(i);
				double threadnum=(double)p.tidpool.size();
				if(threadnum==0.0)
					threadnum=1.0;
				totalenergy+=((p.current*p.voltage*0.0002)/threadnum);//each sample is 0.0002 seconds 

				
			}
			return totalenergy;
		}
		else
		{
			DataPoint p1=datapoints.get(startindex);
			DataPoint p2=datapoints.get(endindex);
			double threadnum=(double)p1.tidpool.size();
			if(threadnum==0.0)
				threadnum=1.0;
			double averagepower=(p1.current*p1.voltage+p2.current*p2.voltage)/2;
			double dnanolast=(double)nanolast/1000000000.0;//tosecond
			totalenergy=(dnanolast*averagepower)/threadnum;

			return 		totalenergy;

		}
	}

	public void display() throws FileNotFoundException
	{
		int i;
		double energy=0;
		double power=0;
		double times,timee;
		PrintWriter pw=new PrintWriter("/home/dingli/plotdata");
		for(i=0;i<datapoints.size()-1;i++)
		{			//System.out.println(datapoints.get(i).cpupower);
			//if(datapoints.get(i).cpupower<0.0000005)
		}
		pw.close();
		times=datapoints.get(0).time;
		System.out.println(times);
		timee=datapoints.get(datapoints.size()-1).time;
		double t=timee-times;
		power=energy/t;
		System.out.println(power);
	}
}
