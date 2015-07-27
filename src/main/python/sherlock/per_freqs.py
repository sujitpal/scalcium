# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt

fin = open("../../../../data/sherlock/PER-graph-verts.tsv", 'rb')
xs = []
ys = []
num_entries = 20
curr_entries = 0
for line in fin:
    cols = line.strip().split("\t")
    xs.append(cols[0])
    ys.append(int(cols[1]))
    if curr_entries >= num_entries:
        break
    curr_entries += 1
fin.close()
plt.barh(range(len(xs[::-1])), ys[::-1], align="center", height=1)
plt.yticks(range(len(xs)), xs[::-1])
plt.xlabel("Occurrence Count")
plt.grid()
#fig = plt.gcf()
#fig.set_size_inches(10, 5)
plt.show()
