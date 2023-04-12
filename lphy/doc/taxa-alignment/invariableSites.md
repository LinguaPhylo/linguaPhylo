invariableSites function
========================
invariableSites([Alignment](../types/Alignment.md) **alignment**, [Boolean](../types/Boolean.md) **ignoreUnknown**)
-------------------------------------------------------------------------------------------------------------------

Return the array of site indices (start from 0) at the given alignment, which are invariable sites.

### Parameters

- [Alignment](../types/Alignment.md) **alignment** - the original alignment.
- [Boolean](../types/Boolean.md) **ignoreUnknown** - If true (as default), ignore the unknown state '?' (incl. gap '-'), when determine variable sites or constant sites.

### Return type

[Integer[]](../types/Integer[].md)



