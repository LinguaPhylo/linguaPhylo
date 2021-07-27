
# posterior File must have:
# mean HPD95.lower HPD95.upper   ESS 
createAnalysisDF <- function(trueValsFile="trueValue.tsv", tru.val.par="μ", posteriorFile="mu.tsv") {
  require(tidyverse)
  
  cat("Load posterior ", posteriorFile, "...\n")
  trueVals <- read_tsv(trueValsFile)
  param <- read_tsv(posteriorFile)
  stopifnot( all(colnames(trueVals)[2:ncol(trueVals)] == colnames(param)[2:ncol(param)]) )
  
  cat("Grep true value of ", tru.val.par, "...\n")
  # tru.val.par="μ"
  param <- param %>% rbind(trueVals %>% filter(grepl(!!tru.val.par, parameter, fixed = T)) %>% unlist) 
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
  print(anal, n = 5)
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
param = "mu"
df <- createAnalysisDF(tru.val.par="μ", posteriorFile="mu.tsv")
cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
cov.per
p <- plotValidations(df, cov.per, x.lab=paste("True",param,"value"))
ggsave(paste0(param, "-all.pdf"), p, width = 4, height = 3)

df.sub <- df %>% filter(mean < 0.045)
nrow(df.sub)
p <- plotValidations(df.sub, cov.per, x.lab=paste("True",param,"value"))
ggsave(paste0(param, "-sub-",nrow(df.sub),".pdf"), p, width = 4, height = 3)

### theta
param = "theta"
df <- createAnalysisDF(tru.val.par="Θ", posteriorFile="Theta.tsv")
cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
cov.per

max(df$HPD95.upper)
bou = round(max(df$HPD95.upper) / 100) * 100 + 200 # show outlier
p <- plotValidations(df, cov.per, x.lab=paste("True",param,"value"), 
                     x.max.lim=bou, y.max.lim=bou, x.txt.just = 0)
ggsave(paste0(param, "-all.pdf"), p, width = 4, height = 3)

df.sub <- df %>% filter(mean < 200)
nrow(df.sub)
p <- plotValidations(df.sub, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
ggsave(paste0(param, "-sub-",nrow(df.sub),".pdf"), p, width = 4, height = 3)


### total.br.len
param = "total.br.len"
df <- createAnalysisDF(tru.val.par=eval(param), posteriorFile=paste0(param,".tsv"))
cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
cov.per

max(df$HPD95.upper)
p <- plotValidations(df, cov.per, x.lab=paste("True total branch length"), 
                     x.txt.just = 0)
ggsave(paste0(param, ".pdf"), p, width = 4, height = 3)

### tree.height
param = "tree.height"
df <- createAnalysisDF(tru.val.par=eval(param), posteriorFile=paste0(param,".tsv"))
cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
cov.per

max(df$HPD95.upper)
p <- plotValidations(df, cov.per, x.lab=paste("True tree height"), 
                     x.txt.just = 0)
ggsave(paste0(param, "-all.pdf"), p, width = 4, height = 3)

df.sub <- df %>% filter(mean < 1500)
nrow(df.sub)
p <- plotValidations(df.sub, cov.per, x.lab=paste("True tree height"), 
                     x.txt.just = 0)
ggsave(paste0(param, "-sub-",nrow(df.sub),".pdf"), p, width = 4, height = 3)

### r_0
for (par in 0:2) {
  param = paste0("r", par)
  tru.val.par = paste0("r_", par)
  post.file = paste0("r_", par, ".tsv")
  cat("plot", param, ", true val name = ", tru.val.par, ", file = ", post.file, "\n")
  
  stopifnot(file.exists(post.file))
  
  df <- createAnalysisDF(tru.val.par=tru.val.par, posteriorFile=post.file)
  cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
  print(cov.per)
  
  p <- plotValidations(df, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
  ggsave(paste0(param, ".pdf"), p, width = 4, height = 3)
}

### kappa1
for (par in 0:2) {
  param = paste0("kappa", (par+1))
  tru.val.par = paste0("κ_", par)
  post.file = paste0("kappa.", (par+1), ".tsv")
  cat("plot", param, ", true val name = ", tru.val.par, ", file = ", post.file, "\n")
  
  stopifnot(file.exists(post.file))
  
  df <- createAnalysisDF(tru.val.par=tru.val.par, posteriorFile=post.file)
  cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
  print(cov.per)
  
  p <- plotValidations(df, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
  ggsave(paste0(param, ".pdf"), p, width = 4, height = 3)
}

### pi_0.A
nuc.arr = c('A','C','G','T')
for (par in 0:2) {
  for (nuc.i in 1:length(nuc.arr)) {
    nuc = nuc.arr[nuc.i]
    param = paste0("pi_", par, "_", nuc)
    tru.val.par = paste0("π_", par, "_", (nuc.i-1))
    post.file = paste0("pi_", par, ".", nuc, ".tsv")
    cat("plot", param, ", true val name = ", tru.val.par, ", file = ", post.file, "\n")
    
    stopifnot(file.exists(post.file))
    
    df <- createAnalysisDF(tru.val.par=tru.val.par, posteriorFile=post.file)
    cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
    print(cov.per)
    
    p <- plotValidations(df, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
    ggsave(paste0(param, ".pdf"), p, width = 4, height = 3)
    
  }
}

