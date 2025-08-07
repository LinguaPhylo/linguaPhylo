# Repository setup guide for manuscripts

This is a simple guide for creating a Git repository to host the necessary materials for a publication.

## Recommended folder structure 

Some necessary subfolders ordered alphabetically:

- `data`: may contain the data files used in the analyses.
- `examples`: may include XML files, LPhy scripts, or other illustrative examples.
- `figures`: contains the plots and figures used in the manuscript.
- `logs`: may include BEAST 2 log files and tree files. 
   Files larger than 100 MB uploaded via Git (command line or push) are blocked by GitHub.
   For large files, consider hosting them in a separate repository.
- `manuscript`: includes the LaTeX documents and the associated bibliography file.
- `README`: explains the purpose and contents of this repository.


## Recommended repositories for large files

1. GitHub large file storage (LFS) 

   The maximum per-file size via Git LFS is up to 2 GB on the free plan.
   https://docs.github.com/en/repositories/working-with-files/managing-large-files
   
2. Figshare

   The University of Auckland [Figshare](https://auckland.figshare.com) 
   
3. Dropbox

   [Dropbox for Researchers in UoA](https://research-hub.auckland.ac.nz/managing-research-data/research-data-storage/dropbox-for-researchers) 
   

The others, such as Zenodo, Google Drive, Microsoft OneDrive, Dryad, and similar platforms, can also be used.
