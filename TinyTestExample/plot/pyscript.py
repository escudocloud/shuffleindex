#!/usr/bin/python2

from string import split, join, strip
def read(filename):
  f = open(filename,"r")
  l= [ split(strip(r,' \t\n\r'),'\t') for r in f.readlines() ]
  for it in l : 
    it[0], it[1] = it[1], it[0]
  l.sort(cmp = lambda x,y : cmp(int(x[0]),int(y[0])))
  f.close()
  for i in xrange(len(l)) : l[i] = join(l[i],' ')
  f1 = open(filename+".dat","w")
  for x in l : 
    f1.write(x+'\n')
  f1.close()

from sys import argv
if __name__ == '__main__' :
  read(argv[1])
  
