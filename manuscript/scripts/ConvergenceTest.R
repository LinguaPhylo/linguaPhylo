# test convergence by looking at whether difference of mean is in 1 SE

require(tidyverse)

# everything in the same folder
MYPATH = "~/WorkSpace/linguaPhylo/manuscript"
setwd(MYPATH)

RUN1.PATH = file.path("noWeigDiriPrior") # also true logs
RUN2.PATH = file.path("sim3par")

### RUN 1 load selected results
getwd()
theta.run1 <- try(read_tsv(file.path(RUN1.PATH, "Theta.tsv"))) %>% select(!trace) %>% names
theta.run2 <- try(read_tsv(file.path(RUN2.PATH, "Theta.tsv"))) %>% select(!trace) %>% names

sim = intersect(theta.run1, theta.run2)
theta.df <- tibble(analysis = character(), m1 = as.numeric(), m2 = as.numeric(),
                   se1 = as.numeric(), se2 = as.numeric(),
                   mean.diff = as.numeric(), mean.bound = as.numeric())
for(lg in sim) {
  # lg = "RSV2_0" 
  lg1 <- file.path(RUN1.PATH, paste0(lg,".tsv"))
  cat("Load ", lg1, "...\n")
  stopifnot(file.exists(lg1))
  
  trace1 <- read_tsv(lg1) 
  tr.nms <- trace1 %>% select(trace) %>% deframe()
  #print(tr.nms)
  trace1 <- trace1 %>% select(Theta) %>% deframe()
  
  lg2 <- file.path(RUN2.PATH, paste0(lg,".tsv"))
  cat("Load ", lg2, "...\n")
  stopifnot(file.exists(lg2))
  
  trace2 <- read_tsv(lg2) %>% select(Theta) %>% deframe()
  
  m1 = as.numeric(trace1[1])
  m2 = as.numeric(trace2[1])
  se1 = as.numeric(trace1[2])
  se2 = as.numeric(trace2[2])
  theta.df <- theta.df %>% 
    add_row(analysis = lg, m1 = m1, m2 = m2, se1 = se1, se2 = se2,
            mean.diff = (m1 - m2), mean.bound = (se1 + se2)/2 )

}

theta.df$in.bound <- abs(theta.df$mean.diff) <= theta.df$mean.bound
# no in 1 SE
theta.df[theta.df$in.bound==F,]

######
# run PlotValidation.R  noWeigDiriPrior 85%
df1 <- theta.df %>% inner_join(df) %>% select(!c(HPD95.lower, HPD95.upper, ESS, true.val))
print(df1[df1$is.in==F,], width = Inf)
# sim3par 89%
df2 <- df1 %>% inner_join(df, by = "analysis") %>% select(!c(HPD95.lower, HPD95.upper, ESS, true.val))
print(df2[df2$is.in.x==F | df2$is.in.y==F,], width = Inf)
print(df2[df2$in.bound==F,], width = Inf)
######

getwd()
write_tsv(df2, file.path(MYPATH, "noWeigDiriPrior", "converg-test.tsv"))


df3 <- try(read_tsv(file.path(MYPATH, "noWeigDiriPrior", "converg-test.tsv")))
# 15 simulations
print(df3[df3$is.in.x==F | df3$is.in.y==F,], width = Inf)
# 48 simulations
print(df3[df3$in.bound==F,], width = Inf)
