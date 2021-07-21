
require(tidyverse)

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs")
setwd(WD)

allStats = list.files(pattern = ".tsv") 

# the selected parameters
param = c("mu","Theta", "r_0", "r_1", "r_2",
          "kappa.1", "kappa.2", "kappa.3",
          "pi_0.A", "pi_0.C", "pi_0.G", "pi_0.T", 
          "pi_1.A", "pi_1.C", "pi_1.G", "pi_1.T", 
          "pi_2.A", "pi_2.C", "pi_2.G", "pi_2.T" )#,"psi.height")
stats.name = c("mean", "HPD95.lower", "HPD95.upper", "ESS")

minESS <- c()

for (pa in param) {
  cat("Analyse parameter : ", pa, "...\n")
  df <- tibble(trace=stats.name)
  
  traces <- NULL
  for(st in allStats) {
    cat("Load ", st, "...\n")
    fn <- sub('\\.tsv$', '', st)
    
    # "mean", "HPD95.lower", "HPD95.upper", "ESS"
    traces <- read_tsv(st) %>% filter(trace %in% stats.name) %>% 
      #mutate_at(pa, as.numeric) %>% 
      select(pa) %>% unlist # need vector here
    
    df <- try(df %>% add_column("{fn}" := traces))
  }
  
  ESS <- df %>% filter(trace == "ESS") %>% select(!trace) %>% unlist %>% as.numeric
  tmp.minESS <- min(ESS)
  cat("min ESS = ", tmp.minESS, "\n")
  
  #stopifnot(tmp.minESS < 200)
  
  minESS <- c(minESS, tmp.minESS)
  
  write_tsv(df, file.path("../figs", paste0(pa, ".tsv")))
}
cat("min ESS = ", paste(minESS, collapse = ", "), "\n")

### true value

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/xmls")
setwd(WD)

allLogs = list.files(pattern = ".log") 

# must have the same order of param
param2 = c("μ","Θ")
param2
param

# save true values to a file
df2 <- tibble(parameter = param2)
tru <- NULL
for(lg in allLogs) {
  cat("Load ", lg, "...\n")
  fn <- sub('_true\\.log$', '', lg)
  
  # must 1 line
  tru <- read_tsv(lg) %>% select(param2) %>% unlist # need vector here

  df2 <- try(df2 %>% add_column("{fn}" := tru))
}

write_tsv(df2, file.path("../figs", "trueValue.tsv"))

### fixed theta

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs-theta-20")
setwd(WD)

allStats = list.files(pattern = ".tsv") 

# the selected parameters
param = c("psi.height", "mu","r_0", "r_1", "r_2")
stats.name = c("mean", "HPD95.lower", "HPD95.upper", "ESS")

minESS <- c()
for (pa in param) {
  cat("Analyse parameter : ", pa, "...\n")
  df <- tibble(trace=stats.name)
  
  traces <- NULL
  for(st in allStats) {
    cat("Load ", st, "...\n")
    fn <- sub('\\.tsv$', '', st)
    
    # "mean", "HPD95.lower", "HPD95.upper", "ESS"
    traces <- read_tsv(st) %>% filter(trace %in% stats.name) %>% 
      #mutate_at(pa, as.numeric) %>% 
      select(pa) %>% unlist # need vector here
    
    df <- try(df %>% add_column("{fn}" := traces))
  }
  
  ESS <- df %>% filter(trace == "ESS") %>% select(!trace) %>% unlist %>% as.numeric
  tmp.minESS <- min(ESS)
  cat("min ESS = ", tmp.minESS, "\n")
  minESS <- c(minESS, tmp.minESS)
  
  if (pa=="psi.height")  write_tsv(df, file.path("../figs", paste0("theta-20-", pa, ".tsv")))
}
cat("min ESS = ", paste(minESS, collapse = ", "), "\n")

