package org.terraswarm.gdp;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : src/gdp/ep/ep_dbg.h:13</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class EP_DBG extends Structure {
        /**
         * debug flag name<br>
         * C type : const char*
         */
        public Pointer name;
        /** current debug level */
        public int level;
        /**
         * description<br>
         * C type : const char*
         */
        public Pointer desc;
        /** flag initialization generation */
        public int gen;
        /**
         * initted flags, in case values change<br>
         * C type : EP_DBG*
         */
        public EP_DBG.ByReference next;
        public EP_DBG() {
                super();
        }
        protected List<? > getFieldOrder() {
                return Arrays.asList("name", "level", "desc", "gen", "next");
        }
        /**
         * @param name debug flag name<br>
         * C type : const char*<br>
         * @param level current debug level<br>
         * @param desc description<br>
         * C type : const char*<br>
         * @param gen flag initialization generation<br>
         * @param next initted flags, in case values change<br>
         * C type : EP_DBG*
         */
        public EP_DBG(Pointer name, int level, Pointer desc, int gen, EP_DBG.ByReference next) {
                super();
                this.name = name;
                this.level = level;
                this.desc = desc;
                this.gen = gen;
                this.next = next;
        }
        public EP_DBG(Pointer peer) {
                super(peer);
        }
        public static class ByReference extends EP_DBG implements Structure.ByReference {
                
        };
        public static class ByValue extends EP_DBG implements Structure.ByValue {
                
        };
}
