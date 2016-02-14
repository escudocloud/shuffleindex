#!/bin/bash
dot -Tpdf tree_1.dot -o tree_1.pdf
./xdot.py tree_1.dot
