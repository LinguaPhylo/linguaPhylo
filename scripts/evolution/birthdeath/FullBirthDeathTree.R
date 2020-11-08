# "Mean fossil tips = 10.6203189650657 +/- 0.0118951242967501"
# "Mean extant tips = 13.831303170197 +/- 0.0180934643415934"
# "Mean sa tips = 0"
# "Mean unsampled tips = 10.6203189650657"
# "Total trees simulated successfully = 534604"

library(TreeSim)
library(FossilSim)

n = 1000000;

trees <- sim.bd.age(age=4,numbsim=n,lambda=1, mu=0.5);

fossil.tips.count = numeric(length=numsims);
extant.tips.count = numeric(length=numsims);
sa.tips.count= numeric(length=numsims);
unsampled.tips.count = numeric(length=numsims);
count = 0;

for (i in 1:length(trees)) {

  tree = trees[[i]];

  if (!is.numeric(tree)) {
    
    fossil.tips = is.extinct(tree,tol=0.000001)
    extant.tips = tree$tip.label[!(tree$tip.label %in% fossil.tips)]

    sa.tips = tree$tip.label[tree$edge[,2][(tree$edge[,2] %in% 1:length(tree$tip.label)) & (tree$edge.length == 0.0)]]

    unsampled.tips = fossil.tips[!(fossil.tips %in% sa.tips)]
    
    fossil.tips.count[i] = length(fossil.tips)
    extant.tips.count[i] = length(extant.tips)
    sa.tips.count[i] = length(sa.tips)
    unsampled.tips.count[i] = length(unsampled.tips)
    
    count = count + 1;
  } else {
    fossil.tips.count[i] = NA;
    extant.tips.count[i] = NA;
    sa.tips.count[i] = NA;
    unsampled.tips.count[i] = NA;
  }
}

fossil.tips.mean = mean(fossil.tips.count, na.rm=T)
extant.tips.mean = mean(extant.tips.count, na.rm=T);
sa.tips.mean = mean(sa.tips.count, na.rm=T);
unsampled.tips.mean = mean(unsampled.tips.count, na.rm=T);

print(paste0("Mean fossil tips = ",fossil.tips.mean, " +/- ", (sd(fossil.tips.count, na.rm=T)/sqrt(count))))
print(paste0("Mean extant tips = ",extant.tips.mean, " +/- ", (sd(extant.tips.count, na.rm=T)/sqrt(count))))
print(paste0("Mean sa tips = ",sa.tips.mean))
print(paste0("Mean unsampled tips = ",unsampled.tips.mean))
print(paste0("Total trees simulated successfully = ",count))