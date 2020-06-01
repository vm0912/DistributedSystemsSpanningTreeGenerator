# DistributedSystemsSpanningTreeGenerator
Implementation of "Asynchronous spanning tree" algorithm for the Distributed Systems university course.


Asynchronous spanning tree algorithm creates a spanning tree consisting of nodes in the distributed system.
The nodes communicate through messages. Initially, the configuration is read from the treeConfig.txt file at the 
"resources" folder. At the file there are all of the relevant node parameters for the network, including the IP address 
of each node, port number and all of the nodes connected to the node. The tree can be modeled as a directed graph,
where the line in which the node parameters are placed represents the source node, and the nodes listed at the end of 
that line represent destination nodes.

The nodes are connected via TCP as the graph (configuration file) suggests. 
When all of the nodes/processes from the graph list begin execution, the root node needs to explicitly send a "create" message to
start the spanning tree formation.
The root node then sends its neighbour nodes (children) a message of type "search".
After the child nodes acquire the "search" message, they mark the sender as their parent and send the "search" message to their 
descendants. 
The descendants can reject the sender as their parent if they already have a parent.
The process iterates until the node which is initially pronounced as the tree root gets the "search" message.
Considering the asynchronous property of the tree (each node starts executing from a different process, with its own ip address
and port number), the order of messages is not deterministic. 
After the process of sending "search" messages is finished, the tree is officially created.
From that point, the root node can send a message to communicate with other nodes which traverses the created tree.

To start the nodes simultaneously, the script named "startup.bat" is created.

The following image represents a valid node configuration and the algorithm itself. 

![alt text](https://github.com/vm0912/DistributedSystemsSpanningTreeGenerator/blob/master/graph.jpg?raw=true)
