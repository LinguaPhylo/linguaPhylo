#library("devtools")
#devtools::install_github("walterxie/TraceR")

library("TraceR")

summariseTracesAndTrees <- function(log.file, tree.file=NA, burn.in=0.1) {
  require("TraceR")
  require(tidyverse)
  
  cat("\nProcess ", log.file, "...\n")
    if (!file.exists(log.file)) stop("\nRequire log file ", log.file)
  
  # read MCMC log
  mcmc.log <- TraceR::readMCMCLog(log.file)
  # get traces and remove burn in
  traces <- TraceR::getTraces(mcmc.log, burn.in=burn.in)
  # get stats
  stats <- TraceR::analyseTraces(traces)
  
  write_tsv(stats, paste0(sub('\\.log$', '', log.file), ".tsv"))
  
  # add tree stats
  if (!is.na(tree.file)) {
    
    if (!file.exists(tree.file)) stop("\nRequire tree file ", tree.file)
    
    tre.sta.df <- TraceR::readTrees(tree.file)
    tre.sta <- TraceR::analyseTreeStats(tre.sta.df)
  
    # ? HPD95.lower.STATE_14150000
    #tre.stats$trace <- sub("\\.STATE.*$","",tre.stats$trace, ignore.case = T)
    
    write_tsv(tre.sta, paste0(tree.file, ".tsv"))  
  }
  
}


WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs")
setwd(WD)

log.files = list.files(pattern = ".log") 
log.files

for(lg in log.files) {
  # assume same file stem
  tree.file=paste0(sub('\\.log$', '', lg), ".trees")
  summariseTracesAndTrees(lg, tree.file)
}

### extra 10
WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs/extra10")
setwd(WD)

extra.log.files = list.files(pattern = ".log") 
extra.log.files

for(lg in extra.log.files) {
  # assume same file stem
  tree.file=paste0(sub('\\.log$', '', lg), ".trees")
  summariseTracesAndTrees(lg, tree.file)
}



