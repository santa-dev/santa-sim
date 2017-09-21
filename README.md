# santa-sim

SANTA simulates the evolution of a population of gene sequences
forwards through time. It models the underlying biological processes
as discrete components; replication (including recombination),
mutation, fitness and selection

[![wercker status](https://app.wercker.com/status/0fa06c11d47c043962dfb79cbe7a9c45/s/ "wercker status")](https://app.wercker.com/project/byKey/0fa06c11d47c043962dfb79cbe7a9c45)

In this readme file a rather general overview about the software and its usage is presented. For more detailed information please consult [the documentation page](https://github.com/santa-dev/santa-sim/wiki/Documentation).

## Usage

You need to have java 8 installed in order to use SANTA. A pre-built jar executable file is located in the folder 'dist'. The parameters defining the simulation are introduced in a simulation xml file. Examples of such files are in the folder 'examples'. 

In order to invoke the software run the following command
```{r, engine='bash', invoke santa}
java -jar /path/to/santa.jar /path/to/simulation_config.xml
```
### Simulation configuration XML File

All properties of a simulation, including definition of the initial
population, fitness functions, replication and mutation operators, and
sampling schema, is defined in the input XML file. Consult [the wiki page of SANTA](https://github.com/abbasjariani/santa-sim/wiki) to understand the variables in this file and how to work with them. Looking at the example input XML files provided in the folder 'examples/' would help you understand the logic easier.

### Command-line options

You can bind values to parameters used in your config file. For
example to bind the value '10000' to parameter 'generations':

```
java -jar path/to/santa.jar -generations=10000 path/to/simulation_config.xml
```

## Build instructions

### Install Java 8

You must have Java 8 installed in order to build or run
SANTA. On Linux, you can install the latest [JRE 8 from Oracle](https://www.java.com/en/download/help/linux_x64_install.xml) or the
latest [OpenJDK](http://openjdk.java.net/install/).  Under MacOS you can install the official [JRE 8 from Oracle](https://www.java.com/en/download/help/mac_install.xml), but I would reccomend installing Java via
[Homebrew](http://brew.sh/).  We have not tested SANTA on Windows recently, but it
should work.  Start by installing the latest [JRE 8 for Windows from Oracle](https://www.java.com/en/download/help/windows_manual_download.xml).

### Install Ant

You will also need to install Apache Ant 1.8 or later to build SANTA.
If you do not have ant on your machine you should follow the
instructions at:

http://ant.apache.org/manual/install.html

### Clone the source repository from Github.

On the command line, enter:
```
git clone --depth 1 https://github.com/santa-dev/santa-sim.git
```

### Building the project

When building for the first time, you'll need to have an internet connection, because additional files need to be downloaded.

Change to the directory where you cloned SANTA, and type:

```
ant dist
```

A directory called dist will be created which contains the file `santa.jar`

## Performance measures

[This page](https://cswarth.github.io/santa-perf/) could be consulted to see the memory and time consumption for different population sizes and generations. The details of these simulations could be found [here](https://github.com/cswarth/santa-perf). 

## SANTA Overview
### Population, individuals and genomes

The population in SANTA consists of individual organisms each of which
contains a genome. The genome is a linear sequence of nucleotides but
this can be specified as coding for amino acids and it can be
partitioned with different modes and degrees of selection. Each
individual stores its total fitness and other statistics that may be
of interest such as the number of offspring. At present the program
simulates only haploid populations and is ideal for simulating simple
microorganisms such as viruses and bacteria and eukaryote organelles
such as mitochondria.

### Evolutionary process

The evolutionary process in SANTA is divided into a sequence of
discrete components. Different processes can be selected for each of
these components so that complex evolutionary interactions can be
modelled. The simulation begins with an initial population of
individuals with a specified or random genome sequence. Evolution then
proceeds as follows:

### Fitness calculation

The fitness of each genome is calculated using one or more fitness
functions. These functions define the relative fitness of each
possible state at each site in the genome. The simplest is a neutral
model where all states have a fitness of 1.0. Other built-in fitness functions
are described at "[Fitness Factors](https://github.com/santa-dev/santa-sim/wiki/Documentation#fitness-factors)".

Different fitness functions can be defined for the nucleotide sequence
and its amino acid translation. For example, a purifying selection
function can model the constraints on amino acid replacement whilst
nucleotide changes can be neutral. The fitness for a codon is then the
product of the fitness for each of the three nucleotides and the
fitness of the resulting amino acid.

Furthermore, different partitions of the genome can be given entirely
different fitness functions (e.g., most sites under purifying
selection but with a few sites under diversifying selection).

### Selection

The next generation of individuals then selects their parents from the
previous generation. This is done using a ‘roulette wheel’ selector
where each individual is selected with replacement with a probability
proportional to their genome’s fitness.  The number of parents that
are selected for each new individual depends on the mode of
replication, below.

### Replication

Together with the mutation component, below, the replication component
is analogous to the actions of a polymerase complex and produces the
genetic material for a new individual from one or more parents. The
simplest replicator is clonal and the descendent inherits the genome
of exactly 1 parent. We also provide a recombinant replicator that
models a ‘template-switching’ polymerase. For this replicator, two
parents are selected and a probability is defined of the polymerase
switching between the parents’ templates as replication proceeds along
the genome.

### Mutation

Although mutation also models the action of the polymerase, it is done
as an independent process after replication. The user specifies a
per-site, per-generation probability of mutation. The mutator
component then applies mutations to the genome accordingly. For
efficiency, the default mutator draws the number of mutations from a
Poisson distribution with an expectation given by the number of
nucleotides and the rate. These are then distributed uniformly
across the sites. A bias towards transition-type mutations can be
specified to reflect the action of some polymerases.

In addition to substitution mutations, the user may also configure
indel processes that change the length of a simulated genome.  It is
assumed that frame-shifting a genome is nearly always fatal so only
whole-codon indels are permitted.  Independent probability
distributions control the frequency of insertions and deletions, and
another distribution controls the length of the indel.  When indels
overlap regions on which fitness functions are defined, the regions
may grow or shrink, or even become fragmented.  As with
substitution mutations, indels will affect subsequent generations of
the lineage but will not affect sibling lineages.

### Sampling and statistics

At predefined time-points or intervals, SANTA can be asked to report
statistics about the current population, including average fitness,
genetic diversity and number of unique genomes. A random sample of
individuals of a specified size can also be taken and the genomic
sequences recorded as a nucleotide or amino acid alignment for use in
other software. Finally, it is possible for SANTA to keep track of the
genealogy of the entire population and then provide the tree of the
individuals sampled. This option has significant implications in terms
of efficiency and memory requirements.

## Implementation details

We have implemented a forwards-time discrete-generation gene sequence
simulator designed to scale to large population sizes and
microorganism-scale genome lengths. The simulator, SANTA, is written
in Java and thus is cross-platform without recompilation. It is
configured using control files written in XML for which an XML schema
is provided so they can be easily created and edited in standard XML
editors.

Given the large scale of a typical simulation, we have made every
effort to use memory efficiently. For example, the genome sequences
are stored in a central ‘gene-pool’ so that only unique genomes are
stored with the individuals having only an index for the genome they
currently carry. Individuals that replicate without any mutations thus
just inherit this index. This also makes calculations of the
population genetic diversity more efficient. We have also implemented
an optional framework where genomes are stored as differences from a
central ‘master’ sequence. This master sequence can be recalculated
occasionally to release memory.

Samples of the population can be taken at specific times during the
course of the simulation or at regular intervals. Sequences can be
stored as FASTA or NEXUS format alignments.

## Future developments

SANTA is an open-source project hosted on a public source-code
repository. It is written in an extremely modular way and we envisage
a wide range of additional components being implemented as required by
researchers.

* Empirical replicators and mutators. We aim to provide combinations
    of replicators and mutators based on empirically-estimated
    behaviours of particular polymerases. The first example of this
    will be the retrovirus polymerase, reverse transcriptase, that is
    used by HIV/AIDS pathogens.
  * Dynamic fitness functions. In particular we are interested in
    providing fitness functions that model epistatic effects both at
    the amino acid and RNA secondary structure level, immunological
    fitness functions to model the action of cellular and
    antibody-mediated immune response for within-host pathogen
    research.
  * Population subdivision/migration. This can be modeled by allowing
    new individuals to select parents non-uniformly from individuals
    in more than one deme.
  * (✓ This has been implemented already)Population-size change. Non-constant demographic functions, such
    as logistic shapes or bottlenecks will be easily implemented our
    existing framework.
