MetaData
========
Methods
-------

- **ages**
  - gets the ages of these taxa as an array of doubles.
- **charset**
  - return a partition. If the string doesn't match charset's syntax, then check if the string matches a defined name in the nexus file. Otherwise it is an error. The string is referred to one partition at a call, but can be multiple blocks, such as d.charset("2-457\3 660-896\3").
- **extractTrait**
  - return a trait alignment, which contains the set of traits<br>extracted from taxa names in this alignment.<br>The regular expression is the separator to split the taxa names,<br>and i (>=0) is the index to extract the trait value.
- **nchar**
  - The number of characters/sites.
- **taxaNames**
  - The names of the taxa.