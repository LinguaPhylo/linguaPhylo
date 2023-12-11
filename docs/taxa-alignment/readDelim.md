readDelim function
==================
readDelim([String](../types/String.md) **file**, [String](../types/String.md) **sep**, [Boolean](../types/Boolean.md) **header**)
---------------------------------------------------------------------------------------------------------------------------------

A function that loads values from a data delimited file and returns a map.

### Parameters

- [String](../types/String.md) **file** - the file name including path.
- [String](../types/String.md) **sep** - the separator (delimiter) to separate values in each row.
- [Boolean](../types/Boolean.md) **header** - If 'header' is true, as default, then use the 1st row as the map keys, otherwise it will create keys and load the values from the 1st row.

### Return type

[Table](../types/Table.md)



