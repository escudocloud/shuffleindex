#!/usr/bin/gnuplot 
GNUPLOT_NAMEFILE='log/Performance.log.dat'
set   autoscale                        
unset log                              
unset label                            
set xtic auto                          
set ytic auto                         
set title "Performance-Db: 740MiB - MFO: 236 HEIGHT: 3 LEAFSIZE: 4K"
set xlabel 'num\_cached'
set ylabel "average query time (ms)"
set yrange [1:30]
set ytics 2
set key top left
set terminal pdf enhanced color 
set output "FIGURE/740M_MFO_236_H3_num_cached_Xaxis.pdf"


plot GNUPLOT_NAMEFILE using 2:($1!=1 ? 1/0 : $3) title 'num\_cover 1' with lines lt 1 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=2 ? 1/0 : $3) title 'num\_cover 2' with lines lt 2 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=3 ? 1/0 : $3) title 'num\_cover 3' with lines lt 3 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=4 ? 1/0 : $3) title 'num\_cover 4'with lines lt 4 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=5 ? 1/0 : $3) title 'num\_cover 5'with lines lt 5 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=6 ? 1/0 : $3) title 'num\_cover 6'with lines lt 6 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=7 ? 1/0 : $3) title 'num\_cover 7'with lines lt 7 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=8 ? 1/0 : $3) title 'num\_cover 8'with lines lt 8 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=9 ? 1/0 : $3) title 'num\_cover 9'with lines lt 9 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=10 ? 1/0 : $3) title 'num\_cover 10'with lines lt 10 lw 2,\
GNUPLOT_NAMEFILE using 2:($1!=1 ? 1/0 : $4) title 'Plain' with lines lt 11 lw 2
