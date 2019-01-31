import networkx as nx
import matplotlib.pyplot as plt


G = nx.Graph(layout='spectral_layout')

nodes = ["   Samsung \nS7", "Samsung \nS5 Mini", "Samsung \nS3 Neo"]
edges = [("   Samsung \nS7", "Samsung \nS5 Mini"),  ("Samsung \nS5 Mini", "Samsung \nS3 Neo")]
# nx.drawing.layout("spectral_layout")

for node in nodes:
    G.add_node(node)

for edge in edges:
    G.add_edge(edge[0], edge[1])

nx.draw_spring(G, with_labels=True, font_weight='bold')
l,r = plt.xlim()
plt.xlim(l-2,r+2)
plt.savefig("test_network.pdf", format="pdf")

plt.clf()

G = nx.Graph(layout='spring_layout')

nodes = [1,2,3,4,5,6,8,9,10]

edges=[(1,2), (2,3), (4,5), (5,2), (5,1), (1,6), (6,7), (7,8), (8,9), (9,10), (10,6)]

for node in nodes:
    G.add_node(node)

for edge in edges:
    G.add_edge(edge[0], edge[1])

nx.draw_spring(G, with_labels=True, font_weight='bold')
plt.savefig("network_self_regulating_full.png")


#####
plt.clf()

G = nx.Graph(layout='spectral_layout')

nodes = [1,2,3,4,5,6,8,9,10]

edges=[(1,2), (2,3), (4,5), (5,2), (5,1), (6,7), (7,8), (8,9), (9,10), (10,6)]

for node in nodes:
    G.add_node(node)

for edge in edges:
    G.add_edge(edge[0], edge[1])

nx.draw_spring(G, with_labels=True, font_weight='bold')
plt.savefig("network_self_regulating_broken.png")