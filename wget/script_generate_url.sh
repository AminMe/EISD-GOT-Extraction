#!/bin/bash

#echo "Start ...";
#i=1;
#max=59;
#for (( i=1; i < $max; ++i ))
#do
#	echo "http://gameofthrones.wikia.com/wiki/Category:Characters?page=$i" >> corpus_links.txt;
#done

#echo "Start ...";
#i=1;
#max=22;
#for (( i=1; i < $max; ++i ))
#do
#	echo "http://gameofthrones.wikia.com/wiki/Category:Locations?page=$i" >> corpus_links.txt;
#done

echo "Start ...";
i=1;
max=11;
for (( i=1; i < $max; ++i ))
do
	echo "http://gameofthrones.wikia.com/wiki/Category:Noble_houses?page=$i" >> corpus_links.txt;
done



