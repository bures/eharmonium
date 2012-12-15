#!/bin/sh

IN_DIR=tracks
OUT_DIR=loops

for IN_FILE in $IN_DIR/*.wav; do
	sox "$IN_FILE" body-tmp.wav trim 0 -4
	sox "$IN_FILE" tail-tmp.wav trim -4 

	sox --norm=-1 tail-tmp.wav body-tmp.wav "$OUT_DIR/`basename $IN_FILE`" splice -q 4,2
	
	rm body-tmp.wav tail-tmp.wav
done
