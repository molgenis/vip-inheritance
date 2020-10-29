# VIP inheritance annotation
## GenemapConverter
A tool to converts the omim 'genemap2.txt' to a tab separated file that can be used by the VEP Inheritance Plugin.
It creates a line for all gene symbols in the input file.
###Usage
```
usage: java -jar genemap-mapper.jar -i <arg> -c <arg> [-o <arg>] [-f]
       [-l] [-p] [-d]
 -i,--input <arg>    Input genemap2.txt location.
 -o,--output <arg>   Output TSV file
 -f,--force          Override the output file if it already exists.
 -d,--debug          Enable debug mode (additional logging).

usage: java -jar genemap-mapper.jar -v
 -v,--version   Print version.
```

###Mapping
|OMIM Inheritance*|Annotation|
|---|---|
|Y-LINKED|YL|
|X-LINKED DOMINANT|XD|
|X-LINKED RECESSIVE|XR|
|X-LINKED|XL|
|AUTOSOMAL RECESSIVE|AR|
|AUTOSOMAL DOMINANT|AD|
|PSEUDOAUTOSOMAL RECESSIVE|PR|
|PSEUDOAUTOSOMAL DOMINANT|PD|
|ISOLATED CASES|IC|
|DIGENIC|DG|
|DIGENIC RECESSIVE|DGR|
|DIGENIC DOMINANT|DGD|
|MITOCHONDRIAL|MT|
|MULTIFACTORIAL|MF|
|SOMATIC MUTATION|SM|
|SOMATIC MOSAICISM|SMM|
|INHERITED CHROMOSOMAL IMBALANCE|ICI|
*:OMIM provisional inheritance modes (starting with ?) are mapped the same as non provisional values.

## Inheritance VEP plugin
A VEP plugin to annotate consequences with inheritance modes based on their gene.
###Installation
Place Inheritance.pm in your vep plugin directory (e.g. '~/.vep/Plugins')
###Usage
```
./vep -i variations.vcf --plugin Inheritance,/FULL_PATH_TO_PREPROCESSED_INHERITANCE_FILE/gene_inheritance_modes.tsv
```
