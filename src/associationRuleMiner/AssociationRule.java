package associationRuleMiner;

import java.util.Set;
import java.util.TreeSet;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
		String str1 = String.format("Rule ID: %d  (strength %.2f) \n", ruleID, ruleStrength);
		String str2 = "( ";
		String delim = "";
		for(int i:antecedentIDs){
			String fName = fItemDB.getFeatureName(i);
			str2 = str2 + delim + fName ;
			delim = ", ";
		}
		str2 = str2 + ") ";
		str2 = str2 + " => " + fItemDB.getFeatureName(consequenceID) + "\n";
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
	
	public void printHeaderInfo(PrintWriter fStream){
		fStream.format("<html> \n <head> \n <title> Association Rule # %d </title> \n <script src=\"%s\"></script> \n </head>\n <body>\n",this.ruleID, AlgoParameters.chartJSPath);
		fStream.format("<h1> %s </h1>\n", toString());
		fStream.println("<div style=\"width: 50%\"> <canvas id=\"myCanvas\" height=\"450\" width=\"600\"> </canvas></div>");
	} 
	public void printChartDataObject(PrintWriter fStream){
		fStream.println("<script>");
		fStream.println("var barChartData = { \n");
		String dateRangeLabels= dRanges.getDateLabels();
		fStream.format("\t labels: [%s], \n",dateRangeLabels);
		fStream.println("\t datasets : [ ");
		fStream.println("\t\t{");
		fStream.println("\t\t\t label : \"percentage of matching comits\", " );
		fStream.println( "\t\t\t fillColor : \"rgba(220,100,120,0.8)\", \n"+
				"\t\t\t strokeColor : \"rgba(220,220,220,0.8)\", \n"+
				"\t\t\t highlightFill: \"rgba(220,220,220,0.75)\",\n"+
				"\t\t\t highlightStroke: \"rgba(220,220,220,1)\",\n"
				);
		String dataString = getDataAsString();
		fStream.format("\t data: [ %s ] \n", dataString);
		fStream.println("\t\t }");
		fStream.println("\t\t ]");
		fStream.println("}\n");
		fStream.println("window.onload = function(){ \n "+
			"var ctx = document.getElementById(\"myCanvas\").getContext(\"2d\"); \n"+
			"window.myBar = new Chart(ctx).Bar(barChartData, { \n"+
			"	responsive : true\n" +
			"});  \n } \n");
		fStream.println("</script>");
		
	}
	private String getDataAsString() {
		int n = dRanges.getNumBins();
		String delim="";
		String retStr = "";
		for(int i=0; i < n; ++i){
			double fr = (double) validMatchesOverTime[i]/(double) dRanges.getNumCommitsForRange(i);
			retStr = retStr + delim + "\""+ (100*fr) +"\"";
			delim = ", ";
		}
		return retStr;
	}

	public void printTailInfo(PrintWriter fStream){
		fStream.println("</body>\n </html>");
	}
	
	public void printMatchesData(PrintWriter fStream){
		fStream.println("<h2> Valid Matches to Association Rules </h2>");
		fStream.println("<table> \n <tbody> \n ");
		for (RepoData r: validMatches){
			String s = r.getCommitURL();
			fStream.println(s);
		}
		fStream.println("</tbody></table>");
	}
	public void htmlPrintRuleWithChart(String fileStem) {
		try  
		{
		    PrintWriter fstream = new PrintWriter(fileStem+ruleID+".html", "UTF-8"); //true tells to append data.
		    printHeaderInfo(fstream);
		    printChartDataObject(fstream);
		    printViolationData(fstream);
		    printMatchesData(fstream);
		    printTailInfo(fstream);
		    fstream.close();
		}
		catch (IOException e)
		{
		    System.err.println("Error: " + e.getMessage());
		}
		
	}

	private void printViolationData(PrintWriter fStream) {
		fStream.println("<h2> Violations to Association Rule </h2>");
		fStream.println("<table> \n <tbody> \n ");
		for (RepoData r: violations){
			String s = r.getCommitURL();
			fStream.println(s);
		}
		fStream.println("</tbody></table>");
		
	}
	
	
}
