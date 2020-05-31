# DistributedSystemsSpanningTreeGenerator
Implementation of "Asynchronous spanning tree" algorithm for the Distributed Systems university course.


Asynchronous spanning tree algorithm creates a spanning tree consisting of nodes in the distributed system.
The nodes communicate through messages. Initially, the configuration is read from the treeConfig.txt file at the 
"resources" folder. At the file there are all of the relevant node parameters for the network, including the IP address 
of each node, port number and all of the nodes connected to the node. The tree can be modeled as a directed graph,
where the line in which the node parameters are placed represents the source node, and the nodes listed at the end of 
that line represent destination nodes.
The following image represents a valid node configuration. 

![alt text](https://github.com/vm0912/DistributedSystemsSpanningTreeGenerator/blob/master/graph.jpg?raw=true)
