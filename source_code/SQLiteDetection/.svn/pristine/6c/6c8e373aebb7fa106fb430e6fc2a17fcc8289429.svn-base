package sql.string;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class ClasslistMain {

	public static void main(String[] args) throws FileNotFoundException {
		String folderName = "/home/yingjun/Documents/sqlite/ClasslistGenerator/sootOutput";
		File folder = new File(folderName);
		

		File[] listOfFiles = folder.listFiles();
		
		PrintWriter pw = new PrintWriter(new File("/home/yingjun/Documents/sqlite/ClasslistGenerator/classlist.txt"));
		for(int i = 0; i < listOfFiles.length; i++){
			
		    pw.println(listOfFiles[i].getName().replaceAll(".jimple", ""));
		   
		    }
		
		pw.flush();
		pw.close();

	}

}
