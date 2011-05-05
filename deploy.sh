#!/bin/bash

if [ "$1" = "prod" ];then
	echo "deploying to nearlyfreespeech"
	scp -r target/jnlp/* nhoibian_displayator@ssh.phx.nearlyfreespeech.net:/home/public/DisplayableCreator/
else
	echo "copying to displayatorSite"
	cp -r target/jnlp/lib /Users/niko/Sites/displayatorSite/DisplayableCreator/
fi