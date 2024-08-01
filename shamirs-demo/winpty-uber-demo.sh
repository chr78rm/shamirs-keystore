#!/bin/bash

# you must quote the arguments which are passed through, like:
# ./winpty-shamirs-demo.sh "--no-bulk --jline --echo"

winpty sh -i -c "./run-shamirs-demo.sh $@"
