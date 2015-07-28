# -*- coding: utf-8 -*-
from __future__ import division
import networkx as nx
import matplotlib.pyplot as plt
import math

G = nx.Graph()
fin = open("../../../../data/sherlock/PER-graph-edges.tsv", 'rb')
for line in fin:
    cols = line.strip().split("\t")
    G.add_edge(cols[0], cols[1], weight=1+int(math.log(10.0*float(cols[2]))))

graph_pos = nx.shell_layout(G)

nx.draw_networkx_nodes(G, graph_pos, node_size=5000, alpha=0.3, 
                       node_color="blue")
nx.draw_networkx_labels(G, graph_pos, font_size=12, font_family="sans-serif")

edge_widths = set(map(lambda x: x[2]["weight"], G.edges(data=True)))
colors = ["magenta", "orange", "green", "blue", "red"]
for edge_width, color in zip(edge_widths, colors):
    edge_subset = [(u, v) for (u, v, d) in G.edges(data=True) 
                                        if d["weight"] == edge_width]
    nx.draw_networkx_edges(G, graph_pos, edgelist=edge_subset, width=edge_width,
                           alpha=0.3, edge_color=color)

fig = plt.gcf()
fig.set_size_inches(15, 15)
plt.xticks([])
plt.yticks([])
plt.show()                
