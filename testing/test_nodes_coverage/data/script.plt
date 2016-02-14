set macro

set terminal postscript enhanced color
set logscale y

my_line_width = "3"
set style line 1 linecolor rgbcolor "#0000AA" linewidth @my_line_width
set style line 2 linecolor rgbcolor "#990000" linewidth @my_line_width
set style line 3 linecolor rgbcolor "#52015b" linewidth @my_line_width
set style line 4 linecolor rgbcolor "#988f03" linewidth @my_line_width
set style line 5 linecolor rgbcolor "#be7400" linewidth @my_line_width
set style line 6 linecolor rgbcolor "#00AA00" linewidth @my_line_width
set style line 7 linecolor rgbcolor "#00b7be" linewidth @my_line_width
set style line 8 linecolor rgbcolor "#808080" linewidth @my_line_width
set style line 9 linecolor rgbcolor "#d26584" linewidth @my_line_width

set title "Node coverage analysis"

set xlabel "Search number" textcolor lt "#000000"
set ylabel "Covered nodes" textcolor lt "#000000"

#########################################################

set title "PIDs coverage analysis (Level 2)"

set output "./Pid_Coverage_4_4_2.ps"
plot "./Pid_Coverage_0.125_4_4_2.log" using 1:3 with lines ti "Profile 0.125", "./Pid_Coverage_0.25_4_4_2.log" using 1:3 with lines ti "Profile 0.25", "./Pid_Coverage_0.5_4_4_2.log" using 1:3 with lines ti "Profile 0.5", "./Pid_Coverage_0.125_4_4_2.log" using 1:4 with lines ti "Total nodes number"

#FREQUENZE DEI PID A CONFRONTO NEI VARI PROFILI PER IL LIVELLO 3

set title "PIDs coverage analysis (Level 3)"

set output "./Pid_Coverage_4_4_3.ps"
plot "./Pid_Coverage_0.125_4_4_3.log" using 1:3 with lines ti "Profile 0.125", "./Pid_Coverage_0.25_4_4_3.log" using 1:3 with lines ti "Profile 0.25", "./Pid_Coverage_0.5_4_4_3.log" using 1:3 with lines ti "Profile 0.5", "./Pid_Coverage_0.125_4_4_3.log" using 1:4 with lines ti "Total nodes number"

#########################################################

set title "VIDs coverage analysis (Level 2)"

set output "./Vid_Coverage_4_4_2.ps"
plot "./Vid_Coverage_0.125_4_4_2.log" using 1:3 with lines ti "Profile 0.125", "./Vid_Coverage_0.25_4_4_2.log" using 1:3 with lines ti "Profile 0.25", "./Vid_Coverage_0.5_4_4_2.log" using 1:3 with lines ti "Profile 0.5", "./Vid_Coverage_0.125_4_4_2.log" using 1:4 with lines ti "Total nodes number"

#FREQUENZE DEI vID A CONFRONTO NEI VARI PROFILI PER IL LIVELLO 3

set title "VIDs coverage analysis (Level 3)"

set output "./Vid_Coverage_4_4_3.ps"
plot "./Vid_Coverage_0.125_4_4_3.log" using 1:3 with lines ti "Profile 0.125", "./Vid_Coverage_0.25_4_4_3.log" using 1:3 with lines ti "Profile 0.25", "./Vid_Coverage_0.5_4_4_3.log" using 1:3 with lines ti "Profile 0.5", "./Vid_Coverage_0.125_4_4_3.log" using 1:4 with lines ti "Total nodes number"

