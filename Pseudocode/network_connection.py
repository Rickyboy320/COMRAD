import networkx as nx
import matplotlib.pyplot as plt
from threading import Thread

node_array = [{"self" : 0, "neighbours" : [1, 2]}, {"self" : 1, "neighbours" : [0, 3, 2]}, {"self" : 2, "neighbours" : [0, 4, 1]}, {"self" : 3, "neighbours" : [1]}, {"self" : 4, "neighbours" : [2]}]
G = nx.Graph()


def add_to_queue(node_id, task):
    node_array[node_id]["queue"].append(task)
    

def send_message(node, task):
    neighbors = G.neighbors(node["self"])
    order = task["order"]
    next = order.pop(0)
    if (task["destination"] == node["self"]):
        print('message: destination {}'.format( task['destination']))
    elif (next == node["self"]):
        add_to_queue(order[0], {"function" : send_message, "destination" : task["destination"], "order" : order})

def routing_callback(node, task):
    neighbors = G.neighbors(node["self"])
    order = task["order"]
    if (not order):
        add_to_queue(task["payload"][0], {"function" : send_message, "destination" : task["destination"], "order" : task["payload"], "payload" : task["payload"]})
    elif (order[0] == node["self"]):
        next = order.pop(0)
        add_to_queue(next, {"function" : routing_callback, "destination" : task["destination"], "order" : order, "payload" : task["payload"]})

def routing(node, task):
    neighbors = G.neighbors(node["self"])
    order = task["order"]
    order.append(node["self"])
    if (node["self"] == task["destination"]):
        add_to_queue(order[-1], {"function" : routing_callback, "destination" : order[0], "order" : order.reverse(), "payload" : order})
    else:
        for neighbor in neighbors:
            if (order and neighbor not in order):
                add_to_queue(neighbor, {"function" : routing, "destination" : task["destination"], "order" : order})

tasks = {"routing" : routing, "routing_callback" : routing_callback, "message" : send_message}


for node in node_array:
    G.add_node(node["self"])
    node["queue"] = []

for node in node_array:
    for neighbour in node["neighbours"]:
        G.add_edge(node["self"], neighbour)  

def run_thread(node):
    print('queue', node["queue"])
    if (node["queue"]):
        task = node["queue"].pop(0)
        task["function"](node, task)
        # function(node, task)

def run_program(node_array):
    for i in range(20):
        for node in node_array:
            run_thread(node)


add_to_queue(4, {"function" : routing, "destination" : 3, "order" : []})
run_program(node_array)

nx.draw(G, with_labels=True, font_weight='bold')
plt.show()