[![Build Status](https://app.travis-ci.com/molgenis/vip-inheritance.svg?branch=main)](https://app.travis-ci.com/molgenis/vip-inheritance)
[![Quality Status](https://sonarcloud.io/api/project_badges/measure?project=molgenis_vip-inheritance&metric=alert_status)](https://sonarcloud.io/dashboard?id=molgenis_vip-inheritance)
# Variant Interpretation Pipeline - Inheritance annotation
## Requirements
- Java 17
## GenemapConverter
A tool to converts the omim 'genemap2.txt' to a tab separated file that can be used by the VEP Inheritance Plugin.
It creates a line for all gene symbols in the input file.
###Usage
```
usage: java -jar genemap-mapper.jar [-i <arg>] -h <arg> [-c <arg>] -ip
       <arg> [-o <arg>] [-f] [-d]
 -i,--omim input <arg>               Input OMIM genemap2 file.
 -h,--hpo <arg>                      Input HPO .hpoa file.
 -c,--cgd input <arg>                Input cgd txt.gz file.
 -ip,--incomplete_penetrance <arg>   file with incomplete penetrance genes, containing at lease a 'Gene' and a 'Source' column.
                                     (.tsv).
 -o,--output <arg>                   Output file (.tsv).
 -f,--force                          Override the output file if it
                                     already exists.
 -d,--debug                          Enable debug mode (additional
                                     logging).

usage: java -jar genemap-mapper.jar -v
 -v,--version   Print version.
```

###Mapping
Currently only values supported by [VIP inheritance matcher](https://github.com/molgenis/vip-inheritance-matcher) are supported.

|OMIM Inheritance*|Annotation|
|---|---|
|X-LINKED DOMINANT|XLD|
|X-LINKED RECESSIVE|XLR|
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
