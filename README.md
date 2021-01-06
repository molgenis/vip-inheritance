# VIP inheritance annotation
## GenemapConverter
A tool to converts the omim 'genemap2.txt' to a tab separated file that can be used by the VEP Inheritance Plugin.
It creates a line for all gene symbols in the input file.
###Usage
```
usage: java -jar genemap-mapper.jar -i <arg> [-o <arg>] [-f] [-d]
 -i,--input <arg>    Input genemap2.txt location.
 -o,--output <arg>   Output TSV file
 -f,--force          Override the output file if it already exists.
 -d,--debug          Enable debug mode (additional logging).

usage: java -jar genemap-mapper.jar -v
 -v,--version   Print version.
```

###Mapping
Currently only values supported by [VIP inheritance matcher](https://github.com/molgenis/vip-inheritance-matcher) are supported.

|OMIM Inheritance*|Annotation|
|---|---|
|X-LINKED DOMINANT|XD|
|X-LINKED RECESSIVE|XR|
|X-LINKED|XL|
|AUTOSOMAL RECESSIVE|AR|
|AUTOSOMAL DOMINANT|AD|
*:OMIM provisional inheritance modes (starting with ?) are mapped the same as non provisional values.

####Unsupported OMIM inheritance values:
PSEUDOAUTOSOMAL RECESSIVE,PSEUDOAUTOSOMAL DOMINANT,ISOLATED CASES,DIGENIC,DIGENIC RECESSIVE,DIGENIC DOMINANT,MITOCHONDRIAL,MULTIFACTORIAL,SOMATIC MUTATION,SOMATIC MOSAICISM,INHERITED CHROMOSOMAL IMBALANCE
####Unsupported CGD inheritance values:
'AD (with imprinting)','Multigenic','AR (Triallelic)','XL (involving both OPN1 genes)','Digenic (Severe digenic insulin resistance can be due to digenic mutations in PPP1R3A and PPARG)','Digenic (with CFTR or other SCCN1 genes)','PAR','Digenic','Maternal','YL','BG'

## Inheritance VEP plugin
A VEP plugin to annotate consequences with inheritance modes based on their gene.
###Installation
Place Inheritance.pm in your vep plugin directory (e.g. '~/.vep/Plugins')
###Usage
```
./vep -i variations.vcf --plugin Inheritance,/FULL_PATH_TO_PREPROCESSED_INHERITANCE_FILE/gene_inheritance_modes.tsv
```
