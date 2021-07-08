
require(ggplot2)

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/figs")
setwd(WD)

trueVals <- read_tsv("trueValue.tsv")

mu <- read_tsv("mu.tsv")
stopifnot( all(colnames(trueVals)[2:ncol(trueVals)] == colnames(mu)[2:ncol(mu)]) )

mu <- mu %>% rbind(trueVals %>% filter(parameter=="μ") %>% unlist) 
statNames <- mu %>% select(trace) %>% unlist
# replace to "true"
statNames[length(statNames)] <- "true"
  
anal <- mu %>% select(!trace) %>% rownames_to_column %>% 
  gather(analysis, value, -rowname) %>% spread(rowname, value) %>%
  mutate_at(2:ncol(anal), as.numeric)
colnames(anal)[2:ncol(anal)] <- statNames


p <- ggplot(anal, aes(x=analysis, y=mean)) + 
  scale_x_discrete(labels = NULL, breaks = NULL) +
  geom_point() +
  geom_errorbar(aes(ymin=HPD95.lower, ymax=HPD95.upper), width=.2,
                position=position_dodge(.9)) 



theta <- read_tsv("Theta.tsv")

