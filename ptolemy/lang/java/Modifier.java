package ptolemy.lang.java;

public class Modifier {

  public static final int NO_MOD           = 0;

  public static final int PUBLIC_MOD       = 0x1;
  public static final int PROTECTED_MOD    = 0x2;
  public static final int PRIVATE_MOD      = 0x4;
  public static final int ABSTRACT_MOD     = 0x8;
  public static final int FINAL_MOD        = 0x10;
  public static final int NATIVE_MOD       = 0x20;
  public static final int SYNCHRONIZED_MOD = 0x40;
  public static final int TRANSIENT_MOD    = 0x80;
  public static final int VOLATILE_MOD     = 0x100;
  public static final int STATIC_MOD       = 0x200;

  public static final String toString(final int modifier) {
    StringBuffer modString = new StringBuffer();

    if (modifier == NO_MOD) return "";

    if ((modifier & PUBLIC_MOD) != 0)
       modString.append("public ");

    if ((modifier & PROTECTED_MOD) != 0)
       modString.append("protected ");

    if ((modifier & PRIVATE_MOD) != 0)
       modString.append("private ");

    if ((modifier & ABSTRACT_MOD) != 0)
       modString.append("abstract ");

    if ((modifier & FINAL_MOD) != 0)
       modString.append("final ");

    if ((modifier & NATIVE_MOD) != 0)
       modString.append("native ");

    if ((modifier & SYNCHRONIZED_MOD) != 0)
       modString.append("synchronized ");

    if ((modifier & TRANSIENT_MOD) != 0)
       modString.append("transient ");

    if ((modifier & VOLATILE_MOD) != 0)
       modString.append("volatile ");

    if ((modifier & STATIC_MOD) != 0)
       modString.append("static ");

    return modString.toString();
  }

  public static final void checkClassModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD |
          ABSTRACT_MOD)) != 0) {
       throw new RuntimeException("Illegal class modifier");
    }
  }

  public static final void checkConstructorModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD)) != 0) {
       throw new RuntimeException("Illegal constructor modifier");
    }
  }

  public static final void checkConstantFieldModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | STATIC_MOD | FINAL_MOD)) != 0) {
       throw new RuntimeException("Illegal constant field modifier");
    }
  }

  public static final void checkFieldModifiers(int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | STATIC_MOD |
          FINAL_MOD | TRANSIENT_MOD | VOLATILE_MOD)) != 0) {
       throw new RuntimeException("Illegal field modifier");
    }
  }

  public static final void checkMethodModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | STATIC_MOD | FINAL_MOD |
          ABSTRACT_MOD | NATIVE_MOD | SYNCHRONIZED_MOD)) != 0) {
       throw new RuntimeException("Illegal method modifier");
    }
  }

  public static final void checkMethodSignatureModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | ABSTRACT_MOD)) != 0) {
       throw new RuntimeException("Illegal method signature modifier");
    }
  }
}
