package associationRuleMiner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadFeatureDescriptors {
	String fileName;
	String [] featureNames;
	
	public ReadFeatureDescriptors(String f){
		fileName = f;
		featureNames = null;
	}
	
	public void readDataFromFile(){
		try{
			BufferedReader in = new BufferedReader( new FileReader(fileName));
			in.readLine(); // Ignore the first line
			String l2 = in.readLine();
			int n = l2.length();
			assert(l2.charAt(0) == '[' && l2.charAt(n-1) ==']');
			String line = l2.substring(1,n-1);
			featureNames = line.split(",");
			System.out.println("Number of features extracted: " + (featureNames.length));
			in.close();
		} catch (FileNotFoundException e){
			System.out.println("Fatal: file " + fileName + " not found. Check path!");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e){
			System.out.println("Fatal IOException reading file:" +fileName);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public String[] getFeatureNames(){
		return featureNames;
	}
}
