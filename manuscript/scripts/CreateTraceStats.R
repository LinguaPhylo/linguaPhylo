#library("devtools")
#devtools::install_github("walterxie/TraceR")
#library("TraceR")

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs")
setwd(WD)

allLogs = list.files(pattern = ".log") 

mcmc.log <- NULL
traces <- NULL
stats <- NULL
for(lg in allLogs) {
  cat("Process ", lg, "...\n")
  
  # read MCMC log
  mcmc.log <- readMCMCLog(lg)
  # get traces and remove burn in
  traces <- getTraces(mcmc.log, burn.in=0.1)
  # get stats
  stats <- analyseTraces(traces)
  
  write_tsv(stats, paste0(sub('\\.log$', '', lg), ".tsv"))
}



