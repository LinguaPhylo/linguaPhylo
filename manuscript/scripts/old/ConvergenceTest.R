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


### plot

MYPATH = "~/WorkSpace/linguaPhylo/manuscript"
setwd(MYPATH)

df3 <- try(read_tsv(file.path(MYPATH, "noWeigDiriPrior", "converg-test.tsv")))
# 15 simulations
print(df3[df3$is.in.x==F | df3$is.in.y==F,], width = Inf)
# 48 simulations
print(df3[df3$in.bound==F,], width = Inf)

require("ggplot2")
fail1 = nrow(subset(df3, is.in.x == F))
fail2 = nrow(subset(df3, is.in.y == F))

x.txt = min(abs(df3$mean.diff), df3$mean.bound)
y.txt = max(abs(df3$mean.diff), df3$mean.bound)
x.txt.just = 0 # max(df3$mean.bound) * 0.1
y2.txt = y.txt * 0.5

df.plot <- df3 %>% select(analysis, mean.diff, mean.bound,contains("in")) %>% 
  mutate(mean.diff = abs(mean.diff)) %>% 
  mutate(is.in = if_else(is.in.x & is.in.y, "TT", "")) 
  # 0 both good, 3 both failed, 1 first group good, 2 second good
df.plot$is.in[df.plot$is.in.x==F & df.plot$is.in.y==F] <- "FF"
df.plot$is.in[df.plot$is.in.x==T & df.plot$is.in.y==F] <- "TF"
df.plot$is.in[df.plot$is.in.x==F & df.plot$is.in.y==T] <- "FT"
#df.plot[nchar(df.plot$is.in) < 1,]
stopifnot(all(nchar(df.plot$is.in) > 1)) 

p <- ggplot(data = df.plot, aes(x = mean.bound, y = mean.diff, 
                                group = is.in, colour = is.in, shape = in.bound)) + 
  geom_point(size = 1, alpha = 0.5) + 
  scale_x_log10() + scale_y_log10() +
  scale_color_manual(values=c("red", "orange", "pink","blue"))+
  scale_shape_manual(values=c(4, 1, 0))+
  geom_abline(intercept = 0, slope = 1, color = "black", linetype = "dotted", size = 0.2) + 
  annotate("text", x = x.txt, y = y.txt, label = paste("1st group fail =", fail1), 
           hjust = x.txt.just, size = 5) + 
  annotate("text", x = x.txt, y = y2.txt, label = paste("2nd group fail =", fail2), 
           hjust = x.txt.just, size = 5) + 
  xlab("One std. err. of means") + ylab("Mean difference") + #guides(colour = FALSE) + 
  theme_classic() + theme(text = element_text(size = 15))
p
ggsave(file.path(MYPATH, "noWeigDiriPrior", "figs", "converg-test.pdf"), p, width = 5, height = 4)


