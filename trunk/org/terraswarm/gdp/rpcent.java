package org.terraswarm.gdp;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : /usr/include/netdb.h:48</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class rpcent extends Structure {
	/**
	 * name of server for this rpc program<br>
	 * C type : char*
	 */
	public Pointer r_name;
	/**
	 * alias list<br>
	 * C type : char**
	 */
	public PointerByReference r_aliases;
	/** rpc program number */
	public int r_number;
	public rpcent() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("r_name", "r_aliases", "r_number");
	}
	/**
	 * @param r_name name of server for this rpc program<br>
	 * C type : char*<br>
	 * @param r_aliases alias list<br>
	 * C type : char**<br>
	 * @param r_number rpc program number
	 */
	public rpcent(Pointer r_name, PointerByReference r_aliases, int r_number) {
		super();
		this.r_name = r_name;
		this.r_aliases = r_aliases;
		this.r_number = r_number;
	}
	public rpcent(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends rpcent implements Structure.ByReference {
		
	};
	public static class ByValue extends rpcent implements Structure.ByValue {
		
	};
}
