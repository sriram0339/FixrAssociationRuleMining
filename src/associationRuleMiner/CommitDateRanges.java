package associationRuleMiner;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CommitDateRanges {
	//Class: CommitDateRanges
	// Splits a range of dates into equally sized bins to help histogram the number of commits of a particular type 
	// for each date range.
	
	Date firstDate; // This is the first date in the range
	Date lastDate; // This is the last date
	int nBins; // Number of bins required
	
	long [] dateRanges; // An array of long integers of size nBins,
	                    // representing the timestamps for start of each date range. 
		                // The timestamp is time elapsed in millisecs since Jan 1, 1970

	int [] numCommitsInRange; // The number of commits that happenend in a date range,
	
	// Constructor: takes start date, end date and number of bins as inputs and 
	// Creates the array of dateRanges. It also initializes the commits.
	
	public CommitDateRanges(Date fDate, Date lDate, int nB){
		firstDate = fDate;
		lastDate = lDate;
		nBins = nB;
		dateRanges = new long[nBins];
		numCommitsInRange = new int[nBins];
		long startTime = fDate.getTime();
		long endTime = lDate.getTime();
		long diff = endTime - startTime;
	
		long step = diff/(long) nBins;
	
		for (int i=0; i < nBins; ++i){
			dateRanges[i] = startTime+  (long) ( (double) i * step ) ;
			numCommitsInRange[i] = 0;
		}
		
		
	}
	// Function: addCommitToBin
	// The argument is the commit_date expressed in terms of milliseconds since epoch.
	// The commit date is added to the bin to update numCommits in range.
	
	public int addCommitToBin(long cTime){
		int i = getDateBin(cTime);
		numCommitsInRange[i] += 1;
		return i;
	}
	
	// Function: getDateBin
	// The argument is a date expressed in terms of milliseconds since epoch
	// The function returns corresponding bin ID.
	// Note that if date is before first date binID returned is 0
	// If date is after the last date binID returned is nBins -1
	
	public int getDateBin(long cTime){
		for (int i = 0; i < nBins; ++i){
			if (cTime <= dateRanges[i]) return i;
		}
		return nBins-1;
	}

	// Function: prettyPrintDateRanges()
	// Prints the date ranges and number of commits in each range.
	public void prettyPrintDateRanges(){
		System.out.println("Date Ranges");
		System.out.println("------------------");
		for (int i = 0; i < nBins; ++i){
			System.out.println(i + " ---> " +  getBinDate(i) + "," + numCommitsInRange[i]);
		}
		System.out.println("------------------");
	}
	
	// Function: getNumBins
	// Get the number of bins
	public int getNumBins(){
		return nBins;
	}

	// Function: getBinDate
	// given a bin id i, calculate the corresponding date for the start of that bin.
	//
	public String getBinDate(int i) {
		assert(i >= 0 && i <nBins);
		Date cDate = new Date (dateRanges[i]);
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		String dateFormatted = formatter.format(cDate);
		return dateFormatted;
	}

	// Function: getNumCommitsForRange
	// Return the number of commits inside a range for a given binID i
	//
	public int getNumCommitsForRange(int i) {
		assert(i >= 0 && i < nBins );
		return numCommitsInRange[i];
	}
	
	// Function: getDateLabels
	// Return a JSON formatted string of date labels for the Chart.js program.
	//
	public String getDateLabels() {
		String delim=", ";
		int i = 0;
		String str = "\"< "+getBinDate(i)+"\"";
		for (i=1; i < nBins-1;++i){
			str = str + delim + "\""+getBinDate(i)+"\"";
		}
		str = str + delim + "\"> "+getBinDate(nBins-1)+"\"";
		return str;
	}
	
	
}
