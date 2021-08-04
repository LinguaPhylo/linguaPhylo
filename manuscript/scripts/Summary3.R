
require(tidyverse)

readTraces <- function(traces.file, stats.name = c("mean", "HPD95.lower", "HPD95.upper", "ESS")) {
  cat("Load ", traces.file, "...\n")
  # "mean", "HPD95.lower", "HPD95.upper", "ESS"
  traces <- try(read_tsv(traces.file)) %>% filter(trace %in% stats.name) 
  return(traces)
}

# RSV2_0.tsv
#trace	posterior	likelihood
#mean	-1395.71800741416	-1260.57129157319
#stderr.of.mean	0.948421280126143	0.142003271885284
#stdev	21.6784291082163	5.61509246716363
pullAValidResultFromExtraPool <- function(file.orig, ext.idx, ext.pool=c(), ext.trees.pool=c(), 
                                          params=c("trace","mu","Theta"), 
                                          stats.name = c("mean", "HPD95.lower", "HPD95.upper", "ESS")) {
  if (ext.idx >= length(ext.pool)) # length(ext.pool) extra
    stop("All extra simulations have been used ! ", ext.idx, " >= ", length(ext.pool))
  
  tre.sta <- NULL
  lowESS <- tibble()
  
  while (ext.idx < length(ext.pool)) {
    # pick up "ext.idx" result from extra pool
    et.fi <- ext.pool[ext.idx]
    stopifnot(file.exists(et.fi))
    
    # file steam
    fstm <- sub('\\.tsv$', '', et.fi)
    
    traces <- try(read_tsv(et.fi)) %>% 
      filter(trace %in% stats.name) %>% 
      select(params) # must incl trace column
    
    ESS <- traces %>% filter(trace == "ESS") %>% 
      select(!trace) %>% as.numeric
    minESS <- min(ESS)
    cat(et.fi, ", min ESS = ", minESS, "\n")
    
    # all ESS >= 200 then break, otherwise continue the loop
    if (minESS >= 200) {
      cat("Replace the low ESS result", file.orig, "to", et.fi, "\n")
      
      # add tree stats
      if (length(ext.trees.pool)>0) {
        tre.sta.fi <- ext.trees.pool[ext.idx]
        stopifnot(file.exists(tre.sta.fi))
        
        tre.sta <- try(read_tsv(tre.sta.fi)) %>% 
          filter(trace %in% stats.name) %>% select(!trace)
        # same row names
        traces <- cbind(traces, tre.sta)
      }
      # point to the next before break loop
      ext.idx <- ext.idx + 1
      break
    } # end if minESS >= 200
    
    # must increase after pick up tree stats from extra pool 
    ext.idx <- ext.idx + 1
  } # end while
  list(file.orig=file.orig, file.selected=et.fi, ext.idx=ext.idx, traces=traces)
}

# everthing in the same folder
MYPATH = "~/WorkSpace/linguaPhylo"
prefix = "al2"
WD = file.path(MYPATH, "manuscript/weights", paste0(prefix, "nowd"))
setwd(WD)
anal = paste0(prefix, "-nowd_")
# true logs could be in another folder
TRU.VAL.PATH = file.path(MYPATH, "manuscript/weights", prefix)

# excl all *_0.trees.tsv
allStats = list.files(pattern = "_([0-9]+).tsv") 
allStats # a char vector
extraStats = allStats[grep("_10([0-9]).tsv", allStats, ignore.case = T)]
extraStats # extra 10 simulations: *_100.tsv ~ *_109.tsv

# the selected parameters, do not change the order
params = c("mu","Theta", "r_0", "r_1", "kappa.1", "kappa.2", 
           "pi_0.A", "pi_0.C", "pi_0.G", "pi_0.T", 
           "pi_1.A", "pi_1.C", "pi_1.G", "pi_1.T",
           "psi.treeLength", "psi.height")
stats.name = c("mean", "HPD95.lower", "HPD95.upper", "ESS")

### check ESS first

getwd()
# record 100 files whose ESS >= 200
tracesDF <- list() 
# if any of 100 has <200 ESS, then it will be replaced by one of extra 10
lowESS <- tibble()
etr <- 1
for(i in 0:99) {
  fi <- paste0(anal, i, ".tsv")
  stopifnot(file.exists(fi))
  
  # "mean", "HPD95.lower", "HPD95.upper", "ESS"
  traces <- readTraces(fi) %>% select(trace, params) 
  ESS <- traces %>% filter(trace == "ESS") %>% select(!trace)
  minESS <- min(ESS %>% as.numeric)
  cat(fi, ", min ESS = ", minESS, "\n")
  
  fn <- sub('\\.tsv$', '', fi)
  tre.sta.fi <- paste0(fn, ".trees.tsv")
  if (file.exists(tre.sta.fi)) {
    # add tree stats
    tre.sta <- try(read_tsv(tre.sta.fi)) %>% 
      filter(trace %in% stats.name) %>% select(!trace)
    traces <- cbind(traces, tre.sta)
  }
  
  if (minESS < 200) {
    selected <- pullAValidResultFromExtraPool(fi, etr, ext.pool=extraStats, params=c("trace", params))
    # selected extra
    file.selected <- selected$file.selected
    fn.sel <- sub('\\.tsv$', '', file.selected)
    tracesDF[[fn.sel]] <- selected$traces
    
    # sync current index in the pool 
    etr <- selected$ext.idx
    
    # record low ESS
    tmp.low <- ESS %>% as_tibble %>% add_column(fn, .before=1) %>% add_column(fn.sel, .before=2)
    lowESS <- lowESS %>% rbind(tmp.low) 
    
  } else {
    tracesDF[[fn]] <- traces
  }
}
stopifnot(length(tracesDF) == 100)
nrow(lowESS)
write_tsv(lowESS %>% mutate_at(3:ncol(.), as.numeric) %>% select(1:2 | where(~ any(. < 200))), 
          file.path(paste0("low-ESS.tsv")))

### write the parameter stats by grouping selected results (ESS >= 200) 

names(tracesDF)
minESS <- c()

for (pa in params) {
  cat("Analyse parameter : ", pa, "...\n")
  df <- tibble(trace=stats.name)
  
  tra.par <- NULL
  for(i in 1:length(tracesDF)) {
    # "mean", "HPD95.lower", "HPD95.upper", "ESS"
    tra.par <- tracesDF[[i]] %>% select(pa) %>% unlist
    stopifnot(length(tra.par) > 0)
    
    df <- try(df %>% add_column(!!(names(tracesDF)[i]) := tra.par))
  }
  
  ESS <- df %>% filter(trace == "ESS") %>% select(!trace) %>% unlist
  # to numeric
  tmp.minESS <- min(ESS %>% as.numeric)
  cat("min ESS = ", tmp.minESS, "\n")
  minESS <- c(minESS, tmp.minESS)

  if (!is.na(tmp.minESS) && tmp.minESS >= 200)  
    write_tsv(df, paste0(pa, ".tsv"))
  else
    warning("Summary not generated ! ", pa, " min ESS = ", tmp.minESS, "\n")
}
cat("min ESS = ", paste(minESS, collapse = ", "), "\n")
cat("min of min ESS = ", min(minESS), "\n")

### true value
require(ape)
require(phytools)
# must have the same order of param
params2 = c("μ","Θ","r_0","r_1","κ_0","κ_1","π_0_0","π_0_1","π_0_2","π_0_3",
            "π_1_0","π_1_1","π_1_2","π_1_3")
# true tree stats have to compute from the true trees
tre.params = c("total.br.len","tree.height")
c(params2, tre.params)
params

setwd(TRU.VAL.PATH)
getwd()

names(tracesDF)
seleTruFilSteam <- sub('-nowd', '', names(tracesDF))
seleTruFilSteam
# save true values to a file
df2 <- tibble(parameter = c(params2, tre.params))
tru <- NULL
# have to use names(tracesDF), it may contain some of extra 10
for(lg in seleTruFilSteam) {
  
  lg.fi <- file.path(paste0(lg,"_true.log"))
  cat("Load ", lg.fi, "...\n")
  stopifnot(file.exists(lg.fi))

  # must 1 line
  tru <- read_tsv(lg.fi) %>% select(params2) %>% unlist # need vector here
  
  # add tree stats
  fn <- sub('\\.log$', '', lg.fi)
  # add tree stats
  tre.fi <- paste0(fn, "_ψ.trees")
  stopifnot(file.exists(tre.fi))
  
  tru.tre <- read.nexus(tre.fi)
  cat("Load true tree from", tre.fi, "having", Ntip(tru.tre), "tips ...\n")
  
  # total branch len and tree height
  tru <- c(tru, sum(tru.tre$edge.length), max(nodeHeights(tru.tre)))
  
  df2 <- try(df2 %>% add_column(!!(lg) := tru))
}

# back to WD
setwd(WD)
getwd()
write_tsv(df2, "trueValue.tsv")



