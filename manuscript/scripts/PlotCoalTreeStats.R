
require(tidyverse)
require(ggplot2)
require(ape)
require(phytools)

# preparation
# "4.13e-03" "6.74e-03" "1.10e-02"
formatC(qlnorm(p=c(0.025,0.5,0.975), meanlog=-5.0, sdlog=0.25), format = "e", digits = 2)


### true tree

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/hky-coal")
setwd(WD)

allTrees = list.files(pattern = ".trees") 
allTrees

# eval how the simulated (trees) root heights 
# and total branch lengths are distributed

tre.tot.len <- list()
tre.height <- list()
for(tre in allTrees) {
  # 129 tips 
  tru.tre <- read.nexus(tre)
  cat("Load ", tre, " having ", Ntip(tru.tre), " tips ...\n")
  
  fn <- sub('_true.*$', '', tre)
  tre.tot.len[[fn]] <- sum(tru.tre$edge.length)
  
  tre.height[[fn]] <- max(nodeHeights(tru.tre))
}
cat("\naverage tree height = ", mean(unlist(tre.height)), "\n")

# true mu
allLogs = list.files(pattern = ".log") 
mu <- list()
for(lg in allLogs) {
  cat("Load ", lg, "...\n")
  fn <- sub('_true\\.log$', '', lg)
  
  # must 1 line
  tru <- read_tsv(lg) %>% select("μ") %>% unlist # need vector here
  
  mu[[fn]] <- tru
}
cat("average mu = ", mean(unlist(mu)), "\n")
# average tree height in units of time * average mu ~ 0.3-0.5
cat( mean(unlist(tre.height)) * mean(unlist(mu)), "\n")


# histogram

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/figs")
setwd(WD)

vec = unlist(tre.tot.len)
m = mean(vec)
se = sd(vec)/sqrt(length(vec))

p <- ggplot(mapping=aes(vec)) +
  geom_histogram() +
  geom_vline(xintercept = m) + 
  geom_vline(xintercept = (m + 2*se)) + 
  geom_vline(xintercept = (m - 2*se)) + 
  xlab("total branch length of true tree") +
  theme_bw()
ggsave(paste0("hky-coal-total-branch-length.pdf"), p, width = 6, height = 5)
#ggsave(paste0("hky-coal-total-branch-length.png"), p, width = 6, height = 5)

vec = unlist(tre.height)
m = mean(vec)
se = sd(vec)/sqrt(length(vec))

p <- ggplot(mapping=aes(vec)) +
  geom_histogram() +
  geom_vline(xintercept = m) + 
  geom_vline(xintercept = (m + 2*se)) + 
  geom_vline(xintercept = (m - 2*se)) + 
  xlab("root height of true tree") +
  theme_bw()
#ggsave(paste0("hky-coal-root-height.png"), p, width = 6, height = 5)
ggsave(paste0("hky-coal-root-height.pdf"), p, width = 6, height = 5)
