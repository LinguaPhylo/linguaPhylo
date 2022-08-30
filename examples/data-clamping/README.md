# Data Clamping

When the `data { }` block has the same random variable containing an existing alignment (e.g. imported from a file), 
the simulated alignment will be replaced by this imported alignment.

## Note

If a LPhy script uses the relative path to load data, 
such as `D = readNexus(file="data/primate.nex", ...);`, 
the `data` sub-folder (containing `primate.nex`) has to be in the same folder 
where `twoPartitionCoalescentNex.lphy` sits inside.

Please be aware of the command line option `-wd` can change `user.dir`, 
which has to be consistent with relative paths inside the LPhy scripts.
If you run LPhy studio, `user.dir` must be the parent folder, where the `examples` and `tutorials` folders are.
The studio will then be able to list all the LPhy scripts and sub-folders containing LPhy scripts.

More details https://linguaphylo.github.io/setup/