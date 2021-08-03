# modify XML


require(xml2)
require(tidyverse)

setwd(file.path("~/WorkSpace/linguaPhylo", "manuscript/weights/al2"))

xmls = list.files(pattern = ".xml") 
xmls

for(xf in xmls) {
  #xf="al1_99.xml"
  xml <- read_xml(xf)
  cat("Read ", xf, " ...\n") 
  
  distr <- xml_find_all(xml, "//distribution")
  r.prior <- distr[grepl("r.prior", xml_attr(distr, "id"), fixed = T)]
  weight <- xml_find_first(r.prior, "//weights")
  xml_remove(r.prior)
  rm(r.prior, distr)
  
  # rm r.prior and replace weights in r.deltaExchange
  operators <- xml_find_all(xml, "//operator")
  delta.ex <- operators[grepl("r.deltaExchange", xml_attr(operators, "id"), fixed = T)]
  xml_set_attr(delta.ex, "weightvector", NULL)
  # change tag to weightvector 
  xml_set_name(weight, "weightvector")
  xml_add_child(delta.ex, weight)
  #print.AsIs(delta.ex)
  rm(delta.ex, operators, weight)
  
  # change log names
  loggers <- xml_find_all(xml, "//logger")
  tracelog <- loggers[xml_attr(loggers, "id")=="Logger1"]
  treelog <- loggers[xml_attr(loggers, "id")=="psi.treeLogger"]
  
  # create new xml
  fn <- sub('\\.xml$', '', xf) %>% str_split("_") %>% unlist
  # rename log and tree
  xml_attr(tracelog, "fileName") <- paste0(fn[1], "-nowd_" ,fn[2], ".log")
  xml_attr(treelog, "fileName") <- paste0(fn[1], "-nowd_" ,fn[2], ".trees")
  rm( tracelog, treelog )
  
  newxml <- paste0(fn[1], "-nowd_" ,fn[2], ".xml")
  cat("Write modified ", newxml, " ...\n") 
  write_xml(xml, file = newxml)
}

