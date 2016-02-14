#!/usr/bin/gnuplot 
GNUPLOT_NAMEFILE='Performance_1TiB_MFO_509_HEIGHT_4_LEAFSIZE_8K_SERVER.log'
set   autoscale                        # scale axes automatically
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic auto                          # set xtics automatically
set ytic auto                          # set ytics automatically
set title "Performance-Db: 1TiB - MFO: 509 HEIGHT: 4 LEAFSIZE: 8K"
set xlabel "Number of Covers"
set ylabel "average query time (ms)"
set key top left
set yrange [1:450]
set terminal postscript enhanced color 
set output GNUPLOT_NAMEFILE.".ps"
#set logscale y 
plot GNUPLOT_NAMEFILE using 2:($1!=1 ? 1/0 : $3) title 'Our Pir algorithm - cache 1' with lines 1,\
GNUPLOT_NAMEFILE using 2:($1!=2 ? 1/0 : $3) title 'Our Pir algorithm - cache 2' with lines 2,\
GNUPLOT_NAMEFILE using 2:($1!=3 ? 1/0 : $3) title 'Our Pir algorithm - cache 3' with lines 3,\
GNUPLOT_NAMEFILE using 2:($1!=4 ? 1/0 : $3) title 'Our Pir algorithm - cache 4'with lines 4,\
GNUPLOT_NAMEFILE using 2:($1!=5 ? 1/0 : $3) title 'Our Pir algorithm - cache 5'with lines 5,\
GNUPLOT_NAMEFILE using 2:($1!=6 ? 1/0 : $3) title 'Our Pir algorithm - cache 6' with lines 6,\
GNUPLOT_NAMEFILE using 2:($1!=7 ? 1/0 : $3) title 'Our Pir algorithm - cache 7' with lines 7,\
GNUPLOT_NAMEFILE using 2:($1!=8 ? 1/0 : $3) title 'Our Pir algorithm - cache 8' with lines 8,\
GNUPLOT_NAMEFILE using 2:($1!=9 ? 1/0 : $3) title 'Our Pir algorithm - cache 9' with lines 9,\
GNUPLOT_NAMEFILE using 2:($1!=10 ? 1/0 : $3) title 'Our Pir algorithm - cache 10' with lines 10,\
GNUPLOT_NAMEFILE using 2:($1!=1 ? 1/0 : $4) title 'Plain index research'with lines 11

#      set terminal postscript {<mode>} {enhanced | noenhanced}
#                              {color | colour | monochrome}
#                              {blacktext | colortext | colourtext}
#                              {solid | dashed} {dashlength | dl <DL>}
#                              {linewidth | lw <LW>}
#                              {<duplexing>}
#                              {"<fontname>"} {<fontsize>}


#     set terminal gif {transparent} {interlace}
#                       {tiny | small | medium | large | giant}
#                       {size <x>,<y>}
#                       {<color0> <color1> <color2> ...}

#     set terminal png
#             {{no}transparent} {{no}interlace}
#             {tiny | small | medium | large | giant}
#             {font <face> {<pointsize>}}
#             {size <x>,<y>} {{no}crop}
#             {{no}enhanced}
#             {<color0> <color1> <color2> ...}
