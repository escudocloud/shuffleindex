
rm -rf FIGURE
mkdir FIGURE
python2 plot/pyscript.py 'log/Performance.log'
gnuplot './plot/Performance.plt'
gnuplot './plot/Performance1.plt'
rm -f log/Performance.log.dat

