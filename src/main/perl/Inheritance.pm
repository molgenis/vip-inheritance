package Inheritance;

use strict;
use warnings;

use Bio::EnsEMBL::Variation::Utils::BaseVepPlugin;
use base qw(Bio::EnsEMBL::Variation::Utils::BaseVepPlugin);

sub version {
    return '1.0';
}

sub feature_types {
    return ['Transcript'];
}

sub variant_feature_types {
    return ['VariationFeature'];
}

sub get_header_info {
    return {
        InheritanceModesGene   => "List of inheritance modes for the gene",
        InheritanceModesPheno   => "List of inheritance modes for the gene per phenotype"
    };
}
sub new {
    my $class = shift;
    my $self = $class->SUPER::new(@_);
    my $file = $self->params->[0];

    my %gene_data;
    my %pheno_data;

    die("ERROR: input file not specified\n") unless $file;
    open(FH, '<', $file) or die $!;

    my @split;

    while(<FH>){
        @split = split(/\t/,$_);
        $gene_data{$split[0]} = $split[1];
        my $pheno = $split[2];
        chomp $pheno;
        $pheno_data{$split[0]} = $pheno;
    }

    $self->{gene_data} = \%gene_data;
    $self->{pheno_data} = \%pheno_data;

    return $self;
}

sub run {
    my ($self, $tva) = @_;
    my $gene_data = $self->{gene_data};
    my $pheno_data = $self->{pheno_data};

    my $symbol = $tva->transcript->{_gene_symbol} || $tva->transcript->{_gene_hgnc};
    return {} unless $symbol;

    return {
        InheritanceModesGene => $gene_data->{$symbol},
        InheritanceModesPheno => $pheno_data->{$symbol}
    };
}

1;