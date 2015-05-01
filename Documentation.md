# Installation #

You will need the software Subversion (svn) to get the source code. Subversion is available at the following link:

http://subversion.apache.org/packages

Afterwards you can get the source code:

```
svn checkout http://santa-sim.googlecode.com/svn/trunk/ santa-sim
```

You will need ant to build the project. If you do not have ant on your machine you can get it at:

http://ant.apache.org/bindownload.cgi

In order to build the project change the directory to location of the file build.xml and run the command:

```
ant
```

A directory called dist will be created which contains the file santa.jar

# Invocation #

## Windows platforms ##

The simplest way to run a simulation configured in config.xml is to
invoke the following command from the DOS prompt, from within the
directory where your config.xml file is located:

```
java -jar c:\path\to\santa.jar config.xml
```

Replace `c:\path\to\santa.jar` with the path where you extracted the santa.jar file.

In some cases it may be necessary to configure the JVM to use more memory:

```
java -Xmx512M -jar c:\path\to\santa.jar config.xml
```

## UNIX, MacOS X, or Linux ##

Change directory to where your config.xml file is located, and invoke using:

```
$ java -jar /path/to/santa.jar config.xml
```

Replace `/path/to/santa.jar` with the path where you extracted the santa.jar file.

In some cases it may be necessary to configure the JVM to use more memory:

```
$ java -Xmx512M -jar /path/to/santa.jar config.xml
```

## Command-line options ##

You can bind values to parameters used in your config file. For
example to bind the value '10000' to parameter 'generations', and
'0.1' to parameter 'selection' use:

```
java -jar .../santa.jar -generations=10000 -selection=0.1 config.xml
```

# Configuration XML File #

All properties of a simulation, including definition of the initial
population, fitness functions, replication and mutation operators, and
sampling information, is defined a single XML file.

## Overall format ##

The overall format of a configuration file is like this:

```
<santa xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:noNamespaceSchemaLocation="santa.xsd">

    <!-- How many times should the simulation be run ?
         Optional (default = 1) -->
    <replicates>100</replicates>

    <simulation>
        <!-- Description of the genome: properties, example sequences, and features -->
        <genome>
            ...
        </genome>

        <!-- Definition of the initial population -->
        <population>
            ...
        </population>

        <!-- Description of the (default) fitness function -->
        <fitnessFunction>
            ...
        </fitnessFunction>

        <!-- Description of the (default) replicator -->
        <replicator>
            ...
        </replicator>

        <!-- Description of the (default) mutator -->
        <mutator>
            ...
        </mutator>

        <!-- Definition of a first epoch -->
        <epoch>
            <!-- A name for the epoch (used only to display simulator progress) -->
            <name>epoch 1</name>

            <!-- Duration of the epoch in number of generations -->
            <generations>1000</generations>

            <!-- Optionally, override the default fitness function, replicator, or mutator -->
            <fitnessFunction>
                 ...
            </fitnessFunction>

            <replicator>
                 ...
            </replicator>

            <mutator>
                 ...
            </mutator>
        </epoch>

        <!-- Optionally, additional epochs... -->
        <epoch>
            ...
        </epoch>

        <!-- Define when and how information must be sampled from the simulation -->
        <samplingSchedule>
            ...
        </samplingSchedule>
    </simulation>
</santa>
```

## Genome description: 

&lt;genome&gt;

 ##

The genome description specifies the length and organization of the
genome in different features. A feature corresponds to an open reading
frame, and specifies either a nucleotide or transcribed amino acid
sequence.

The organization of the genome in features allows to later define
different modes of selection that act on different parts of the
genome.

In addition, a single sequence or sequence alignment may be specified
in the genome definition, which may be used to seed the initial
population (if configured so in the 

&lt;population&gt;

 definition), or to
configure a purifying selection to reflect observed states (if
configured so in a 

&lt;purifyingFitness&gt;

 definition).

An example of a genome definition:

```
<genome>
    <length>21</length>

    <!-- protein from a forward ORF that spans the entire genome -->
    <feature>
       <name>ABC protein</name>
       <type>aminoAcid</type>
       <coordinates>1-21</coordinates>
    </feature>

    <!-- protein from a backward ORF spanning sites 11 to 19 --> 
    <feature>    
       <name>DE protein</name>
       <type>aminoAcid</type>
       <coordinates>19-11</coordinates>
    </feature>

    <sequences>
>seq1
CCTCAGGTCACTCTTTGGCAAC
>seq2
CCTCGGGTCACTCCTTGGCGAC
    </sequences>
</genome>
```

### Genome length: 

&lt;length&gt;

 ###

The genome length, as a number of nucleotides.

### Genome feature: 

&lt;feature&gt;

 ###

A genome feature has three properties:
  * 

&lt;name&gt;

**A unique feature name.**

  * 

&lt;type&gt;

**Must be 'nucleotide' or 'aminoAcid'. This is used to define if a fitness factor acts on nucleotides or amino acids. Note that for aminoAcid, the length of the feature needs to be a multiple of 3.   'aminoAcid' features implicitly get a fitness criteria that assigns `-infinity` to any stop codon (TAA, TAG, or TGA) generated by a mutation, regardless of any other fitness criteria that is defined.**

  * 

&lt;coordinates&gt;

**Defines how the feature is created from nucleotides in the genome. The format is a comma-separated list of fragments. Each fragment is defined by a single nucleotide site, or a range (begin-end). A range where begin is larger than end is read in the opposite direction.**

By default, a nucleotide feature _genome_ is created, which represents
the entire genome.

### Sequence or sequence alignment: 

&lt;sequences&gt;

 ###

One or multiple full-genome sequences may be given, either in FASTA or
_plain_ format. In the plain format, sequences are separated by a
new-line.

## Initial population: 

&lt;population&gt;

 ##

The initial population is described by a population size and a way to
create its genomes. Note that currently, the population size is kept
constant throughout the entire simulation.

An example of a population definition:
```
<population>
    <populationSize>10000</populationSize>
    <inoculum>all</inoculum>
</population>
```

### Population size: 

&lt;populationSize&gt;

 ###

The number of individuals in the population. Simulation speed is
roughly _N_ log _N_ to the population size _N_. **TODO: check this!**

### Inoculum: 

&lt;inoculum&gt;

 ###

Defines how the genomes for all individuals in the initial population
are initialized. The 

&lt;inoculum&gt;

 definition is optional and 'none' is
the default value.

Possible values are:

  * **none**: initialize the genome of all individuals to a default nucleotide sequence which is poly-adenine ('AAA...')

  * **all**: initialize the genome of each individual by sampling, with replacement, from all sequences provided in the 

&lt;sequences&gt;

 within the 

&lt;genome&gt;

 description.

  * **consensus**: initialize the genome of all individuals to the consensus sequence for the sequences provided in the 

&lt;sequences&gt;

 within the 

&lt;genome&gt;

 description.

  * **random**: initialize the genome of all individuals to the same sequence, randomly chosen from the sequences provided in the 

&lt;sequences&gt;

 within the 

&lt;genome&gt;

 description.

## Fitness function: 

&lt;fitnessFunction&gt;

 ##

This defines a fitness function. The fitness function is provided as
one or more factors, each acting on certain genomic nucleotide or
amino acid features, which are multiplied to compute the fitness for a
single individual.

An empty 

&lt;fitnessFunction&gt;

 definition corresponds to a neutral fitness
function: the fitness of all individuals is the same, regardless of
their genome sequence.

Each factor applies to a selection of sites within a single genome feature.

An example of a fitness function block (with two factors):

```
<fitnessFunction>

    <!-- purifying fitness on all amino acids in ABC protein, except for the 4th -->    
    <purifyingFitness>
        <feature>ABC protein</feature>
        <sites>1-3,5-7</sites>
        <rank>
       	    <order>observed</order>
            <breakTies>random</breakTies>
        </rank>
        <fitness>
	    <lowFitness>0.5</lowFitness>
            <minimumFitness>0.1</minimumFitness>
        </fitness>
    </purifyingFitness>

    <!-- age dependent fitness on alleles corresponding to the entire DE protein -->    
    <ageDependentFitness>
	<feature>DE protein</feature>
	<declineRate>0.005</declineRate>
    </ageDependentFitness>

</fitnessFunction>
```

### Feature and sites ###

Each factor is applied to a selection of sites in a genome feature. By
default, this feature is the _genome_ feature (nucleotides of the
entire genome), and the sites are all sites in the feature.

This may be overridden by:

  * 

&lt;feature&gt;

**: the name of one of the defined features in the**

&lt;genome&gt;

 description. If omitted, _genome_ is assumed.

  * 

&lt;sites&gt;

**: A comma separated list of single sites or site ranges within the feature. Note that if the feature is an amino acid feature, this refers to amino acid sites, while if the feature is a nucleotide feature, this refers to nucleotide sites.**

### Empirical fitness function: 

&lt;empiricalFitness&gt;

 ###

This fitness function defines the fitness for each state, and acts on each site individually.

All that is needed to define an empirical fitness function is a list
of fitness values for each of the possible states (20 for an amino
acid feature, 4 for a nucleotide feature).   When working with an
amino acid feature, a stop codon (TAA, TAG, or TGA) always has
a fitness of -infinity.

An example of an empirical fitness function:

```
<empiricalFitness>
    <feature>ABC protein</feature>
    <sites>4</sites>

    <!-- assign fitness 1 to K, 0.85 to N, and 0.01 to all other amino acids -->
    <values>
      0.01 <!-- A -->
      0.01 <!-- C -->
      0.01 <!-- D -->
      0.01 <!-- E -->
      0.01 <!-- F-->
      0.01 <!-- G -->
      0.01 <!-- H -->
      0.01 <!-- I -->
      1    <!-- K -->
      0.01 <!-- L -->
      0.01 <!-- M -->
      0.85 <!-- N -->
      0.01 <!-- P -->
      0.01 <!-- Q -->
      0.01 <!-- R -->
      0.01 <!-- S -->
      0.01 <!-- T -->
      0.01 <!-- V -->
      0.01 <!-- W -->
      0.01 <!-- Y -->
    </values>
</empiricalFitness>
```

To define an empirical fitness function, you need to define:

  * 

&lt;values&gt;

**: A list of fitness values (4 for a nucleotide feature or 20 for an amino acid feature), separated by white-space (space or new-lines), which corresponds to the states in alphabetical order.**

### Purifying fitness function: 

&lt;purifyingFitness&gt;

 ###

This fitness function defines a purifying selection, which acts on
each site individually.

For each site, it assigns a fitness to the different states, between 1
and 0, where '1' corresponds to the most fit state, and '0' to a
lethal state. The definition is modular: you need to define a way to
rank the states from most to least fit, and separately define a way to
assign the fitness values to this rank. To reflect that some states
are completely lethal, the ranking also defines a _probable_ set of
states.

For each site, the different possible states (4 nucleotides or 20
amino acids) are ordered according to a 

&lt;rank&gt;

 definition and these
states are then assigned a fitness, according to a 

&lt;fitness&gt;


definition.

Optionally, in a 

&lt;fluctuate&gt;

 block, you may define a process which, at
random time points, makes another state the most fit. In this way, the
purifying fitness function may be used to cause a non-stationary
positive selection.

An example of a purifying fitness function block:

```
<purifyingFitness>
    <feature>ABC protein</feature>
    <sites>1-3,5-7</sites>
    <rank>
        <order>chemical</order>
        <breakTies>ordered</breakTies>
    </rank>
    <fitness>
	<lowFitness>0.5</lowFitness>
        <minimumFitness>0.1</minimumFitness>
    </fitness>
</purifyingFitness>
```

#### States ranked from most fit to most deleterious: 

&lt;rank&gt;

 ####

Within the rank, it is specified how the possible states at each site
are ordered from most fit to most deleterious, and defines a set of
_probable_ states.

  * 

&lt;order&gt;

**: how the states should be ordered. Possible values are:
    ***observed**: states are ordered by their frequency in the**

&lt;sequences&gt;

 alignment defined in the 

&lt;genome&gt;

 description, by decreasing frequency.
    * **chemical**: amino acids are ordered so that the amino acids in the set with chemical properties similar to the most frequent amino acid in the 

&lt;sequences&gt;

 alignment defined in the 

&lt;genome&gt;

 description, are ranked before the other states. The sets (in no particular order, and separated by |) are: AVIL|F|CM|G|ST|W|Y|P|DE|NQ|HKR.
    * **hydropathy**: idem but using a partition based on hydropathy: IVLFCMAW|GTSYPH|DEKNQR.
    * **volume**: idem but using a partition based on volume: GAS|CDPNT|EVQH|MILKR|FYW
    * A custom partition of the states, which is defined by listing them separated by '|' (like the partitions defined above).
    * A custom order of _probable_ states, which is defined by listing the _probable_ states from most fit to least fit. (e.g. `QDNTP`)

  * 

&lt;breakTies&gt;

**: configure whether ties should be broken randomly ('random') or should the state order be used ('ordered') when two states would be given the same rank.**

  * 

&lt;probableSet&gt;

**: when defining**

&lt;fitness&gt;

 as a piece-wise linear model in terms of 

&lt;lowFitness&gt;

 and 

&lt;minimumFitness&gt;

, only the _probable_ states are not assigned the minimum fitness.

> Unless the amount of probable states are overridden here, it is defined, for each site, based on the order used: when order is _observed_, only the states with frequency non-zero frequency are _probable_; when order is based on a partition, only those states in the set with the most frequent state are _probable_.

#### Fitness values assigned to states: 

&lt;fitness&gt;

 ####

Fitness values are assigned to the ranked states, either by specifying
a list of fitness values (with 

&lt;values&gt;

) or based on a piece wise
linear model (with 

&lt;lowFitness&gt;

 and 

&lt;minimumFitness&gt;

):

  * 

&lt;values&gt;

**: A decreasing list of relative fitness values (4 for a nucleotide feature or 20 for an amino acid feature), all between 0 and 1, separated by white-space (space or new-lines). The most fit should be assigned a value of 1.**

  * 

&lt;lowFitness&gt;

**and**

&lt;minimumFitness&gt;

**: Defines a piece wise linear model as in the figure below (**TODO**).**

#### Creating a non-stationary positive selection: 

&lt;fluctuate&gt;

 ####

A purifying fitness function can be transformed into a non-stationary
positive selection, by making, at random times, another state the most
fit state (i.e. the state with fitness '1'). This process is
controlled by two parameters:

  * 

&lt;rate&gt;

**: The rate of a Bernouilli process, which is the probability to fluctuate, per site, per generation.**

  * 

&lt;fitnessLimit&gt;

**: Only consider states with at least this fitness to become the new fittest state (default value is 0, allowing any state to become the new fittest state).**

### Frequency dependent fitness function: 

&lt;frequencyDependentFitness&gt;

 ###

This fitness function (as well as the age and exposure dependent
fitness functions), considers unique _alleles_ formed by the selected
feature sites. Individuals have a same allele if they have exactly the
same nucleotide or amino acid sequence at the selected sites.

The frequency dependent fitness function assigns a fitness to an
individual based on the frequency of the allele in the population, and
assigns a lower fitness to alleles that occur more frequently, using
the formula: f = 1 - _p_ _<sup>shape</sup>_, where _p_ is the frequency
(between 0 and 1) of the allele in the population, and _shape_ a
parameter that controls how severe more frequent alleles are punished
in terms of fitness.

An example of a frequency dependent fitness function:

```
<frequencyDependentFitness>
    <feature>DE protein</feature>
    <shape>0.5</shape>
</frequencyDependentFitness>
```

The fitness function is controlled by a single parameter:

  * 

&lt;shape&gt;

**: a positive number indicating how severe more frequent alleles are punished: e.g. 0.5 for square root function, 1 for linear, 2 for quadratic, ...**

![https://santa-sim.googlecode.com/svn/wiki/frequency%20dependent%20fitness.png](https://santa-sim.googlecode.com/svn/wiki/frequency%20dependent%20fitness.png)

### Age dependent fitness function: 

&lt;ageDependentFitness&gt;

 ###

This fitness function (as well as the frequency and exposure dependent
fitness functions), considers unique _alleles_ formed by the selected
feature sites. Individuals have a same allele if they have exactly the
same nucleotide or amino acid sequence at the selected sites.

The age dependent fitness function assigns a fitness to an
individual based on the age of the allele in the population (since it
last appeared), and assigns a lower fitness to alleles that have been
continuously present in the population for a longer time, using the
formula: f = _e_ _<sup>-declineRate a</sup>_, where _a_ is the age (in number
of generations) of the allele in the population, and _declineRate_ a
parameter that controls how severe older alleles are punished in terms
of fitness.

An example of an age dependent fitness function:

```
<ageDependentFitness>
    <feature>DE protein</feature>
    <declineRate>0.005</declineRate>
</ageDependentFitness>
```

The fitness function is controlled by a single parameter:

  * 

&lt;declineRate&gt;

**: a positive number indicating how severe older alleles are punished in terms of fitness.**

![https://santa-sim.googlecode.com/svn/wiki/age%20dependent%20fitness%202.png](https://santa-sim.googlecode.com/svn/wiki/age%20dependent%20fitness%202.png)

### Exposure dependent fitness function: 

&lt;exposureDependentFitness&gt;

 ###

This fitness function (as well as the frequency and age dependent
fitness functions), considers unique _alleles_ formed by the selected
feature sites. Individuals have a same allele if they have exactly the
same nucleotide or amino acid sequence at the selected sites.

The exposure dependent fitness function assigns a fitness to an
individual based on how much the allele has been exposed in the
population (since it last appeared), and assigns a lower fitness to
alleles that have been present for a longer time in a higher
prevalence in the population for a longer time, using the formula: f =
_e_ _<sup>-penalty E</sup>_, where _E_ is the integrated prevalence of the
allele over time since its last appearance, and _penalty_ a parameter
that controls how severe past exposure is punished in terms of
fitness.

In this way, the exposure dependent fitness function is like the age
dependent fitness function, but takes into account the prevalence
rather then the mere presence of the allele.

An example of an exposure dependent fitness function:

```
<exposureDependentFitness>
    <feature>DE protein</feature>
    <penalty>0.01</penalty>
</exposureDependentFitness>
```

The fitness function is controlled by a single parameter:

  * 

&lt;penalty&gt;

**: a positive number indicating how severe more exposed alleles are punished in terms of fitness.**

**TODO: show figure relating exposure to fitness for different values of the penalty parameter.**

## The replication operator: 

&lt;replicator&gt;

 ##

The replicator defines how a child genome is created given one or more
parent genomes.

Two different replication operators are available. A clonal replicator
copies the genome from a single parent. A recombinant replicator
considers that occasionally a child genome is derived from two
parents, where the child genome sequence is created by copying the
genome of one or the other parent, switching between these two genomes
at random sites. In both cases, occasional mistakes during the copying
are modelled by the mutation operator 

&lt;mutator&gt;

.

### Clonal replication: 

&lt;clonalReplicator&gt;

 ###

The clonal replicator simply copies the genome of a single parent. It
has no parameters that need to be defined.

An example of a clonal replicator:

```
<replicator>
    <clonalReplicator />
</replicator>
```

### Recombinant replication: 

&lt;recombinantReplicator&gt;

 ###

The recombinant replicator considers that occasionally a child genome
is derived from two parents, where the child genome sequence is
created by copying the genome of one or the other parent, switching
between these two genomes at random sites.

The operator is configured using two parameters. The first parameter
(

&lt;dualInfectionProbability&gt;

) defines the probability that
recombination takes place, and its name is based on the replication
model of HIV where recombination can only occur when a single cell was
infected simultaneously by two different virions, in this way
packaging two different genomes in a single virion. The second
parameter (

&lt;recombinationProbability&gt;

) defines the probability (per
nucleotide site) that recombination occurs, switching between these
two template genomes.

An example of a recombinant replicator:

```
<replicator>
    <recombinantReplicator>
        <dualInfectionProbability>0.05</dualInfectionProbability>
	<recombinationProbability>0.001</recombinationProbability>
    </recombinantReplicator>
</replicator>
```

The parameters are:

  * 

&lt;dualInfectionProbability&gt;

**: the probability that a child genome should be derived from two parent genomes, using the recombination process, instead of being derived from a single parent genome using clonal replication.**

  * 

&lt;recombinationProbability&gt;

**: the probability per site that a recombination event happens while copying the genome.**

## The mutation operator: 

&lt;mutator&gt;

 ##

The mutation operator describes the probability that mistakes are made
while creating a new genome.

### The nucleotide mutation operator: 

&lt;nucleotideMutator&gt;

 ###

The nucleotide mutation operator describes the mutation process as a
Poisson process that acts on individual sites. A mutation bias may be
specified either by specifying a bias for transitions versus
transversions, or by specifying the relative rate for all 12 possible
mutations.

An example of a nucleotide mutation operator:

```
<mutator>
    <nucleotideMutator>
        <mutationRate>1.0E-4</mutationRate>
        <transitionBias>2.0</transitionBias>
    </nucleotideMutator>
</mutator>
```

The parameters are:

  * 

&lt;mutationRate&gt;

**: the probability for a mutation to occur (per site and per generation)**

  * 

&lt;transitionBias&gt;

**: a relative preference for transitions versus transversions**

  * 

&lt;rates&gt;

**: instead of specifying a transition bias, you can provide a white-space separated list of all 12 relative rates: A->C A->G A->T C->A C->G C->T G->A G->C G->T T->A T->C T->G**

## A simulation epoch: 

&lt;epoch&gt;

 ##

A simulation consists of one or more epochs, which are simulated
sequentially, and which allow the population to experience
consecutively different selection environments, or have different
replication properties.

An epoch has a fixed duration (in number of generations), and
inherits, unless overriden, the default fitness function and
replication and mutation operators.

Example of an epoch:

```
<epoch>
    <name>Epoch with age dependent fitness function</name>
    <generations>1000</generations>

    <!-- override the default fitness function -->
    <fitnessFunction>
        <ageDependentFitness>
            <feature>DE protein</feature>
            <declineRate>0.005</declineRate>
        </ageDependentFitness>
    </fitnessFunction>
</epoch>
```

Specific properties of an epoch are:

  * 

&lt;name&gt;

**: an epoch name (useful for tracking simulation progress)**

  * 

&lt;generations&gt;

**: the duration of the epoch in number of generations**

## Getting simulation results: 

&lt;samplingSchedule&gt;

 ##

The sampling schedule lists one or more _samplers_, which each extract
a particular type of information from the simulation, and dumps it to
a file.

An example of a sampling schedule which defines three samplers:
```
<samplingSchedule>

    <!-- Sample 100 sequences from the population, every 1000 generations,
         in NEXUS format. -->
    <sampler>
        <atFrequency>1000</atFrequency>
        <fileName>alignment_%r.nex</fileName>
        <alignment>
            <sampleSize>100</sampleSize>
            <format>NEXUS</format>
            <label>seq_%g_%s</label>
        </alignment>
    </sampler>

    <!-- Sample the frequency of all 20 states at two amino acid sites,
         every 1000 generations. -->
    <sampler>
        <atFrequency>1000</atFrequency>
        <fileName>aa_frequencies.csv</fileName>
        <alleleFrequency>
	     <feature>protein AB</feature>
             <sites>1,2</sites>
        </alleleFrequency>
    </sampler>

    <!-- Sample population statistics, every generation --> 
    <sampler>
        <atFrequency>1</atFrequency>
        <fileName>stats.csv</fileName>
        <statistics />
    </sampler>
</samplingSchedule>
```

The following properties are common for every sampler:

  * 

&lt;atFrequency&gt;

**: define the sampler to run at a certain frequency, every so many generations**

  * 

&lt;atGeneration&gt;

**: define the sampler to run once at a specific generation**

  * 

&lt;fileName&gt;

**: dumps its result in a given filename. The special string '%r' will be replaced with the current replicate, to avoid that each replicate writes in the same value, each time erasing the results of the previous run.**

### Sampling alignments: 

&lt;alignment&gt;

 ###

The alignment sampler samples whole genome alignments from the
population at a given generation. It has the following properties:

  * 

&lt;sampleSize&gt;

**: the amount of genomes to be sampled**

  * 

&lt;format&gt;

**: the format in which the alignments need to be stored:
    ***NEXUS**: NEXUS format
    ***FASTA**: FASTA format
    ***XML**: a custom XML format**

  * 

&lt;label&gt;

**: the label for each sequence. The special strings '%r', '%g', and '%s' are substituted with respectively the replicate index, the generation number, and the index of the sequence within the sample.**

  * 

&lt;consensus&gt;

**(**true**or**false**): whether a consensus sequence should be synthesized and stored, rather than the full alignment (false by default).**

### Sampling statistics: 

&lt;statistics&gt;

 ###

The statistics sampler dumps some common population genetic statistics:
  * **mean`_`diversity**: mean nucleotide sequence diversity (estimated from a sample of 10 random sequences)
  * **max`_`diversity**: maximum nucleotide sequence diversity (estimated from a sample of 10 random sequences)
  * **min`_`fitness**: fitness of individual with lowest fitness
  * **mean`_`fitness**: mean fitness of population
  * **max`_`fitness**: fitness of individual with highest fitness
  * **max`_`frequency**: frequency of most common genome in the population
  * **mean`_`distance**: mean sequence distance of population from initial population (ignoring mutation saturation, thus an overestimate)

The statistics sampler does not require any configuration.

### Sampling allele frequencies: 

&lt;alleleFrequency&gt;

 ###

This sampler will output the frequency of each possible state at each
given site in a nucleotide or amino acid feature.

By default, the feature is the _genome_ feature (nucleotides of the
entire genome), and the sites are all sites in the feature.

This may be overridden by:

  * 

&lt;feature&gt;

**: the name of one of the defined features in the**

&lt;genome&gt;

 description. If omitted, _genome_ is assumed.

  * 

&lt;sites&gt;

**: A comma separated list of single sites or site ranges within the feature. Note that if the feature is an amino acid feature, this refers to amino acid sites, while if the feature is a nucleotide feature, this refers to nucleotide sites.**

### Sampling genealogies: 

&lt;tree&gt;

 ###

In addition to sampling sequences, the genealogies that gave rise to
those samples may also be sampled.  For example, the configuration below
produces a NEXUS format ancestral tree for 10 random viruses selected
from the population every 100 generations.

As with the `<alignment>` sampler, a random subset of viruses is
selected from the population
each time the sampler is run.  It is possible to get a complete
picture of the branching process if the tree sample size matches the
population size.  The resulting trees may be viewed with
[figtree](http://tree.bio.ed.ac.uk/software/figtree/) or
other tree visualization software.

```
    <sampler>
        <atFrequency>100</atFrequency>
        <fileName>santa_out.trees</fileName>
        <tree>
   	    <sampleSize>10</sampleSize>
   	    <format>NEXUS</format>
   	    <label>sequence_%s</label>
        </tree>
    </sampler>
```

  * 

&lt;sampleSize&gt;

**: number of leaves in the sampled trees.**

  * 

&lt;format&gt;

**: format of the genealogy trees to be produced:
    ***NEXUS**: NEXUS format
    ***NEWICK**: NEWICK format**

  * 

&lt;format&gt;

**: labels associated with the leaves of each tree.
> As with `<label>` elements in alignment samplers,  the strings '%r', '%g', and '%s' are substituted with respectively the replicate index, the generation number, and the index of the sequence within the sample. The format provided here must provide unique names across all sampled taxa, but should provide consistent names across samples.  For example, a value of `name` would not work because all taxa would have the same name.  `sequence_%s_%g` also would not work because the taxon names would change across samples.   A value of `sequence_%s` satisfies all requirements and works well.   An incorrect value here results in a Java exception thrown from deep in the `jebl.jar` library.**



**TODO**

## Advanced configuration file features ##

### Reusing previously defined objects ###

Especially when using multiple epochs, in some cases it is necessary
to keep a particular factor in the fitness function, while changing
other factors.

While this could be achieved, in principle, by copying the definition
of the factor, this is not only error-prone but also resets any state
present in the object. For example, an age dependent or exposure
dependent frequency function keeps track of allele frequency changes
through time, and ideally this shouldn't be disturbed when entering a
new epoch. Likewise, a purifying fitness function may be initialized
using a random tie breaker, and you may want to keep this same
initialization throughout all your epochs.

Therefore, there is a way to attach an _id_ to some of the objects
with state or random initialization, and later refer to these.

For example:

```

<simulation>

...

    <fitnessFunction>
        <!-- fitness function which sets, for each site, all amino acids observed at least once
             in the alignment neutral, and unobserved deleterious -->
        <purifyingFitness id="observed-neutral">
            <feature>protein ABC</feature>
            <rank id="alignment">
                <order>observed</order>
                <breakTies>random</breakTies>
            </rank>
            <fitness>
                <minimumFitness>0.0</minimumFitness>
                <lowFitness>1.0</lowFitness>
            </fitness>
        </purifyingFitness>
    </fitnessFunction>

    <epoch>
        <name>neutral</name>
        <generationCount>1000</generationCount>

        <fitnessFunction>
            <purifyingFitness ref="observed-neutral" />
        </fitnessFunction>
    </epoch>

    <epoch>
       	<name>neutral + selection</name>
        <generationCount>1000</generationCount>

        <fitnessFunction>
       	    <purifyingFitness ref="observed-neutral" />

            <!-- Add a fitness function which results in positive selection
                 at one site -->
            <empiricalFitness>
                ...
            </empiricalFitness>
	</fitnessFunction>
    </epoch>

    ...

</simulation>
```

The attributes _id_ and _ref_ may be used for the following objects:
  * 

&lt;rank&gt;



  * any 

&lt;fitnessFunction&gt;

 factor

### Using run-time parameters ###

The configuration file supports the use of parameters, for which
values may be defined at run time. This may be done for all numeric
configuration parameters. Parameters must start with a '$' sign.

For example, the following population block allows you to specify the
actual population size as a run-time parameter, and would be useful
to study the effect of population size on the simulation, by running
the simulator with different values for the parameter
_populationSize_.

```
<population>
    <populationSize>$populationSize</populationSize>
    <inoculum>all</inoculum>
</population>
```