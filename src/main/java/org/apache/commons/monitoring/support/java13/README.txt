runtime classes required by backport version to run on Java 1.3 / 1.4

Commons-monitoring uses Java 5 for :
 * compile-time constructs (autoboxing, for loops, generics) that can be safely retrotranslated
 * java.util.concurrent package, available on java 1.3/1.4 as backport-util-concurrent

Retrotranslor is used to translate Java5 compiled code to java 1.3 class file format. Some java5 methods
introduced during java5 compilation needs to get backported. This package contains such code. It is not
intended to backport all Java5 runtime to java 1.3, only to provide code required for commons-monitoring
to run on java 1.3 :

 * primitive autoboxing gets compiled to <Wrapper>.valueOf( primitive )
 * ...