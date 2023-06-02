latexdiff --exclude-textcmd="listing,alltt" --config='PICTUREENV=(?:picture|DIFnomarkup|listing|lstlisting|alltt)[\w\d*@]*' linguaPhylo_plos_revision.tex linguaPhylo_plos.tex > diff_final.tex
sed 's/\\hspace{0pt}/ /g' diff_final.tex > manuscript_diff_final.tex
