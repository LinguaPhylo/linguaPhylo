#library("devtools")
#devtools::install_github("walterxie/TraceR")

library("TraceR")

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs-theta-20")
setwd(WD)

allLogs = list.files(pattern = ".log") 
allLogs

mcmc.log <- NULL
traces <- NULL
stats <- NULL
for(lg in allLogs) {
  cat("\nProcess ", lg, "...\n")
  
  # read MCMC log
  mcmc.log <- readMCMCLog(lg)
  # get traces and remove burn in
  traces <- getTraces(mcmc.log, burn.in=0.1)
  # get stats
  stats <- analyseTraces(traces)
  
  write_tsv(stats, paste0(sub('\\.log$', '', lg), ".tsv"))
}



