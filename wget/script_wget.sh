#!/bin/bash

echo "Start ..."
while read line; do
	wget "$line"
done < corpus_links.txt
