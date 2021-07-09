
require(tidyverse)
require(ggplot2)

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/figs")
setwd(WD)

# posteriorFile must have:
# mean HPD95.lower HPD95.upper   ESS 
createAnalysisDF <- function(trueValsFile="trueValue.tsv", param.name="μ", posteriorFile="mu.tsv") {
  trueVals <- read_tsv(trueValsFile)
  param <- read_tsv(posteriorFile)
  stopifnot( all(colnames(trueVals)[2:ncol(trueVals)] == colnames(param)[2:ncol(param)]) )
  
  # param.name="μ"
  param <- param %>% rbind(trueVals %>% filter(parameter==param.name) %>% unlist) 
  statNames <- param %>% select(trace) %>% unlist
  # replace to "true.val"
  statNames[length(statNames)] <- "true.val"
    
  anal <- param %>% select(!trace) %>% rownames_to_column %>% 
    gather(analysis, value, -rowname) %>% spread(rowname, value) %>%
    mutate_at(2:ncol(.), as.numeric)
  colnames(anal)[2:ncol(anal)] <- statNames
  
  # sort by true value
  anal <- anal %>% arrange(true.val) %>%
    # for colouring
    mutate(is.in = (true.val >= HPD95.lower & true.val <= HPD95.upper) ) %>%
    mutate(analysis = fct_reorder(analysis, true.val))
  # analysis    mean HPD95.lower HPD95.upper   ESS    true
  print(anal)
  return(anal)
}

mu <- createAnalysisDF(param.name="μ", posteriorFile="mu.tsv")
  
p <- ggplot(data=mu, aes(x=true.val, y=mean, group = is.in, colour = is.in)) + 
  scale_x_log10() + scale_y_log10() + 
  geom_point(shape=5, size=.5) +
  # regression line 
  geom_smooth(method = "lm", se = FALSE, group = 1, color="blue", size=.3, alpha=.6) +
  geom_linerange(aes(ymin=HPD95.lower, ymax=HPD95.upper)) +
  xlab("") + ylab("mu") + theme_bw()

ggsave(paste0("mu.png"), p, width = 6, height = 5)

theta <- createAnalysisDF(param.name="Θ", posteriorFile="Theta.tsv")

p <- ggplot(data=theta, aes(x=true.val, y=mean, group = is.in, colour = is.in)) + 
  scale_x_log10() + scale_y_log10() + 
  geom_point(shape=5, size=.5) +
  # regression line 
  geom_smooth(method = "lm", se = FALSE, group = 1, color="blue", size=.3, alpha=.6) +
  geom_linerange(aes(ymin=HPD95.lower, ymax=HPD95.upper)) +
  xlab("") + ylab("theta") + theme_bw()

ggsave(paste0("theta.png"), p, width = 6, height = 5)
