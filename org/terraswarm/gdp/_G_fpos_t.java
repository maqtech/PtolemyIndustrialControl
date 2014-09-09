package org.terraswarm.gdp;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : /usr/include/_G_config.h:3</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class _G_fpos_t extends Structure {
	/** C type : __off_t */
	public NativeLong __pos;
	/** C type : __mbstate_t */
	public __mbstate_t __state;
	public _G_fpos_t() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("__pos", "__state");
	}
	/**
	 * @param __pos C type : __off_t<br>
	 * @param __state C type : __mbstate_t
	 */
	public _G_fpos_t(NativeLong __pos, __mbstate_t __state) {
		super();
		this.__pos = __pos;
		this.__state = __state;
	}
	public _G_fpos_t(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends _G_fpos_t implements Structure.ByReference {
		
	};
	public static class ByValue extends _G_fpos_t implements Structure.ByValue {
		
	};
}
