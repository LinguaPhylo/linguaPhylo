
# posterior File must have:
# mean HPD95.lower HPD95.upper   ESS 
createAnalysisDF <- function(trueValsFile="trueValue.tsv", param.name="μ", posteriorFile="mu.tsv") {
  require(tidyverse)
  
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

# colnames must have: "analysis mean HPD95.lower HPD95.upper ESS true.val is.in"
plotValidations <- function(df, cov.per, transp = 0.3, x.lab="", 
                            x.max.lim=NA, y.max.lim=NA, x.txt.just=NA) {
  require(ggplot2)
  
  x.txt = min(df$true.val)
  y.txt = max(df$HPD95.upper)
  if (is.na(x.txt.just)) x.txt.just = max(df$HPD95.upper) * 0.1
  
  p <- ggplot(data=df, aes(x=true.val, y=mean, group = is.in, colour = is.in)) + 
    geom_linerange(aes(ymin=HPD95.lower, ymax=HPD95.upper), size=1.2, alpha=transp) +
    geom_point(size=.2) + 
    geom_abline(intercept = 0, slope = 1, color="black", linetype="dotted", size=.2) +
    annotate("text", x=x.txt, y=y.txt, label= paste("covg. =", cov.per, "%"), 
             hjust = x.txt.just, size = 5) + 
    xlab(x.lab) + ylab("Mean posterior") + 
    guides(colour=FALSE) + theme_classic() + theme(text = element_text(size=15)) 
  
  # same scale in x and y
  if (!is.na(x.max.lim)) 
    p <- p + xlim(NA, x.max.lim) 
  else 
    p <- p + xlim(NA, y.txt) 
  if (!is.na(y.max.lim)) 
    p <- p + ylim(NA, y.max.lim) 
  else 
    p <- p + ylim(NA, y.txt) 
  
  return(p)
}

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/figs")
setwd(WD)

### mu
mu <- createAnalysisDF(param.name="μ", posteriorFile="mu.tsv")
cov.per <- round(nrow(subset(mu, is.in==TRUE)) / nrow(mu) * 100)
mu.sub <- mu %>% filter(mean < 0.045)
nrow(mu.sub)

param = "mu"
p <- plotValidations(mu.sub, cov.per, x.lab=paste("True",param,"value"))
ggsave(paste0(param, "-sub-",nrow(mu.sub),".pdf"), p, width = 4, height = 3)

bound = 0.15
p <- plotValidations(mu, cov.per, x.lab=paste("True",param,"value"), x.max.lim=bound, y.max.lim=bound)
ggsave(paste0(param, "-all.pdf"), p, width = 4, height = 3)

### theta
theta <- createAnalysisDF(param.name="Θ", posteriorFile="Theta.tsv")

cov.per <- round(nrow(subset(theta, is.in==TRUE)) / nrow(theta) * 100)
theta.sub <- theta %>% filter(mean < 1000)
nrow(theta.sub)

param = "theta"
p <- plotValidations(theta.sub, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
ggsave(paste0(param, "-sub-",nrow(theta.sub),".pdf"), p, width = 4, height = 3)

bound = 1000
p <- plotValidations(theta, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
ggsave(paste0(param, "-all.pdf"), p, width = 4, height = 3)

###







