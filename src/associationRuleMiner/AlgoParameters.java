package associationRuleMiner;

public class AlgoParameters {
	
	// frequentItemSetFreqCutoff
	// This is a cutoff parameter for regarding an itemset as "frequent".
	// Set this number to be reasonably large so that if a series of methods appear in <frequentItemSetFreqCutOff>
	// commits, then it is deemed statistically significant.
	
	public static int frequentItemSetFreqCutoff=2000;
	
	// associationRuleStrengthCutoff
	// This is a cutoff parameter for the strength of an association rule
	public static double associationRuleStrengthCutoff=0.9;
	
	public static String startDate = "01/01/10";
	public static String endDate = "07/01/15";
	public static int nBins = 24;
	public static String chartJSPath="Chart.js";
	public static String fileStem = "rule_";
	public static boolean addedRemovedFeatures = false;
	public static String dateFormatInJSON=  "MM/dd/yy"; //"yyyy-MM-dd kk:mm:ss Z";

	public static boolean ignoreZeroFeature = true;

	public static String jsonFileName = "allData.json";
}
