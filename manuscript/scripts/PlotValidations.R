library("devtools")
remove.packages("TraceR")
devtools::install_github("walterxie/TraceR")

###
# use https://github.com/walterxie/TraceR/blob/master/examples/Pipeline.R
# to create all required stats files (*.tsv)
###

library("TraceR")

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/alpha2")
setwd(WD)
figs.dir = "figs"
if (!dir.exists(figs.dir)) dir.create(figs.dir)

### mu
param = "mu"
df <- read_tsv(paste0(param,"-coverage.tsv"), col_types = cols())
cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
cov.per
p <- TraceR::ggCoverage(df, cov.per, x.lab=paste("True",param,"value"))
ggsave(file.path(figs.dir, paste0(param, ".pdf")), p, width = 4, height = 3)

stopifnot(cov.per >= 90)

#df.sub <- df %>% filter(mean < 0.045)
#nrow(df.sub)
#p <- TraceR::ggCoverage(df.sub, cov.per, x.lab=paste("True",param,"value"))
#ggsave(file.path(figs.dir, paste0(param, "-sub-",nrow(df.sub),".pdf")), p, width = 4, height = 3)

### theta
param = "theta"
df <- read_tsv("Theta.tsv", col_types = cols())
cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
cov.per

max(df$HPD95.upper)
#bou = round(max(df$HPD95.upper) / 100) * 100 + 200 # show outlier
p <- ggCoverage(df, cov.per, x.lab=paste0("True log-",param," value"), y.lab="Log-mean posterior",
                     #x.max.lim=bou, y.max.lim=bou, 
                     x.txt.just = 0)
# log scale and fix labels and text 
p <- p + scale_x_log10(limits = c(1,1e4), 
                       breaks = scales::trans_breaks("log10", function(x) 10^x),
                       labels = scales::trans_format("log10", scales::math_format(10^.x))) + 
  scale_y_log10(limits = c(1,1e4), breaks = scales::trans_breaks("log10", function(x) 10^x),
                labels = scales::trans_format("log10", scales::math_format(10^.x))) +
  annotate("text", x=1, y=9000, label= paste("covg. =", cov.per, "%"),
           hjust = 0, size = 5) 

ggsave(file.path(figs.dir, paste0(param, "-lg10.pdf")), p, width = 4, height = 3)

stopifnot(cov.per >= 90)

#df.sub <- df %>% filter(mean < 200)
#nrow(df.sub)
#p <- TraceR::ggCoverage(df.sub, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
#ggsave(paste0(param, "-sub-",nrow(df.sub),".pdf"), p, width = 4, height = 3)


### total.br.len
param = "total.br.len"
df <- read_tsv("psi.treeLength.tsv", col_types = cols())
cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
cov.per

max(df$HPD95.upper)
p <- TraceR::ggCoverage(df, cov.per, x.lab=paste("True total branch length"), 
                     x.txt.just = 0)
ggsave(file.path(figs.dir, paste0(param, ".pdf")), p, width = 4, height = 3)

stopifnot(cov.per >= 90)

### tree.height
param = "tree.height"
df <- read_tsv("psi.height.tsv", col_types = cols())
cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
cov.per

max(df$HPD95.upper)
p <- TraceR::ggCoverage(df, cov.per, x.lab=paste("True tree height"), 
                     x.txt.just = 0)
ggsave(file.path(figs.dir, paste0(param, ".pdf")), p, width = 4, height = 3)

stopifnot(cov.per >= 90)

#df.sub <- df %>% filter(mean < 1500)
#nrow(df.sub)
#p <- TraceR::ggCoverage(df.sub, cov.per, x.lab=paste("True tree height"), 
#                     x.txt.just = 0)
#ggsave(paste0(param, "-sub-",nrow(df.sub),".pdf"), p, width = 4, height = 3)

### r_0
for (par in 0:2) {
  param = paste0("r", par)
  post.file = paste0("r_", par, ".tsv")
  cat("plot", param, ", summary file = ", post.file, "\n")
  stopifnot(file.exists(post.file))
  
  df <- read_tsv(post.file, col_types = cols())
  cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
  print(cov.per)
  
  p <- TraceR::ggCoverage(df, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
  ggsave(file.path(figs.dir, paste0(param, ".pdf")), p, width = 4, height = 3)
  
  stopifnot(cov.per >= 90)
}

### kappa1
for (par in 0:2) {
  param = paste0("kappa", (par+1))
  post.file = paste0("kappa.", (par+1), ".tsv")
  cat("plot", param, ", summary file = ", post.file, "\n")
  
  stopifnot(file.exists(post.file))
  
  df <- read_tsv(post.file, col_types = cols())
  cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
  print(cov.per)
  
  p <- TraceR::ggCoverage(df, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
  ggsave(file.path(figs.dir, paste0(param, ".pdf")), p, width = 4, height = 3)
  
  stopifnot(cov.per >= 90)
}

### pi_0.A
nuc.arr = c('A','C','G','T')
for (par in 0:2) {
  for (nuc.i in 1:length(nuc.arr)) {
    nuc = nuc.arr[nuc.i]
    param = paste0("pi_", par, "_", nuc)
    post.file = paste0("pi_", par, ".", nuc, ".tsv")
    cat("plot", param, ", summary file = ", post.file, "\n")
    
    stopifnot(file.exists(post.file))
    
    df <- read_tsv(post.file, col_types = cols())
    cov.per <- round(nrow(subset(df, is.in==TRUE)) / nrow(df) * 100)
    print(cov.per)
    
    p <- TraceR::ggCoverage(df, cov.per, x.lab=paste("True",param,"value"), x.txt.just = 0)
    ggsave(file.path(figs.dir, paste0(param, ".pdf")), p, width = 4, height = 3)
    
    stopifnot(cov.per >= 90)
    
  }
}


### saturation test: root height * mu * r vs. converage

WD = file.path("~/WorkSpace/linguaPhylo", "manuscript/alpha2")
setwd(WD)
getwd()

df1 <- createAnalysisDF(tru.val.par="Θ", posteriorFile="Theta.tsv") %>%
  select(analysis, is.in) %>% rename(theta=is.in)
cov.per <- round(nrow(subset(df1, theta==TRUE)) / nrow(df1) * 100)
cov.per

#df2 <- createAnalysisDF(tru.val.par="μ", posteriorFile="mu.tsv") %>%
#  select(analysis, is.in) %>% rename(mu=is.in)
#df.covg <- inner_join(df1, df2, by = "analysis")
#stopifnot(nrow(df.covg) == nrow(df1) && nrow(df.covg) == nrow(df2))

#for (par in 0:2) {
#  post.file = paste0("r_", par, ".tsv")
#  stopifnot(file.exists(post.file))
  
#  tru.val.par=paste0("r_", par)
#  df <- createAnalysisDF(tru.val.par=tru.val.par, posteriorFile=post.file) %>%
#    select(analysis, is.in) %>% rename(!!tru.val.par := is.in)
#  df.covg <- df.covg %>% inner_join(df, by = "analysis")
#}
#stopifnot(nrow(df.covg) == nrow(df) && nrow(df.covg) == nrow(df1))
#df.covg %>% filter(theta==F)

df.covg <- df1
# root height * mu * r
sub.site <- list()
tru <- NULL

sele.tru <- df.covg %>% select(analysis) %>% unlist
#sele.tru <- sub('-nowd', '', sele.tru) %>% file.path("../al2",.)
sele.tru

for(lg in sele.tru ) {
  
  lg.fi <- file.path(paste0(lg,"_true.log"))
  cat("Load ", lg.fi, "...\n")
  
  # must 1 line
  tru <- read_tsv(lg.fi) %>% select(params2) %>% unlist # need vector here
  
  # add tree stats
  fn <- sub('\\.log$', '', lg.fi)
  # add tree stats
  tre.fi <- paste0(fn, "_ψ.trees")
  if (!file.exists(tre.fi)) stop("Cannot find ", tre.fi)
  tru.tre <- read.nexus(tre.fi)
  cat("Load true tree from", tre.fi, "having", Ntip(tru.tre), "tips ...\n")
  
  # total branch len and tree height
  tru <- c(tru, sum(tru.tre$edge.length), max(nodeHeights(tru.tre)))
  
  ### tree hight * mu * relative rate
  hei <- max(nodeHeights(tru.tre))
  mu <- tru[names(tru) == "μ"]
  r0 <- tru[names(tru) == "r_0"]
  r1 <- tru[names(tru) == "r_1"]
  r2 <- tru[names(tru) == "r_2"]
  tmp.hmr <- c(r0*hei*mu, r1*hei*mu, r2*hei*mu)
  
  sub.site[[lg]] <- tmp.hmr
}

# at least 1 partition (tree hight * mu * relative rate) > 1
bad.sim <- as_tibble(sub.site) %>% select_if(~any(. > 1))
ncol(bad.sim) # 56

gene.dis <- as_tibble(sub.site) %>% add_column(rowname=c("d_0","d_1","d_2")) %>% 
  pivot_longer(-rowname, 'analysis', 'value') %>%
  pivot_wider(analysis, rowname)

gene.dis$analysis <- sub('(.*)_', '', gene.dis$analysis) %>% paste0("al2_", .)

df <- inner_join(df.covg, gene.dis, by = "analysis")
df
stopifnot(nrow(df.covg) == nrow(gene.dis) && nrow(df.covg) == nrow(df))

df.plot <- df %>% select(analysis, theta, starts_with("d_")) %>% 
  gather(partition, distance, -c(analysis, theta)) %>% 
  group_by(analysis, theta) %>%  
  arrange(analysis, desc(distance)) %>% 
  # take the partition having max distance 
  slice(which.max(distance)) 

#select(analysis, theta, starts_with("d_")) %>% 
#pivot_longer(cols = starts_with("d_"))

y.txt = max(df.plot$distance)
param = "Theta"
p <- ggplot(df.plot, aes(x = analysis, y = distance, group = theta, colour = theta)) + 
  geom_point(aes(shape = factor(theta)), size = 0.2, alpha = 0.9) + 
  scale_shape_manual(values=c(4, 1, 0))+
  geom_hline(yintercept = 1.0, linetype = "dotted", size = 0.2) + 
  geom_hline(yintercept = 0.5, linetype = "dotted", size = 0.2) + 
  scale_y_log10() +
  annotate("text", x = 2, y = y.txt, label = paste("covg. =", cov.per,"%"), hjust = 0, size = 5) +
  xlab(param) + ylab("max(root height * mu * r)") + guides(colour = FALSE, shape = FALSE) + 
  theme_classic() + 
  theme(text = element_text(size = 15), 
        axis.text.x=element_blank(),
        axis.ticks.x=element_blank())
getwd()
ggsave(file.path("figs", paste0(param, "-saturation", ".pdf")), p, width = 4, height = 3)

###

df.mu <- createAnalysisDF(tru.val.par="μ", posteriorFile="mu.tsv") %>% subset(is.in==F)
df.theta <- createAnalysisDF(tru.val.par="Θ", posteriorFile="Theta.tsv") %>% subset(is.in==F)
df.r0 <- createAnalysisDF(tru.val.par="r_0", posteriorFile="r_0.tsv") %>% subset(is.in==F)
df.r1 <- createAnalysisDF(tru.val.par="r_1", posteriorFile="r_1.tsv") %>% subset(is.in==F)
df.r2 <- createAnalysisDF(tru.val.par="r_2", posteriorFile="r_2.tsv") %>% subset(is.in==F)
df.k1 <- createAnalysisDF(tru.val.par="κ_0", posteriorFile="kappa.1.tsv") %>% subset(is.in==F)
df.k2 <- createAnalysisDF(tru.val.par="κ_1", posteriorFile="kappa.2.tsv") %>% subset(is.in==F)
df.k3 <- createAnalysisDF(tru.val.par="κ_2", posteriorFile="kappa.3.tsv") %>% subset(is.in==F)

df.theta %>% inner_join(df.mu, by = c("analysis" = "analysis")) %>% print(width = Inf)

  # left_join(df.r0, by = c("analysis" = "analysis")) %>%
  # left_join(df.r1, by = c("analysis" = "analysis")) %>%
  # left_join(df.r2, by = c("analysis" = "analysis")) %>%
  # left_join(df.k1, by = c("analysis" = "analysis")) %>%
  # left_join(df.k2, by = c("analysis" = "analysis")) %>%
  # left_join(df.k3, by = c("analysis" = "analysis")) 

