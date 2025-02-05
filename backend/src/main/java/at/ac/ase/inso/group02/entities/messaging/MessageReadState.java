package at.ac.ase.inso.group02.entities.messaging;

/**
 * represents the state of a message,
 * i.e. the "ticks" if someone read your message
 */
public enum MessageReadState {
    UNSEEN, // message has not been seen in any way
    NOTIFIED, // user has been notified of the message but did not see it
    SEEN // user has seen the message
}
