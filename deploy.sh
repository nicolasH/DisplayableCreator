#!/bin/bash

if [ "$1" = "prod" ];then
        echo "deploying to nearlyfreespeech"
        scp -r target/jnlp/* nhoibian_displayator@ssh.phx.nearlyfreespeech.net:/home/public/DisplayableCreator/
        scp -r latest nhoibian_displayator@ssh.phx.nearlyfreespeech.net:/home/public/DisplayableCreator/
        exit
fi
if [ "$1" = "beta" ];then
            echo "deploying to nearlyfreespeech beta directory"
            scp -r target/jnlp/* nhoibian_displayator@ssh.phx.nearlyfreespeech.net:/home/public/DisplayableCreator/beta/
            exit
else
        echo "copying locally to displayatorSite - use 'prod' for remote deploy."
        cp -r target/jnlp/lib /Users/niko/Sites/displayatorSite/DisplayableCreator/
fi
