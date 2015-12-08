package associationRuleMiner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class RepoData {
	public int id;
	public String repo;
	public String childHash;
	public String parentHash;
	public String name;
	public List<Integer> indices;
	public List<Boolean> values;
	public RepoData(int myId){
		this.id = myId;
		repo="";
		childHash="";
		parentHash="";
		name="";
		indices=new ArrayList<Integer>(0);
		values=new ArrayList<Boolean>(0);
	}
	public void setRepo(String val) {
		this.repo = val;
		
	}

	public void setChildHash(String val) {
		this.childHash=val;
		
	}

	public void setParentHash(String val) {
		this.parentHash=val;
		
	}

	public void setName(String val) {
		this.name=val;
		
	}

	public void setIndicesString(String val) {
		// List of comma separated indices
		// Split them into fields and parse each field
		if (val.length() <=0 )
			return;
		Pattern p = Pattern.compile("\\s*,\\s*");
		String [] inds = p.split(val);
		for(String s: inds){
			int i = Integer.parseInt(s);
			indices.add(i);
		}
	}
	
	public void setValuesString(String val){
		// List of comma separated indices
		// Split them into fields and parse each field
		if (val.length() <=0)
			return;
		Pattern p = Pattern.compile("\\s*,\\s*");
		String [] inds = p.split(val);
		for(String s: inds){
			if (s.equals("1.0") || s.equals("1")){
				values.add(true);
			} else {
				assert(false); // For the time being let us not permit this
				values.add(false);
			}
		}
	}

	public String toString(){
		String str;
		str = "ID: "+ this.id + "\n";
		str = str+"Repo: " + this.repo + "\n";
		str = str + "Name: " + this.name + "\n";
		str = str + "Indices: (length = "+ indices.size() +") ~~ " + (indices.toString()) + "\n";
		str = str + "Values: " + (values.toString()) + "\n";	
		return str;
	}
	
	public int getMaxIndex() {
		int m = 0;
		for(int j: indices){
			if (j > m)
				m = j;
		}
		return m;
	}
	
	public void addIndexToSet(Set<Integer> s, TreeMap<Integer, TreeSet<Integer> > m){
		for (int j: indices){
			s.add(j);
			TreeSet<Integer> mj = null;
			if (m.containsKey(j)){
				mj = m.get(j);
			} else {
				mj = new TreeSet<Integer>();
			}
			mj.add(this.id);
			m.put(j, mj);
		}
	}
}