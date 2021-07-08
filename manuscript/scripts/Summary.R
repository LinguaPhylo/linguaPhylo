
require(tidyverse)

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/logs")
setwd(WD)

allStats = list.files(pattern = ".tsv") 

param = c("mu","Theta")#,"psi.height")
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
  cat("min ESS = ", min(ESS), "\n")
  minESS <- c(minESS, min(ESS))
  
  write_tsv(df, file.path("../figs", paste0(pa, ".tsv")))
}

cat("min ESS = ", paste(minESS, collapse = ", "), "\n")

### true value

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/xmls")
setwd(WD)

allLogs = list.files(pattern = ".log") 

param2 = c("μ","Θ")

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

