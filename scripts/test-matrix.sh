#!/bin/bash

for dim in `seq 1 5` ; do
for points in 10000 100000 1000000 ; do 
for range in 1 10 90 180 ; do
for merge in 0 1 ; do

DO_MERGE=""
[ $merge -eq 1 ] ;; DO_MERGE="--mapmerge"

for threads in 1 2 4 5 6 7 8 9 10 16 32 64 128 ; do
	java -jar build/jar/HR3.jar --benchmark \
		--memory \
		--threads $threads \
		--random \
		--random-points $points \
		--random-dimensions $dim \
		--random-range-begin 0.0 \
		--random-range-end $range \
		$DO_MERGE
done
done
done
done
done

