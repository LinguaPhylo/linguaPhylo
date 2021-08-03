#library("devtools")
#devtools::install_github("walterxie/TraceR")

library("TraceR")

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/alpha1")
setwd(WD)

# inlcude extra 10
log.files = list.files(pattern = "_[0-9]+.log") 
log.files

for(lg in log.files) {
  # assume same file stem
  tree.file=paste0(sub('\\.log$', '', lg), ".trees")
  summariseTracesAndTrees(lg, tree.file=tree.file)
}



### separately summarise extra 10
extra.log.files = log.files[grep("-e_", log.files, fixed = TRUE)]
extra.log.files

for(lg in extra.log.files) {
  # assume same file stem
  tree.file=paste0(sub('\\.log$', '', lg), ".trees")
  TraceR::summariseTracesAndTrees(lg, tree.file)
}



