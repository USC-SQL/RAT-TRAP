import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class DataCollection {

	/*
	I/System.out( 1759): Block Starts: 1434479186084
	I/System.out( 1759): Start counter: 1
	D/audio_hw_primary(  190): out_set_parameters: enter: usecase(1: low-latency-playback) kvpairs: routing=2
	I/System.out( 1759): End counter: 0 1434479186102
	I/System.out( 1759): Block Ends: 1434479186102
*/
	public static void main(String[] args) throws IOException {


		collectInfo(args[0]);

		System.out.println("-----------Results in section 6.4-----------");
		System.out.println("See the PeformanceEvaluation.ods\n");
		
		collectTime(args[1]);

		
	}

	public static void collectInfo(String file) throws IOException
	{
		
			BufferedReader br = new BufferedReader(new FileReader(new File(file)));
			String line = br.readLine();
					
			String app = null;


			int numAHP = 0;
			int numHP = 0;
			int numSAHP = 0;
			int numDBL = 0;
			int numAHPRelatedToLock = 0;
			Map<Integer,Integer> length = new HashMap<>();
			Map<Integer,Integer> lengthAHP = new HashMap<>();
			int totalAHPCallChain = 0;
			int maxAHPCallChain = 0;
			List<Integer> relevantCode = new ArrayList<>();
			double pruneCCGTotal = 0;
			int numApps = 0;
			int numDBApps = 0;
			int numHPApps = 0;
			int numAHPApps = 0;
			int currentAHP = 0;
			int currentSAHP = 0;
			int currentHP = 0;
			List<Integer> totalCode = new ArrayList<>();
			List<Integer> AHPNums = new ArrayList<>();
			boolean isAHP = false;
			
	        while (line != null) {
	        	if(line.startsWith("/App"))
	        	{
	        		app = line;

	        		
	        		numApps++;	        		
	        		/*
	        		if(numApps == 90)
	        		{
	        			System.out.println(line);
	        			break;
	        		}
	        		*/
	        	}
	        	else if(line.startsWith("Exist Repair:true"))
	        		numAHPApps++;
	        	else if(line.startsWith("Exist Opp:true"))
	        		numHPApps++;
	        	else if(line.startsWith("Exist SQL in User Code:true"))
	        		numDBApps++;
	        	else if(line.startsWith("Num of HP"))
	        	{
	        		currentHP = Integer.parseInt(line.split(":")[1]);
	        		numHP += Integer.parseInt(line.split(":")[1]);

	
	        	}
	        	else if(line.startsWith("Num of AHP"))
	        	{
	        		numAHP += Integer.parseInt(line.split(":")[1]);
	        		currentAHP = Integer.parseInt(line.split(":")[1]);
	        		
	        		if(currentAHP!=0)
	        			AHPNums.add(currentAHP);
	        		//	System.out.println(currentAHP);
	        	}
	        	else if(line.startsWith("Num of in scope HP"))
	        	{

	        		numSAHP += Integer.parseInt(line.split(":")[1]);
	        	}
	        	else if(line.startsWith("Num of DB in loop"))
	        		numDBL += Integer.parseInt(line.split(":")[1]);
	        	else if(line.startsWith("@HP@"))
	        		isAHP = false;
	        	else if(line.startsWith("@AHP@"))
	        		isAHP = true;
	        	else if(line.startsWith("Target loop nests a lock or is nested in a lock:true"))
	        	{
	        		if(isAHP&&!app.contains("/App562/"))
	        			numAHPRelatedToLock+=1;
	        	}

	        	else if(line.startsWith("Line of target bytecodes"))
	        	{
	        		int count = Integer.parseInt(line.split(":")[1]);
	        		relevantCode.add(count);
	        	}
	        	else if(line.startsWith("CCG prune out code"))
	        	{
	        		if(!line.contains("NaN"))
	        		{
	        			double percent = Double.parseDouble(line.split(":")[1].replaceAll("%", ""));
	        			pruneCCGTotal += percent;
	        		}
	        	}
	        	else if(line.startsWith("Num of bytecodes in program:"))
	        	{
	        		totalCode.add(Integer.parseInt(line.split(":")[1]));
	        	}
	         	else if(line.startsWith("Length of call chain between db operation and call graph entry"))
	        	{
	         		if(isAHP&&!app.contains("/App562/"))
	        		{
	         			int chain = Integer.parseInt(line.split(":")[1]);
	         			totalAHPCallChain += chain;
	         			if(chain > maxAHPCallChain)
	         				maxAHPCallChain = chain;
	        		}
	        	}
	        	else if(line.startsWith("Length of call chain between db operation and loop header"))
	        	{
	        		//do not include the outlier
	        		if(!app.contains("/App562/"))
	        		{
		        		int l = Integer.parseInt(line.split(":")[1]);
	
		        		if(length.containsKey(l))
		        			length.put(l, length.get(l)+1);
		        		else
		        			length.put(l, 1);
		        		if(isAHP)
		        		{
			        		int l1 = Integer.parseInt(line.split(":")[1]);
			        		if(lengthAHP.containsKey(l1))
			        			lengthAHP.put(l1, lengthAHP.get(l1)+1);
			        		else
			        			lengthAHP.put(l1, 1);
		        		}
	        		}
	        		
	        	}

	        	line = br.readLine();
	        }
	        br.close();
	        
	        //App 69 has 20134 relevant byte code
	        int largerThanApp69  = 0;
	        for(int i = 0; i < relevantCode.size(); i++)
	        {
	        	if(relevantCode.get(i) > 20134)
	        		largerThanApp69++;
	        }
	        Collections.sort(totalCode);
	        int totalApp = 0;
	        int[] dis = {1000,10000,100000};
	        Integer [] disNum = new Integer[4];
	        for(int i = 0; i < 4; i++)
	        	disNum[i] = 0;
 	        for(int i = 0; i < totalCode.size();i++)
	        {
	        	int size = totalCode.get(i);
	        	if(size!=0)
	        		totalApp++;
	        	for(int j = 0; j < 2; j++)
	        	{
	        		if(size >= dis[j] && size < dis[j+1])
	        			disNum[j+1] ++;
	        	}
	        	if(size < dis[0])
	        		disNum[0]++;
	        	if(size >= dis[2])
	        		disNum[3]++;
	        			
	        }
 	        System.out.println("-----------Relevant bytecode in section 4.2 and section 6.5-----------");
 	        System.out.println("CCG prune out in average:"+pruneCCGTotal*1.0/totalApp+"%");
 	        System.out.println("Relevant Bytecode larger than App69:"+largerThanApp69*1.0/relevantCode.size()*100+"%");
 		    System.out.println();

	        System.out.println("-----------Results in section 6.2-----------");
	       	System.out.println("Distribution of bytecode in program 0-10000,10000-100000, 100000-inf:"
	        +(disNum[0]+disNum[1])*100/totalApp+"%,"+(disNum[2])*100/totalApp+"%,"+(disNum[3])*100/totalApp+"%");
	        System.out.println();
	 
	        System.out.println("-----------Results in section 6.3-----------");
	        System.out.println("Total num of apps:"+numApps+"\n"
	        		+ "Total num of apps with db operations:"+numDBApps+"\n"
	        		+ "Total num of apps with ARAT:"+numAHPApps +"\n"
	        		+ "Percentage of apps with ARAT over all apps:"+numAHPApps*100.0/numApps+"%\n"
	        		+ "Percentage of apps with ARAT over apps with db operations:"+numAHPApps*100.0/numDBApps+"%");
	        
	        
	        System.out.println("Num of ARAT:"+numAHP+"   after excluding outlier:"+(numAHP-4220));
	        
	        System.out.println("Average number of ARAT:"+(numAHP-4220)*1.0/(numAHPApps-1));
	        Collections.sort(AHPNums);
	        
	        System.out.println("Median number of ARAT:"+AHPNums.get(numAHPApps/2));
	        System.out.println("ARAT with loop related to locks:"+numAHPRelatedToLock+" Percentage:"+numAHPRelatedToLock*1.0/(numAHP-4220)*100+"%");
	        System.out.println("Percentage of in-scope RATs that may cause deadlock:"
	        		+ (numSAHP-numAHP)*1.0/(numSAHP-4220)*100+"%");
	        

	        //outlier 
	        //lengthAHP.put(2, lengthAHP.get(2)-4220);
	        int total = 0;
	        int zero = length.get(0);
	        
	        int totalAHP = 0;
	        int zeroAHP = lengthAHP.get(0);

	        for(Entry<Integer,Integer> en : lengthAHP.entrySet())
	        {
	        	totalAHP += en.getValue();
	        }
	        System.out.println("Call path length between db operation and loop header:"+lengthAHP);	        
	        System.out.println("AHP with call path length >=1:"+(1-zeroAHP*1.0/totalAHP)*100+"%");

	        System.out.println("Average call path length between db operation and call graph entry:"+totalAHPCallChain*1.0/(numAHP-4220));
	        System.out.println("Max call path length between db operation and call graph entry:"+maxAHPCallChain);	        
	        System.out.println();

	}
	public static void collectTime(String path) throws IOException
	{

		int times = 15;
		 BufferedReader br = new BufferedReader(new FileReader(path));

		 Map<String,Double[]> map = new HashMap<>();
		 String line = br.readLine();
		 String appName = null;
	        while (line != null) {
	           // if(line.contains("Batch"))
	           // 	System.out.println(listOfFiles[i].getName());
	        	
	        	if(line.startsWith("/App"))
	        	{
	       
	        		appName = line;
	        		if(map.get(appName)==null)
	        		{
	        		Double[] d = new Double[4];
	        		Arrays.fill(d, 0.0);
	 
	        		map.put(appName, d);
	        		}
	        	}
	        	else if(line.startsWith("Soot:"))
	        	{
	        		int time = Integer.parseInt(line.split(":")[1].replaceAll("ms", ""));
	        		
	        		map.get(appName)[0] += time;
	        	}

	        	else if(line.startsWith("Analysis:"))
	        	{
	        		int time = Integer.parseInt(line.split(":")[1].replaceAll("ms", ""));
	        		map.get(appName)[1] += time;
	        	}
	        	else if(line.startsWith("Rewrite:"))
	        	{
	        		int time = Integer.parseInt(line.split(":")[1].replaceAll("ms", ""));
	        		map.get(appName)[2] += time;
	        	}
	        	else if(line.startsWith("Total:"))
	        	{
	        		int time = Integer.parseInt(line.split(":")[1].replaceAll("ms", ""));
	        		map.get(appName)[3] += time;
	        	}
	            line = br.readLine();
	        }
	        
	       br.close();
	       for(String app : map.keySet())
	       {
	    	   //System.out.println(app);
	    	  
	    	   for(int i = 0; i < 4; i++)
	    	   {
	    		   //System.out.println(map.get(app)[i]);
	    		   map.get(app)[i] =  map.get(app)[i] * 1.0 / times;
	    	   }
	       }
	       
	       double total = 0;
	       double sootPercent = 0;
	  
	       double all[] = new double[6]; 
	       int j = 0;
	       System.out.println("-----------Results in section 6.5-----------");
	       for(String app : map.keySet())
	       {
	    	   System.out.println(app);
	    	   System.out.println("Soot:"+(map.get(app)[0]/1000)+"s");
	    	   System.out.println("Analysis:"+(map.get(app)[1]/1000)+"s");
	    	   System.out.println("Rewrite:"+(map.get(app)[2]/1000)+"s");
	    	   System.out.println("Total:"+(map.get(app)[3]/1000)+"s");
	    	   System.out.println();
	    	   all[j++] = map.get(app)[3];
	    	   total += map.get(app)[3];
	    	   sootPercent += map.get(app)[0] / map.get(app)[3];
	       }
	       Arrays.sort(all);
	       System.out.println("Min Total:"+(int)(all[0]/1000)+"s");
	       System.out.println("Max Total:"+(int)(all[map.keySet().size()-1]/1000)+"s");
	       System.out.println("Median Total:"+(int)((all[2]+all[3])/2000)+"s");
	       System.out.println("Average Total:"+(int)(total/1000)/map.keySet().size()+"s");
	       
	       
	       System.out.println("Average Percentage of time spent on Soot:"+(int)(sootPercent*100.0)/map.keySet().size()+"%");
	
	}

}

