#!/bin/bash
dot -Tpdf tree.dot -o tree.pdf
./xdot.py tree.dot
