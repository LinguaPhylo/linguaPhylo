latexdiff --exclude-textcmd="listing,alltt" --config='PICTUREENV=(?:picture|DIFnomarkup|listing|lstlisting|alltt)[\w\d*@]*' linguaPhylo_plos_old.tex linguaPhylo_plos.tex > diff.tex
sed 's/\\hspace{0pt}/ /g' diff.tex > manuscript_diff.tex
