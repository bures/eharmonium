#!/bin/sh

IN_DIR=tracks
OUT_DIR=loops

# Requires the samples to be at least 4 sec

for IN_FILE in $IN_DIR/*.wav; do
	sox "$IN_FILE" body-tmp.wav trim 0 -2
	sox "$IN_FILE" tail-tmp.wav trim -2

	sox --norm=-1 tail-tmp.wav body-tmp.wav "$OUT_DIR/`basename $IN_FILE`" splice -q 2,1
	
	rm body-tmp.wav tail-tmp.wav
done
