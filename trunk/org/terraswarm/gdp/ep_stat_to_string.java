package org.terraswarm.gdp;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : src/gdp/ep/ep_stat.h:9</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class ep_stat_to_string extends Structure {
	/**
	 * status code<br>
	 * C type : EP_STAT
	 */
	public EP_STAT estat;
	/**
	 * string representation<br>
	 * C type : char*
	 */
	public Pointer estr;
	public ep_stat_to_string() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("estat", "estr");
	}
	/**
	 * @param estat status code<br>
	 * C type : EP_STAT<br>
	 * @param estr string representation<br>
	 * C type : char*
	 */
	public ep_stat_to_string(EP_STAT estat, Pointer estr) {
		super();
		this.estat = estat;
		this.estr = estr;
	}
	public ep_stat_to_string(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends ep_stat_to_string implements Structure.ByReference {
		
	};
	public static class ByValue extends ep_stat_to_string implements Structure.ByValue {
		
	};
}
