
require(tidyverse)
require(ggplot2)
require(ape)
require(phytools)

### true tree

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/xmls-theta-20")
setwd(WD)

allTrees = list.files(pattern = ".trees") 

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
ggsave(paste0("total-branch-length.png"), p, width = 6, height = 5)

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
ggsave(paste0("root-height.png"), p, width = 6, height = 5)
