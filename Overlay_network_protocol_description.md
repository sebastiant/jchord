## A brief description of the protocol

###How we cope with churn and nodes not known to be disconnected:
All forwards are assured to be to connected nodes. That is, a peer A geting information of peer C about peer B, can rely
    on that C is connected to B. If B suddenly disconnects in between these moments, A is responsible to get in touch with
    C again to get a new peer's ip/port. As predecessors periodically inform their successors of their existence and
    requests their predecessor's information, disconnected predecessors are replaced-, and newly joined predecessors
    are included in the ring in finite time.

###Messaging protocol:
PING: Used as keep-alive, a ping is sent from every peer to all of its connected peers in a constant interval.
    Each peer waits for twice that interval before disconnecting from another (probably dead) peer.
JOIN#port#overlaySize: Used to request to join a network. A JOIN message is the first message sent from a new peer. It
    contains the sending peers listening port along with the peers overlay size. If the overlay size matches with the
    peer receiving the message the peer is allowed to join. A JOIN is replied with a WELCOME, containing information of
    who to add as successor.
WELCOME#ip#port: Grants a requesting peer access to join and supplies the new peer with an ip address and port to its
    assigned successor.
SUCC#port: Sent by a peer to inform the receiving peer that it is the sending peers successor and it would like to be the
    receiving peers predecessor. A SUCC is responded by a PRED-message informing who is the current predecessor of the
    sending peer.
PREDREQUEST: Sent by a peer to receive information of who is the receiving peers predecessor. A PREDREQUEST is responded by
    a PRED, containing just that.
PRED#ip#port: A response to a SUCC or PREDREQUEST, informing the receiving peer who is the sending peers predecessor.
 

###Example scenario:
*A connects to B.

*A asks B to join its network by supplying its listening port and overlay size (JOIN#port#overlaySize)

If overlay size matches:
*B writes back contact information to A's assigned successor C: WELCOME#ip#port

*A to assumed successor: SUCC#port, where port equals A's listening port.

The successor checks wether or not its current predecessor should be replaced by A or not and acts accordingly. The SUCC is
    responded witha PRED#ip#port containing ip and port to C's predecessor. If A is C's predecessor all is fine, else
    A sends a new SUCC to C's predecessor.

*Periodically every peer requests its successors predecessor to verify that it infact is still the successor's predecessor
    using PREDREQUEST. If that is not the case, another node has joined and has an id in between the two. That very node
    should be used as the new successor for the not-updated node in such a case.
