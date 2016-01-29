package associationRuleMiner;

public class AlgoParameters {
	
	// frequentItemSetFreqCutoff
	// This is a cutoff parameter for regarding an itemset as "frequent".
	// Set this number to be reasonably large so that if a series of methods appear in <frequentItemSetFreqCutOff>
	// commits, then it is deemed statistically significant.
	
	public static final double nSigmasForTrending = 3.5;

	public static int frequentItemSetFreqCutoff=2000;
	
	// associationRuleStrengthCutoff
	// This is a cutoff parameter for the strength of an association rule
	public static double associationRuleStrengthCutoff=0.95;
	public static String repoDataJSONFilename="/Users/macuser/Projects/git/FixrAssociationRuleMining/data/json/clusteringBvCtx.json";
	public static String featuresFilename ="/Users/macuser/Projects/git/FixrAssociationRuleMining/data/feature_types/featType.txt";
	public static String startDate = "01/01/10";
	public static String endDate = "07/01/15";
	public static int nBins = 20;
	public static String chartJSPath="Chart.js";
	public static String fileStem = "rule_";
	public static boolean addedRemovedFeatures = false;
	public static String dateFormatInJSON=  "yyyy-MM-dd kk:mm:ss Z";

	public static boolean ignoreZeroFeature = false;

	public static String jsonFileName = "allData.json";

	public static int nUniqueRepositoriesCutoff = 50;

	public static boolean writeJSON = false;

	public static boolean writeAssociationRuleGraph = true;

	public static String assocDotFileName = "a.dot";
}
