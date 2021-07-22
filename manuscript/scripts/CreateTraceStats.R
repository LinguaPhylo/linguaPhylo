#library("devtools")
#devtools::install_github("walterxie/TraceR")

library("TraceR")


createTracesReport <- function(log.files) {
  require("TraceR")
  
  mcmc.log <- NULL
  traces <- NULL
  stats <- NULL
  for(lg in log.files) {
    cat("\nProcess ", lg, "...\n")
    
    # read MCMC log
    mcmc.log <- readMCMCLog(lg)
    # get traces and remove burn in
    traces <- getTraces(mcmc.log, burn.in=0.1)
    # get stats
    stats <- analyseTraces(traces)
    
    write_tsv(stats, paste0(sub('\\.log$', '', lg), ".tsv"))
  }
}

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs")
setwd(WD)

log.files = list.files(pattern = ".log") 
log.files

createTracesReport(log.files)

### extra 10
WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs/extra10")
setwd(WD)

extra.log.files = list.files(pattern = ".log") 
extra.log.files

createTracesReport(extra.log.files)




