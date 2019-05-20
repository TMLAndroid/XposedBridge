package com.android.internal.os;


/**
 * Startup class for the zygote process.
 *
 * Pre-initializes some classes, and then waits for commands on a UNIX domain socket. Based on these
 * commands, forks off child processes that inherit the initial state of the VM.
 *
 * Please see {@link ZygoteArguments} for documentation on the client protocol.
 *
 */
public class ZygoteInit {
	public static void main(String[] argv) {
		throw new UnsupportedOperationException("STUB");
	}
}
