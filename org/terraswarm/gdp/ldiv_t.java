package org.terraswarm.gdp;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : /usr/include/stdlib.h:9</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class ldiv_t extends Structure {
	/** quotient */
	public NativeLong quot;
	/** remainder */
	public NativeLong rem;
	public ldiv_t() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("quot", "rem");
	}
	/**
	 * @param quot quotient<br>
	 * @param rem remainder
	 */
	public ldiv_t(NativeLong quot, NativeLong rem) {
		super();
		this.quot = quot;
		this.rem = rem;
	}
	public ldiv_t(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends ldiv_t implements Structure.ByReference {
		
	};
	public static class ByValue extends ldiv_t implements Structure.ByValue {
		
	};
}
