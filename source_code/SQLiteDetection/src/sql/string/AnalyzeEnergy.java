package sql.string;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

public class AnalyzeEnergy {

	static Map<String, List<List<Long>> > result = new HashMap<>();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File file = new File(args[0]);
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory()&&!name.contains("android--1");
		  }
		});
		int[] ids = new int [directories.length];
		for(int i = 0; i < directories.length; i++)
			ids[i] = Integer.parseInt(directories[i].replace("App", ""));
		Arrays.sort(ids);
		for(int i = 0; i < directories.length; i++)
			directories[i] = "App" + ids[i];
		//System.out.println(Arrays.toString(directories));
		List<String> outputOrder = new ArrayList<>();
		for(String app: directories)
		{
			File opt = new File(args[0]+"/"+app+"/data/opt");
		
			String[] dirOpt = opt.list(new FilenameFilter() {
				  @Override
				  public boolean accept(File current, String name) {
				    return new File(current, name).isDirectory();
				  }
				});
			Arrays.sort(dirOpt, new Comparator<String>()
				    {
				      public int compare(String s1, String s2)
				      {
				        return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
				      }
				    });

			//appName+opt, List<List<>>
			//appName+unopt
			
			
			//System.out.println(app+"---------------------Unoptimized version---------------------");
			//System.out.println(Arrays.toString(dirOpt));
			for(String inputNum : dirOpt)
			{
				calculateEnergy(args[0]+"/"+app+"/data/unopt"+"/"+inputNum);
				outputOrder.add(args[0]+"/"+app+"/data/unopt"+"/"+inputNum);
				
			}
			//System.out.println(app+"---------------------Optimized version---------------------");
			
			for(String inputNum : dirOpt)
			{
				calculateEnergy(args[0]+"/"+app+"/data/opt"+"/"+inputNum);
				outputOrder.add(args[0]+"/"+app+"/data/opt"+"/"+inputNum);
			}
			
		}
		MannWhitneyUTest mt = new MannWhitneyUTest();
		for(String appInput : outputOrder)
		{
			if(appInput.contains("unopt"))
			{
			
				String appInputOpt = appInput.replace("unopt", "opt");
				for(int i = 0; i < 4; i++)
				{
					if(i == 0)
						System.out.println("Loop Time\n" + appInput);
					if(i == 1)
						System.out.println("Loop Energy\n " + appInput);
					if(i == 2)
						System.out.println("App Time\n" + appInput);
					if(i == 3)
						System.out.println("App Energy\n" + appInput);
					if(result.get(appInput)== null)
						continue;
					double[] unopt = new double[15];
					double[] opt = new double[15];
					int j = 0;
					
					for(long data : result.get(appInput).get(i))
					{
		
						//if(appInput.contains("App802"))
						System.out.println(data);
						unopt[j++] = data;
						
					}
					j = 0;
					
					System.out.println(appInputOpt);
					for(long data : result.get(appInputOpt).get(i))
					{
						//if(appInput.contains("App802"))
						System.out.println(data);
						opt[j++] = data;
	
					}

					System.out.println("P-VALUE:"+mt.mannWhitneyUTest(unopt, opt));
				}
			}
		}
		
		/*
		for(String appInput : outputOrder)
		{
			if(appInput.contains("unopt") )
			{
			
				if(appInput.endsWith("1"))
				{
				if(appInput.contains("App69"))
					System.out.print("\\apppay");
				if(appInput.contains("App180"))
					System.out.print("\\appquiz");
				if(appInput.contains("App219"))
					System.out.print("\\appmani");
				if(appInput.contains("App567"))
					System.out.print("\\appcata");
				if(appInput.contains("App802"))
					System.out.print("\\appnews");
				if(appInput.contains("App1447"))
					System.out.print("\\appride");
				}
				String appInputOpt = appInput.replace("unopt", "opt");
				StringBuilder out = new StringBuilder();
				for(int i = 0; i < 4; i++)
				{
			
					
					double[] unopt = new double[15];
					double[] opt = new double[15];
					int j = 0;
					
					for(long data : result.get(appInput).get(i))
					{
		
						//if(appInput.contains("App802"))
						//System.out.println(data);
						unopt[j++] = data;
						
					}
					j = 0;
					
					//System.out.println(appInputOpt);
					for(long data : result.get(appInputOpt).get(i))
					{
						//if(appInput.contains("App802"))
						//	System.out.println(data);
						opt[j++] = data;
	
					}
					
					Arrays.sort(unopt);
					Arrays.sort(opt);
					if(i % 2 == 0)
						out.append(" & "+ (long)unopt[7] + "/" + (long)opt[7]);
					else
						out.append(" & "+ (long)(unopt[7]/1000) + "/" + (long)(opt[7]/1000));
					if(i == 3)
					{
						out.append("\\\\");
						System.out.println(out);
					}
				}
			}
		}
		*/

		
	}
	public static void calculateEnergy(String path)
	{
		//System.out.println("File path:"+path);
		String daq_path = path+"/energy";
		EnergyDataBase database=new EnergyDataBase(daq_path);
		 
		try {
 
			String line;
 
			BufferedReader br = new BufferedReader(new FileReader(path+"/logcat.txt"));
 
			long start=0, end = 0, startLoop=0, endLoop=0;
			int countForApp219 = 0;
			boolean startRead = false;
			boolean endRead = false;
			boolean target = false;
			int endCount = 0;
			while ((line = br.readLine()) != null) {

				if(line.contains("StartAction"))
				{
					start = Long.parseLong(line.split(": StartAction")[1]);
					

					startRead = false;	
				}
				else if(line.contains("StartLoop"))
				{

					
					//App802 has nested loops, both inner loop and outer loop are optimized has timestamps, we evaluate the outer loop 
					if(path.contains("App802"))
					{
						if(!startRead)
						{
							startLoop = Long.parseLong(line.split(": StartLoop")[1]);
							startRead = true;
						}
					}
					else
						startLoop = Long.parseLong(line.split(": StartLoop")[1]);
				}
				else if(line.contains("EndLoop"))
				{
					endLoop = Long.parseLong(line.split(": EndLoop")[1]);
				}
				else if(line.contains("EndAction"))
				{	
					endCount++;
					end = Long.parseLong(line.split(": EndAction")[1]);
					
					if(startLoop>=start&&endLoop<=end)
					{
						long loopTime = endLoop - startLoop;
						long loopEnergy = (int)database.QueryMilliEnergy(startLoop,endLoop);
						long appTime = end - start;
						long appEnergy = (int)database.QueryMilliEnergy(start,end);
						if(result.get(path) == null)
						{
							List<List<Long>> data = new ArrayList<>();
							data.add(new ArrayList<Long>());
							data.add(new ArrayList<Long>());
							data.add(new ArrayList<Long>());
							data.add(new ArrayList<Long>());
							result.put(path, data);
						}
						//System.out.println(delta);
						if(path.contains("App69"))
						{
							//Some timestamps that are not related to the evaluated loop and event handler are also printed.
							//This is because our detection and transformation had applied to all the AHPs found in an app.
							//The threshold is used for filtering
							if(endCount % 3 == 0)
							{
								result.get(path).get(0).add(loopTime);
								result.get(path).get(1).add(loopEnergy);
								result.get(path).get(2).add(appTime);
								result.get(path).get(3).add(appEnergy);
							
								//System.out.println(appEnergy);
								/*
								System.out.println("Loop time:"+(endLoop-startLoop));
								System.out.println("Loop energy:"+(int)database.QueryMilliEnergy(startLoop,endLoop));
								System.out.println("App time:"+(end-start));
								System.out.println("App energy:"+(int)database.QueryMilliEnergy(start,end));
								System.out.println();
								*/
							}
						}
						else
						{
							//the logs for App219 was stored in the same file, split it for the convenience of verifying
							if(path.contains("App219"))
							{
								//if(countForApp219%5==0)
								//	System.out.println("---------------");
								countForApp219++;
							}
							//System.out.println(loopEnergy);
							result.get(path).get(0).add(loopTime);
							result.get(path).get(1).add(loopEnergy);
							result.get(path).get(2).add(appTime);
							result.get(path).get(3).add(appEnergy);
							/*
							System.out.println("Loop time:"+(endLoop-startLoop));
							System.out.println("Loop energy:"+(int)database.QueryMilliEnergy(startLoop,endLoop));
							System.out.println("App time:"+(end-start));
							System.out.println("App energy:"+(int)database.QueryMilliEnergy(start,end));
							System.out.println();
							*/
							
						}
					}
					
				}

			}
			
			br.close();
 
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		
		
	}
}
