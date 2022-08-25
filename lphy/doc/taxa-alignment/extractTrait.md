extractTrait function
=====================
extractTrait(Taxa **taxa**, String **sep**, Integer **i**, String **name**)
---------------------------------------------------------------------------

return a trait alignment, which contains the set of traits extracted from taxa names.

### Parameters

- Taxa **taxa** - the set of taxa whose names contain the traits.
- String **sep** - the substring to split the taxa names, where Java regular expression escape characters will be given no special meaning.
- Integer **i** - i (>=0) is the index to extract the trait value.
- String **name** - the map containing optional arguments and their values for reuse.

### Return type

- Alignment



