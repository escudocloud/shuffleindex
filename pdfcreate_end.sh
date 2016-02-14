#!/bin/bash
dot -Tpdf tree_end.dot -o tree_end.pdf
./xdot.py tree_end.dot
