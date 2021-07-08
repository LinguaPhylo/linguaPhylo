
require(tidyverse)
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
  mutate_at(2:ncol(.), as.numeric)
colnames(anal)[2:ncol(anal)] <- statNames
# analysis    mean HPD95.lower HPD95.upper   ESS    true

# sort by true value
anal <- anal %>% arrange(true) %>%
  mutate(analysis = fct_reorder(analysis, true))
# add colour


p <- ggplot() + 
  scale_x_discrete(labels = NULL, breaks = NULL) +
  scale_y_log10() +
  geom_point(data=anal, aes(x=analysis, y=mean)) +
  geom_errorbar(data=anal, aes(x=analysis, y=mean, ymin=HPD95.lower, ymax=HPD95.upper), width=.2,
                position=position_dodge(.9)) +
  # true value
  geom_line(data=anal, aes(x=analysis, y=true, group = 1), colour = "blue") +
  theme_bw()



theta <- read_tsv("Theta.tsv")

