// $Id$
// The .cpp and .h files in this directory are from salma-hayek, found at
// http://software.jessies.org/terminator/
// salma-hayek is LGPL'd, see the COPYING.txt file.

#ifdef __CYGWIN__
#include <windows.h>
#endif

#include "DirectoryIterator.h"
#include "join.h"
#include "synchronizeWindowsEnvironment.h"

#include <jni.h>

#include <algorithm>
#include <deque>
#include <dlfcn.h>
#include <fstream>
#include <iostream>
#include <sstream>
#include <stdexcept>
#include <string>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <vector>

struct UsageError : std::runtime_error {
  UsageError(const std::string& description)
  : std::runtime_error(description) {
  }
};

typedef std::deque<std::string> NativeArguments;

extern "C" {
  typedef jint JNICALL (*CreateJavaVM)(JavaVM**, void**, void*);
}

struct JvmLocation {
private:
  std::string jvmDirectory;
  
public:
  static std::string readFile(const std::string& path) {
    std::ifstream is(path.c_str());
    if (is.good() == false) {
      throw UsageError("Couldn't open \"" + path + "\".");
    }
    std::ostringstream contents;
    contents << is.rdbuf();
    return contents.str();
  }
  
  static std::string readRegistryFile(const std::string& path) {
    std::string contents = readFile(path);
    // Cygwin's representation of REG_SZ keys seems to include the null terminator.
    if (contents.empty() == false && contents[contents.size() - 1] == '\0') {
      return contents.substr(0, contents.size() - 1);
    }
    return contents;
  }
  
  std::string chooseVersionFromRegistry(const std::string& registryPath) const {
    std::vector<std::string> versions;
    for (DirectoryIterator it(registryPath); it.isValid(); ++ it) {
      std::string version = it->getName();
      if (version.empty() || isdigit(version[0]) == false) {
        // Avoid "CurrentVersion", "BrowserJavaVersion", or anything else Sun might think of.
        // "CurrentVersion" didn't get updated when I installed JDK-1.5.0_06 (or the two prior versions by the look of it)..
        continue;
      }
      if (version < "1.5") {
        continue;
      }
      if (version >= "1.6") {
        // Uncomment the next line to prevent usage of 1.6.
        //continue;
      }
      versions.push_back(version);
    }
    std::sort(versions.begin(), versions.end());
    if (versions.empty()) {
      throw UsageError("No suitable Java found under \"" + registryPath + "\".");
    }
    std::string version = versions.back();
    return version;
  }
  
  std::string findJvmLibraryUsingJreRegistry() const {
    const char* jreRegistryPath = "/proc/registry/HKEY_LOCAL_MACHINE/SOFTWARE/JavaSoft/Java Runtime Environment";
    std::string version = chooseVersionFromRegistry(jreRegistryPath);
    // What should we do if this points to "client" when we want "server"?
    std::string jvmRegistryPath = std::string(jreRegistryPath) + "/" + version + "/RuntimeLib";
    return readRegistryFile(jvmRegistryPath);
  }
  
  std::string findJvmLibraryUsingJdkRegistry() const {
    const char* jdkRegistryPath = "/proc/registry/HKEY_LOCAL_MACHINE/SOFTWARE/JavaSoft/Java Development Kit";
    std::string version = chooseVersionFromRegistry(jdkRegistryPath);
    std::string javaHome = readRegistryFile(std::string(jdkRegistryPath) + "/" + version + "/JavaHome");
    return javaHome + "/jre/bin/client/jvm.dll";
  }
  
  std::string findWin32JvmLibrary() const {
    std::ostringstream os;
    os << "Couldn't find jvm.dll - please install a 1.5 or newer JRE or JDK.";
    os << std::endl;
    os << "Error messages were:";
    os << std::endl;
    try {
      return findJvmLibraryUsingJdkRegistry();
    } catch (const std::exception& ex) {
      os << "  ";
      os << ex.what();
      os << std::endl;
    }
    try {
      return findJvmLibraryUsingJreRegistry();
    } catch (const std::exception& ex) {
      os << "  ";
      os << ex.what();
      os << std::endl;
    }
    throw UsageError(os.str());
  }
  
  std::string findJvmLibraryFilename() const {
#if defined(__CYGWIN__)
    return findWin32JvmLibrary();
#else
    // This only works on Linux if LD_LIBRARY_PATH is already set up to include something like:
    // "$JAVA_HOME/jre/lib/$ARCH/" + jvmDirectory
    // "$JAVA_HOME/jre/lib/$ARCH"
    // "$JAVA_HOME/jre/../lib/$ARCH"
    // Where $ARCH is "i386" rather than `arch`.
    return "libjvm.so";
#endif
  }
  
  void setClientClass() {
    jvmDirectory = "client";
  }
  void setServerClass() {
    jvmDirectory = "server";
  }
  
  JvmLocation() {
    setClientClass();
  }
};

struct JavaInvocation {
private:
  JavaVM* vm;
  JNIEnv* env;
  
private:
  static CreateJavaVM findCreateJavaVM(const char* sharedLibraryFilename) {
    void* sharedLibraryHandle = dlopen(sharedLibraryFilename, RTLD_LAZY);
    if (sharedLibraryHandle == 0) {
      std::ostringstream os;
      os << "dlopen(\"" << sharedLibraryFilename << "\") failed with " << dlerror() << ".";
      throw UsageError(os.str());
    }
    // Work around:
    // warning: ISO C++ forbids casting between pointer-to-function and pointer-to-object
    CreateJavaVM createJavaVM = reinterpret_cast<CreateJavaVM> (reinterpret_cast<long> (dlsym(sharedLibraryHandle, "JNI_CreateJavaVM")));
    if (createJavaVM == 0) {
      std::ostringstream os;
      os << "dlsym(\"" << sharedLibraryFilename << "\", JNI_CreateJavaVM) failed with " << dlerror() << ".";
      throw UsageError(os.str());
    }
    return createJavaVM;
  }
  
  jclass findClass(const std::string& className) {
    jclass javaClass = env->FindClass(className.c_str());
    if (javaClass == 0) {
      if (env->ExceptionOccurred()) {
          std::cout << "Exception\n";
          env->ExceptionDescribe();
          env->ExceptionClear();
      }
      std::cout << "About to call GetStaticMethod\n";
      std::cout.flush();

      //const char * systemClassName = strdup();
      jclass systemClass = env->FindClass("java/lang/System");
      
      //const char * getPropertySignature = strdup("getProperty(Ljava/lang/String;)Ljava/lang/String;)");

      jmethodID systemId = env->GetStaticMethodID(systemClass,
              "getProperty",
              "(Ljava/lang/String;)Ljava/lang/String;");

      if (env->ExceptionOccurred()) {
          std::cout << "Exception\n";
          std::cout.flush();
          env->ExceptionDescribe();
          env->ExceptionClear();
      }

      std::cout << "About to call NewString\n";
      std::cout.flush();

      jstring propertyName = env->NewStringUTF("java.class.path");
      //jstring propertyName = env->NewStringUTF("file.separator");

      if (env->ExceptionOccurred()) {
          std::cout << "Exception\n";
          std::cout.flush();
          env->ExceptionDescribe();
          env->ExceptionClear();
      }

      std::cout << "About to call CallStaticObjectMethod " << systemClass << " " << systemId << " " << propertyName << "\n" ;
      std::cout.flush();

      jstring property = (jstring) env->CallStaticObjectMethod(systemClass, systemId, propertyName);

      if (env->ExceptionOccurred()) {
          std::cout << "Exception\n";
          std::cout.flush();
          env->ExceptionDescribe();
          env->ExceptionClear();
      }

      std::cout << "About to call GetStringChars\n";
      std::cout.flush();

      //const jchar * propertyString = env->GetStringChars((jstring)&property, JNI_FALSE);

      const char * propertyString = env->GetStringUTFChars(property, NULL);
      std::ostringstream os;
      os << "FindClass(\"" << className << "\") failed.\n" << "Try using / separated paths instead of . separated, for example: foo/bar/bif. Classpath: " << propertyString;
      
      throw UsageError(os.str());
    }
    return javaClass;
  }
  
  jmethodID findMainMethod(jclass mainClass) {
    jmethodID method = env->GetStaticMethodID(mainClass, "main", "([Ljava/lang/String;)V");
    if (method == 0) {
      throw UsageError("GetStaticMethodID(\"main\") failed.");
    }
    return method;
  }
  
  jstring makeJavaString(const char* nativeString) {
    jstring javaString = env->NewStringUTF(nativeString);
    if (javaString == 0) {
      std::ostringstream os;
      os << "NewStringUTF(\"" << nativeString << "\") failed.";
      throw UsageError(os.str());
    }
    return javaString;
  }
  
  jobjectArray convertArguments(const NativeArguments& nativeArguments) {
    jclass jstringClass = findClass("java/lang/String");
    jstring defaultArgument = makeJavaString("");
    jobjectArray javaArguments = env->NewObjectArray(nativeArguments.size(), jstringClass, defaultArgument);
    if (javaArguments == 0) {
      std::ostringstream os;
      os << "NewObjectArray(" << nativeArguments.size() << ") failed.";
      throw UsageError(os.str());
    }
    for (size_t index = 0; index != nativeArguments.size(); ++ index) {
      std::string nativeArgument = nativeArguments[index];
      jstring javaArgument = makeJavaString(nativeArgument.c_str());
      env->SetObjectArrayElement(javaArguments, index, javaArgument);
    }
    return javaArguments;
  }
  
public:
  JavaInvocation(const std::string& jvmLibraryFilename, const NativeArguments& jvmArguments) {
    CreateJavaVM createJavaVM = findCreateJavaVM(jvmLibraryFilename.c_str());
    
    typedef std::vector<JavaVMOption> JavaVMOptions; // Required to be contiguous.
    JavaVMOptions javaVMOptions(jvmArguments.size());
    for (size_t ii = 0; ii != jvmArguments.size(); ++ ii) {
      // I'm sure the JVM doesn't actually write to its options.
      javaVMOptions[ii].optionString = const_cast<char*>(jvmArguments[ii].c_str());
    }
    
    JavaVMInitArgs javaVMInitArgs;
    javaVMInitArgs.version = JNI_VERSION_1_2;
    javaVMInitArgs.options = &javaVMOptions[0];
    javaVMInitArgs.nOptions = javaVMOptions.size();
    javaVMInitArgs.ignoreUnrecognized = false;
    
    int result = createJavaVM(&vm, reinterpret_cast<void**>(&env), &javaVMInitArgs);
    if (result < 0) {
      std::ostringstream os;
      os << "JNI_CreateJavaVM(" << javaVMOptions.size() << " options) failed with " << result << ".";
      throw UsageError(os.str());
    }
  }
  
  ~JavaInvocation() {
    // If you attempt to destroy the VM with a pending JNI exception,
    // the VM crashes with an "internal error" and good luck to you finding
    // any reference to it on google.
    if (env->ExceptionOccurred()) {
      env->ExceptionDescribe();
    }
    
    // The non-obvious thing about DestroyJavaVM is that you have to call this
    // in order to wait for all the Java threads to quit - even if you don't
    // care about "leaking" the VM.
    // Deliberately ignore the error code, as the documentation says we must.
    vm->DestroyJavaVM();
  }
  
  void invokeMain(const std::string& className, const NativeArguments& nativeArguments) {
    jclass javaClass = findClass(className);
    jmethodID javaMethod = findMainMethod(javaClass);
    jobjectArray javaArguments = convertArguments(nativeArguments);
    env->CallStaticVoidMethod(javaClass, javaMethod, javaArguments);
  }
};

struct LauncherArgumentParser {
private:
  JvmLocation jvmLocation;
  NativeArguments jvmArguments;
  std::string className;
  NativeArguments mainArguments;
  
private:
  static bool beginsWith(const std::string& st, const std::string& prefix) {
    return st.substr(0, prefix.size()) == prefix;
  }
  
public:
  LauncherArgumentParser(const NativeArguments& launcherArguments) {
    NativeArguments::const_iterator it = launcherArguments.begin();
    NativeArguments::const_iterator end = launcherArguments.end();
    while (it != end && beginsWith(*it, "-")) {
      std::string option = *it;
      if (option == "-client") {
        jvmLocation.setClientClass();
      } else if (option == "-server") {
        jvmLocation.setServerClass();
      } else {
        jvmArguments.push_back(option);
      }
      ++ it;
    }
    if (it == end) {
      throw UsageError("No class specified.");
    }
    className = *it;
    ++ it;
    while (it != end) {
      mainArguments.push_back(*it);
      ++ it;
    }
  }
  
  std::string getJvmLibraryFilename() const {
    return jvmLocation.findJvmLibraryFilename();
  }
  NativeArguments getJvmArguments() const {
    return jvmArguments;
  }
  std::string getClassName() const {
    return className;
  }
  NativeArguments getMainArguments() const {
    return mainArguments;
  }
};

int main(int, char** argv) {
  synchronizeWindowsEnvironment();
  const char* programName = *argv;
  ++ argv;
  NativeArguments launcherArguments;
  while (*argv != 0) {
    launcherArguments.push_back(*argv);
    ++ argv;
  }
  try {
    LauncherArgumentParser parser(launcherArguments);
    JavaInvocation javaInvocation(parser.getJvmLibraryFilename(), parser.getJvmArguments());
    javaInvocation.invokeMain(parser.getClassName(), parser.getMainArguments());
  } catch (const UsageError& usageError) {
    std::ostringstream os;
    os << "Error: " << usageError.what() << std::endl;
    os << std::endl;
    os << "Usage: " << programName << " [options] class [args...]" << std::endl;
    os << "where options are:" << std::endl;
    os << "  -client - use client VM" << std::endl;
    os << "  -server - use server VM" << std::endl;
    os << "  -D<name>=<value> - set a system property" << std::endl;
    os << "  -verbose[:class|gc|jni] - enable verbose output" << std::endl;
    // FIXME: If we know which version of JVM we've selected here, we could say so.
    os << "or JVM 1.5 or newer -X options." << std::endl;
    os << std::endl;
    os << "Command line was:";
    os << std::endl;
    os << programName << " ";
    os << join(" ", launcherArguments);
    os << std::endl;
    std::cerr << os.str();
#ifdef __CYGWIN__
    std::string message = "Please copy this message to the clipboard with Ctrl-C and mail it to software@jessies.org.";
    message += "\n";
    message += "(Windows won't let you select the text but Ctrl-C works anyway.)";
    message += "\n";
    message += "\n";
    message += os.str();
    MessageBox(GetActiveWindow(), message.c_str(), "Launcher", MB_OK);
#endif
    return 1;
  }
}
