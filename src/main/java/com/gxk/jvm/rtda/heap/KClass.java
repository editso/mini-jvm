package com.gxk.jvm.rtda.heap;

import com.gxk.jvm.classfile.ConstantPool;
import com.gxk.jvm.classfile.attribute.BootstrapMethods;
import com.gxk.jvm.classloader.ClassLoader;
import com.gxk.jvm.rtda.LocalVars;
import com.gxk.jvm.rtda.Slot;

import java.util.ArrayList;
import java.util.stream.Collectors;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class KClass {

  public final String name;
  public final String superClassName;
  public final List<String> interfaceNames;
  public final List<KMethod> methods;
  public final List<KField> fields;
  public final BootstrapMethods bootstrapMethods;
  public final ConstantPool constantPool;
  public final ClassLoader classLoader;

  private KClass superClass;
  private List<KClass> interfaces;
  private int staticInit = 0;

  public KClass(String name, ClassLoader classLoader) {
    this.name = name;
    this.superClassName = "java/lang/Object";
    this.interfaceNames = new ArrayList<>();
    this.interfaces = new ArrayList<>();
    this.bootstrapMethods = null;
    this.constantPool = null;
    this.classLoader = classLoader;
    this.methods = new ArrayList<>();
    this.fields = new ArrayList<>();
    this.staticInit = 2;
  }

  public KClass(String name,
                String superClassName,
                List<String> interfaceNames,
                List<KMethod> methods,
                List<KField> fields,
                BootstrapMethods bootstrapMethods,
                ConstantPool constantPool,
                ClassLoader classLoader
  ) {
    this.name = name;
    this.superClassName = superClassName;
    this.interfaceNames = interfaceNames;
    this.interfaces = new ArrayList<>();
    this.methods = methods;
    this.fields = fields;
    this.bootstrapMethods = bootstrapMethods;
    this.constantPool = constantPool;
    this.classLoader = classLoader;

    methods.forEach(it -> it.clazz = this);
  }

  public KMethod getMainMethod() {
    for (KMethod method : methods) {
      if (Objects.equals("main", method.name)) {
        return method;
      }
    }
    return null;
  }

  public KMethod getClinitMethod() {
    return getMethod("<clinit>", "()V");
  }

  public KMethod getMethod(String name, String descriptor) {
    for (KMethod method : methods) {
      if (Objects.equals(method.name, name) && Objects.equals(method.descriptor, descriptor)) {
        return method;
      }
    }
    if (this.superClass == null) {
      return null;
    }
    return this.superClass.getMethod(name, descriptor);
  }

  public KMethod getLambdaMethod(String name) {
    for (KMethod method : methods) {
      if (Objects.equals(method.name, name)) {
        return method;
      }
    }
    return null;
  }

  public KField getField(String fieldName, String fieldDescriptor) {
    for (KField field : fields) {
      if (Objects.equals(field.name, fieldName) && Objects.equals(field.descriptor, fieldDescriptor)) {
        return field;
      }
    }
    return null;
  }

  public void setSuperClass(KClass superClass) {
    this.superClass = superClass;
  }

  public KObject newObject() {
    List<KField> newFields = fields.stream().map(this::map).collect(Collectors.toList());
    KObject object = new KObject(newFields, this);
    if (this.superClass != null) {
      object.setSuperObject(this.superClass.newObject());
    }
    return object;
  }

  public KLambdaObject newLambdaObject(List<Object> args) {
    return new KLambdaObject(this, args);
  }

  private KField map(KField source) {
    if (source.isStatic()) {
      return source;
    }
    switch (source.descriptor) {
      case "Z":
      case "C":
      case "B":
      case "S":
      case "I":
      case "F":
        return new KField(source.accessFlags, source.name, source.descriptor, new Slot[] {new Slot(0)});
      case "D":
      case "J":
        return new KField(source.accessFlags, source.name, source.descriptor, new Slot[] {new Slot(0), new Slot(0)});
      default:
        return new KField(source.accessFlags, source.name, source.descriptor, new Slot[] {new Slot((Object) null)});
    }
  }

  public boolean isStaticInit() {
    return this.staticInit > 0;
  }

  public void setStaticInit(int level) {
    this.staticInit = level;
  }

  public KClass getUnStaticInitSuperClass() {
    if (!this.isStaticInit()) {
      return this;
    }
    if (this.superClass == null) {
      return null;
    }
    return this.superClass.getUnStaticInitSuperClass();
  }

  @Override
  public String toString() {
    return "KClass{" +
      "name='" + name + '\'' +
      ", superClassName='" + superClassName + '\'' +
      ", methods=" + methods.size() +
      ", fields=" + fields.size() +
      ", classLoader=" + classLoader.getClass().getName() +
      ", superClass=" + (superClass == null ? "null" : superClass.getName()) +
      ", staticInit=" + staticInit +
      '}';
  }
}
