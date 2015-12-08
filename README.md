# FixrAssociationRuleMining
## Author

Primary Author is Sriram S <srirams{at}gmail{dotcom}>

## Brief Description

This code takes a list of bit vectors after features are extracted and performs association rule mining by the way of frequent item sets.

- Computes frequent item sets that are larger than some given threshold cutoff frequency.

- For each frequent item set with two or more item, it considers possible association rules derived from this set. For each association rule antecedent => consequence, it computes the ratio of frequency(antecedent /\ consequent)/frequency(antecedent).


## To Execute

All sources are in src/*.java
Compile them into corresponding class files bin/associationRuleMiner/*.class

run

	cd bin
	java associationRuleMiner/LeMain ../clusteringBv.txt

