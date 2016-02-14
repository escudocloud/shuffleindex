#!/bin/bash
python pyscript.py 'Performance_1TiB_MFO_509_HEIGHT_4_LEAFSIZE_8K_SERVER.log'
gnuplot './Performance.plt'
gnuplot './Performance1.plt'
rm ./Performance_1TiB_MFO_509_HEIGHT_4_LEAFSIZE_8K_SERVER.log.dat

