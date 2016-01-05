package associationRuleMiner;

import java.util.Set;
import java.util.TreeSet;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// This is a class that represents an association rule object
// It makes things like computing the time signature of a rule easier.

public class AssociationRule {
	Set<Integer> antecedentIDs;
	int consequenceID;
	double ruleStrength;
	FrequentItemSetDB fItemDB;
	int ruleID;
	static int numRules =0;
	// Repo objects
	List<RepoData> validMatches;
	List<RepoData> violations;
	// Temporal histogram 
	CommitDateRanges dRanges;
    int [] validMatchesOverTime; // Map from date range ID to count of number of matches
	int [] violationsOverTime; // Map from date range ID to count of number of matches
	
	public void setDateRanges(CommitDateRanges d){
		assert(dRanges == null && validMatchesOverTime== null && violationsOverTime==null);
		this.dRanges = d;
		int n = dRanges.getNumBins();
		this.validMatchesOverTime = new int[n];
		this.violationsOverTime= new int[n];
		for (int i = 0; i < n; ++i){
			this.validMatchesOverTime[i] = this.violationsOverTime[i]=0;
		}
	}
	
	public AssociationRule(Set<Integer> antsID, int cID, double strength, FrequentItemSetDB fidb){
		antecedentIDs = new TreeSet<Integer>(antsID);
		consequenceID = cID;
		ruleStrength = strength;
		fItemDB = fidb;
		ruleID = numRules++;
		validMatches = new ArrayList();
		violations = new ArrayList();
		dRanges=null;
		validMatchesOverTime=null;
		violationsOverTime = null;
		dRanges=null;
	}
	
	public String toString(){
		String str="";
		String str1 = String.format("Association Rule ID: %d with strength %f \n", ruleID, ruleStrength);
		String str2 = "{ ";
		for(int i:antecedentIDs){
			String fName = fItemDB.getFeatureName(i);
			str2 = str2 + fName + ", ";
		}
		str2 = str2 + "} ";
		str2 = str2 + " ===> " + fItemDB.getFeatureName(consequenceID) + "\n";
		str = str1 + str2;
		return str;
	}
	public void displayTimeSignature(){
		if (dRanges == null) return;
		assert(dRanges != null && violationsOverTime != null && validMatchesOverTime != null);
		int n = dRanges.getNumBins();
		System.out.println("-------------------");
		System.out.println("Date,  Percent Matches");
		for (int i = 0; i < n; ++i){
			double fr = (double) validMatchesOverTime[i]/(double) dRanges.getNumCommitsForRange(i);
			
			System.out.format("%s, %3.2f",dRanges.getBinDate(i) , fr*100);
			if (violationsOverTime[i] > 0){
				System.out.println( "  //" + violationsOverTime[i] + " rule violations");
			} else {
				System.out.println("  // No violations");
			}
		}
	}
	public boolean ruleAntecedentMatch(RepoData r){
		List<Integer> idxList = r.indices;
		for (int id: antecedentIDs){
			if (!idxList.contains(id))
				return false;
		}
		return true;
	}
	
	public void evaluateRuleAgainstRepoData(RepoData r){
		if (ruleAntecedentMatch (r)){
			List<Integer> idxList = r.indices;
			//long rTime = r.getTime();
			//int i = dRanges.getDateBin(rTime);
			int i = r.getCommitBinID();
			if (idxList.contains(consequenceID)){
				validMatchesOverTime[i]++;
				validMatches.add(r);
			} else {
				violationsOverTime[i]++;
				violations.add(r);
			}
		}
	}
	
	public void htmlPrintRuleWithChart(String fileStem) throws IOException{
		BufferedWriter out = null;
		try  
		{
		    FileWriter fstream = new FileWriter(fileStem+ruleID+".html", false); //true tells to append data.
		    out = new BufferedWriter(fstream);
		    
		}
		catch (IOException e)
		{
		    System.err.println("Error: " + e.getMessage());
		}
		finally
		{
		    if(out != null) {
		        out.close();
		    }
		}
	}
}
