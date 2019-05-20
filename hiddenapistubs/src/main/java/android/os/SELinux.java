package android.os;

/**
 * This class provides access to the centralized jni bindings for
 * SELinux interaction.
 */
// SELinux(Security-Enhanced Linux) 是美国国家安全局（NSA）对于强制访问控制的实现，是Linux历史上最杰出的新安全子系统。
public class SELinux {

	/**
	 * Gets the security context of the current process.
	 * @return a String representing the security context of the current process.
	 */
	public static final String getContext() {
		throw new UnsupportedOperationException("STUB");
	}

	/**
	 * Determine whether SELinux is disabled or enabled.
	 * @return a boolean indicating whether SELinux is enabled.
	 */
	public static final boolean isSELinuxEnabled() {
		throw new UnsupportedOperationException("STUB");
	}

	/**
	 * Determine whether SELinux is permissive or enforcing.
	 * @return a boolean indicating whether SELinux is enforcing.
	 */
	public static final boolean isSELinuxEnforced() {
		throw new UnsupportedOperationException("STUB");
	}
}
