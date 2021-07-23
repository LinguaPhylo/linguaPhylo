#library("devtools")
#devtools::install_github("walterxie/TraceR")

library("TraceR")

summariseTracesAndTrees <- function(log.file, tree.file=NA, burn.in=0.1) {
  require("TraceR")
  require(tidyverse)
  
  cat("\nProcess ", log.file, "...\n")
    if (!file.exists(log.file)) stop("\nRequire log file ", log.file)
  
  # read MCMC log
  mcmc.log <- readMCMCLog(log.file)
  # get traces and remove burn in
  traces <- getTraces(mcmc.log, burn.in=burn.in)
  # get stats
  stats <- analyseTraces(traces)
  
  write_tsv(stats, paste0(sub('\\.log$', '', log.file), ".tsv"))
  
  # add tree stats
  if (!is.na(tree.file)) {
    require(ape)
    require(phytools)
    
    if (!file.exists(tree.file)) stop("\nRequire tree file ", tree.file)
    
    tre.list <- read.nexus(tree.file)
    n.tre <- length(tre.list)
    if (n.tre < 3) stop("Not enough trees in posterior ! ", tre.list) 
    cat("Load ", n.tre, "trees from ", tree.file, " having", Ntip(tre.list[[1]]), "tips ...\n")
    
    # same to tracer burnin method
    start = round(burn.in * n.tre) + 1
    tres <- tre.list[start:n.tre]
    #sum(tres[[1]]$edge.length)
    
    # 1801 elements
    tot.br.len.list <- mapply(function(x) sum(x$edge.length), tres)
    tre.height.list <- mapply(function(x) max(nodeHeights(x)), tres)
    
    stopifnot(length(tres) == length(tot.br.len.list) && length(tres) == length(tre.height.list))
    
    states <- names(tot.br.len.list) %>% str_remove_all("[A-Z]|[a-z]|\\_") %>% as.numeric
    tres.df <- tibble(states=states, total.br.len=(tot.br.len.list %>% unlist), 
                      tree.height=(tre.height.list %>% unlist))

    tre.stats <- analyseTraces(tres.df, id=c("total.br.len", "tree.height"))
    # ? HPD95.lower.STATE_14150000
    tre.stats$trace <- sub("\\.STATE.*$","",tre.stats$trace, ignore.case = T)
    
    write_tsv(tre.stats, paste0(tree.file, ".tsv"))  
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



