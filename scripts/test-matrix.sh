#!/bin/bash

export THREADS="2 4 5 8 9 16 17 64 128 256"
export JAVA_OPTS="-Xmx8G"
JAVA=java

run_bench() {
	echo $JAVA $JAVA_OPTS \
		 -jar build/jar/HR3.jar \
		--benchmark \
		--memory \
		--random \
		--random-points $1 \
		--random-dimensions $2 \
		--random-range-begin $3 \
		--random-range-end $4 \
		--benchmark-header

	for p in 0 1 ; do
		DO_EXTREMEPOOL=""
		[ $p -eq 1 ] && DO_EXTREMEPOOL="--extremepool"

		for m in 0 1 ; do
			DO_MERGE=""
			[ $m -eq 1 ] && DO_MERGE="--mapmerge"

			for t in $THREADS ; do
				echo $JAVA $JAVA_OPTS \
					 -jar build/jar/HR3.jar \
					--benchmark \
					--memory \
					--threads $t \
					--random \
					--random-points $1 \
					--random-dimensions $2 \
					--random-range-begin $3 \
					--random-range-end $4 \
					$DO_MERGE $DO_EXTREMEPOOL
			done
		done
	done
}

run_bench 6000 3 0 1
run_bench 10000 3 0 10
run_bench 100000 3 0 45
run_bench 150000 3 0 90
run_bench 300000 3 0 180

