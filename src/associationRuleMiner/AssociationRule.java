package associationRuleMiner;

import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// This is a class that represents an association rule object
// It makes things like computing the time signature of a rule easier.

public class AssociationRule implements Comparable<AssociationRule>{
	
	Set<Integer> antecedentIDs;
	int consequenceID;
	double ruleStrength;
	FrequentItemSetDB fItemDB;
	int ruleID;
	int clusterID;
	static int numRules =0;
	// Repo objects
	List<RepoData> validMatches;
	List<RepoData> violations;
	// Temporal histogram 
	CommitDateRanges dRanges;
    int [] validMatchesOverTime; // Map from date range ID to count of number of matches
	int [] violationsOverTime; // Map from date range ID to count of number of matches
	boolean [] isTrending;
	
	public void setDateRanges(CommitDateRanges d){
		assert(dRanges == null && validMatchesOverTime== null && violationsOverTime==null);
		this.dRanges = d;
		int n = dRanges.getNumBins();
		this.validMatchesOverTime = new int[n];
		this.violationsOverTime= new int[n];
		this.isTrending = new boolean[n];
		for (int i = 0; i < n; ++i){
			this.validMatchesOverTime[i] = this.violationsOverTime[i]=0;
			isTrending[i]=false;
		}
	}
	
	public AssociationRule(Set<Integer> antsID, int cID, double strength, FrequentItemSetDB fidb){
		antecedentIDs = new TreeSet<Integer>(antsID);
		consequenceID = cID;
		ruleStrength = strength;
		fItemDB = fidb;
		ruleID = numRules++;
		validMatches = new ArrayList<RepoData>();
		violations = new ArrayList<RepoData>();
		dRanges=null;
		validMatchesOverTime=null;
		violationsOverTime = null;
		dRanges=null;
		isTrending=null;
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
		fStream.format("<h2> Valid Matches to Association Rules (%d) </h2>",validMatches.size());
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
		    printMatchesData(fstream);
		    printViolationData(fstream);
		    printTailInfo(fstream);
		    fstream.close();
		}
		catch (IOException e)
		{
		    System.err.println("Error: " + e.getMessage());
		}
		
	}

	private void printViolationData(PrintWriter fStream) {
		fStream.format("<h2> Violations to Association Rule (%d) </h2>",violations.size());
		fStream.println("<table> \n <tbody> \n ");
		for (RepoData r: violations){
			String s = r.getCommitURL();
			fStream.println(s);
		}
		fStream.println("</tbody></table>");
		
	}

	public JSONObject dumpJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("ruleID", this.ruleID);
		JSONArray antArray = new JSONArray();
		for(int i: antecedentIDs){
			antArray.put(fItemDB.getFeatureNameNoID(i));
		}
		obj.put("antecedents", antArray);
		obj.put("consequent",fItemDB.getFeatureNameNoID(consequenceID));
		obj.put("ruleStrength", ruleStrength);
		JSONArray matchingRepoIDs = new JSONArray();
		for (RepoData r: validMatches){
			matchingRepoIDs.put(r.id);
		}
		obj.put("matchingRepoIDs", matchingRepoIDs);
		JSONArray vRepoIDs = new JSONArray();
		for (RepoData r: violations){
			vRepoIDs.put(r.id);
		}
		obj.put("violatingRepoIDs", vRepoIDs);
		JSONArray dLabels = dRanges.getDateLabelsJSON();
		obj.put("dateLabels", dLabels);
		JSONArray matchesOverTime = this.getMatchesOverTimeJSON();
		obj.put("matchesOverTime", matchesOverTime);
		obj.put("isTrending", this.isTrending);
		return obj;
	}

	private JSONArray getMatchesOverTimeJSON() throws JSONException {
		JSONArray a = new JSONArray();
		int n = dRanges.getNumBins();
		for(int i=0; i < n; ++i){
			double fr = (double) validMatchesOverTime[i]/(double) dRanges.getNumCommitsForRange(i);
			a.put(fr);
		}
		return a;
	}
	
	public boolean isTrendingRule() {
		// Need a criterion for whether something is trending
		// Compute the mean and standard deviation of first n-2 bins.
		// Check if any of the last two bins have values at least 2 standard deviations above the mean.
		//  If so yes, otherwise no.
		int n = dRanges.getNumBins();
		boolean overallTrend = false;
		double [] fr = new double[n];
		double meanValue=0.0;
		double sdev = 0.0;
		double nItems=0;
		for(int i=0; i < n; ++i){
			fr[i] = (double) validMatchesOverTime[i]/(double) dRanges.getNumCommitsForRange(i);
			if (i > 0 && i < n -2 ){
				meanValue = meanValue + fr[i];
				sdev = sdev + fr[i]*fr[i];
				nItems = nItems +1;
			}
		}
		assert(nItems > 0);
		meanValue = meanValue / (double) nItems;
		sdev = Math.sqrt(sdev/nItems - meanValue * meanValue);
		for (int j=1; j < n; ++j){
			if (fr[j] >= meanValue + AlgoParameters.nSigmasForTrending * sdev ){
				isTrending[j]=true;
				if ( j > 1)
					overallTrend = true;
			}
		}
		return overallTrend;
	}

	public int numViolations() {
		return violations.size();
	}
	
	public int numMatches(){
		return validMatches.size();
		
	}

	public boolean isFeatureIDInvolved(int j){
		return (j == this.consequenceID ) || (this.antecedentIDs.contains(j));
				
	}
	public boolean intersectsFrequentItemSet(AssociationRule a2) {
		if (a2.isFeatureIDInvolved(this.consequenceID)){
			return true;
		}
		
		for (int i: this.antecedentIDs){
			if (a2.isFeatureIDInvolved(i))
				return true;
		}
		
		return false;
	}

	@Override
	public int compareTo(AssociationRule o) {
		if (o.clusterID != this.clusterID)
			return (o.clusterID < this.clusterID)?1:-1;
		else {
			if (o.numMatches() != this.numMatches())
				return (o.numMatches() > this.numMatches()) ? 1: -1;
			
			if (o.numViolations() != this.numViolations())
				return (o.numViolations() > this.numViolations())? 1:-1;
			
			if (o.ruleID > this.ruleID)
				return (o.ruleID > this.ruleID)? 1:-1;
		}
		
		return 0;
	}
	
	
}
