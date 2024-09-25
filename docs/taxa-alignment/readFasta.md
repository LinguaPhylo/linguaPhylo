readFasta function
==================
readFasta([String](../types/String.md) **file**, [Object](../types/Object.md) **options**, [SequenceType](../types/SequenceType.md) **sequenceType**)
-----------------------------------------------------------------------------------------------------------------------------------------------------

A function that parses an alignment from a fasta file.

### Parameters

- [String](../types/String.md) **file** - the name of fasta file including path, which contains an alignment.
- [Object](../types/Object.md) **options** - (optional) the map containing optional arguments and their values for reuse.
- [SequenceType](../types/SequenceType.md) **sequenceType** - (optional) the sequence type for sequences in the fasta format, default to guess the type between Nucleotide and Amino Acid.

### Return type

[Alignment](../types/Alignment.md)


### Examples

- covidDPG.lphy



