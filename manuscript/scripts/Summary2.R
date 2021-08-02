
require(tidyverse)

readTraces <- function(traces.file, stats.name = c("mean", "HPD95.lower", "HPD95.upper", "ESS")) {
  cat("Load ", traces.file, "...\n")
  # "mean", "HPD95.lower", "HPD95.upper", "ESS"
  traces <- try(read_tsv(traces.file)) %>% filter(trace %in% stats.name) 
  return(traces)
}


# everthing in the same folder
WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/alpha1")
anal = "al1_"
setwd(WD)
# excl all *_0.trees.tsv
allStats = list.files(pattern = "_([0-9]+).tsv") 
allStats # a char vector
extraStats = allStats[grep("_10([0-9]).tsv", allStats, ignore.case = T)]
extraStats # extra 10 simulations: *_100.tsv ~ *_109.tsv

# the selected parameters, do not change the order
#params = c("mu","Theta", "kappa", "pi.A", "pi.C", "pi.G", "pi.T",
#           "psi.treeLength", "psi.height")
params = c("mu","Theta", "r_0", "r_1", "r_2",
           "kappa.1", "kappa.2", "kappa.3",
           "pi_0.A", "pi_0.C", "pi_0.G", "pi_0.T", 
           "pi_1.A", "pi_1.C", "pi_1.G", "pi_1.T", 
           "pi_2.A", "pi_2.C", "pi_2.G", "pi_2.T",
           "psi.treeLength", "psi.height")
stats.name = c("mean", "HPD95.lower", "HPD95.upper", "ESS")

### check ESS first

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

write_tsv(lowESS %>% mutate_at(3:ncol(.), as.numeric) %>% select(1:2 | where(~ any(. < 200))), 
          file.path(paste0("low-ESS.tsv")))

### write ESS summary by parameters

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
#params2 = c("μ","Θ","κ","π_0","π_1","π_2","π_3")
params2 = c("μ","Θ","r_0","r_1","r_2","κ_0","κ_1","κ_2","π_0_0","π_0_1","π_0_2","π_0_3",
            "π_1_0","π_1_1","π_1_2","π_1_3","π_2_0","π_2_1","π_2_2","π_2_3")
params2
params
# true tree stats have to compute from the true trees
tre.params = c("total.br.len","tree.height")

names(tracesDF)
# save true values to a file
df2 <- tibble(parameter = c(params2, tre.params))
tru <- NULL
# have to use names(tracesDF), it may contain some of extra 10
for(lg in names(tracesDF)) {
  
  lg.fi <- file.path(paste0(lg,"_true.log"))
  cat("Load ", lg.fi, "...\n")

  # must 1 line
  tru <- read_tsv(lg.fi) %>% select(params2) %>% unlist # need vector here
  
  # add tree stats
  fn <- sub('\\.log$', '', lg.fi)
  # add tree stats
  tre.fi <- paste0(fn, "_ψ.trees")
  if (!file.exists(tre.fi)) stop("Cannot find ", tre.fi)
  tru.tre <- read.nexus(tre.fi)
  cat("Load true tree from", tre.fi, "having", Ntip(tru.tre), "tips ...\n")
  
  # total branch len and tree height
  tru <- c(tru, sum(tru.tre$edge.length), max(nodeHeights(tru.tre)))
  
  df2 <- try(df2 %>% add_column(!!(lg) := tru))
}

write_tsv(df2, "trueValue.tsv")





