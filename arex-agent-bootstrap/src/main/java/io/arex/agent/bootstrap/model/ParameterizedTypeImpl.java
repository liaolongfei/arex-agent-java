package io.arex.agent.bootstrap.model;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;

/**
 * ref: <a
 * href="https://github.com/openjdk/jdk17/blob/master/src/java.base/share/classes/sun/reflect/generics/reflectiveObjects/ParameterizedTypeImpl.java">
 * sun/reflect/generics/reflectiveObjects/ParameterizedTypeImpl.java
 * </a>
 */
public class ParameterizedTypeImpl implements ParameterizedType {
    private final Type[] actualTypeArguments;
    private final Class<?>  rawType;
    private final Type   ownerType;

    private ParameterizedTypeImpl(Class<?> rawType,
        Type[] actualTypeArguments,
        Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType             = rawType;
        this.ownerType = (ownerType != null) ? ownerType : rawType.getDeclaringClass();
        validateConstructorArguments();
    }

    private void validateConstructorArguments() {
        TypeVariable<?>[] formals = rawType.getTypeParameters();
        // check correct arity of actual type args
        if (formals.length != actualTypeArguments.length){
            throw new MalformedParameterizedTypeException();
        }
        for (int i = 0; i < actualTypeArguments.length; i++) {
            // check actuals against formals' bounds
        }
    }

    /**
     * Static factory. Given a (generic) class, actual type arguments
     * and an owner type, creates a parameterized type.
     * This class can be instantiated with a a raw type that does not
     * represent a generic type, provided the list of actual type
     * arguments is empty.
     * If the ownerType argument is null, the declaring class of the
     * raw type is used as the owner type.
     * <p> This method throws a MalformedParameterizedTypeException
     * under the following circumstances:
     * If the number of actual type arguments (i.e., the size of the
     * array <tt>typeArgs</tt>) does not correspond to the number of
     * formal type arguments.
     * If any of the actual type arguments is not an instance of the
     * bounds on the corresponding formal.
     * @param rawType the Class representing the generic type declaration being
     * instantiated
     * @param actualTypeArguments - a (possibly empty) array of types
     * representing the actual type arguments to the parameterized type
     * @param ownerType - the enclosing type, if known.
     * @return An instance of <tt>ParameterizedType</tt>
     * @throws MalformedParameterizedTypeException - if the instantiation
     * is invalid
     */
    public static ParameterizedTypeImpl make(Class<?> rawType,
        Type[] actualTypeArguments,
        Type ownerType) {
        return new ParameterizedTypeImpl(rawType, actualTypeArguments,
            ownerType);
    }


    /**
     * Returns an array of <tt>Type</tt> objects representing the actual type
     * arguments to this type.
     *
     * <p>Note that in some cases, the returned array be empty. This can occur
     * if this type represents a non-parameterized type nested within
     * a parameterized type.
     *
     * @return an array of <tt>Type</tt> objects representing the actual type
     *     arguments to this type
     * @throws <tt>TypeNotPresentException</tt> if any of the
     *     actual type arguments refers to a non-existent type declaration
     * @throws <tt>MalformedParameterizedTypeException</tt> if any of the
     *     actual type parameters refer to a parameterized type that cannot
     *     be instantiated for any reason
     * @since 1.5
     */
    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    /**
     * Returns the <tt>Type</tt> object representing the class or interface
     * that declared this type.
     *
     * @return the <tt>Type</tt> object representing the class or interface
     *     that declared this type
     */
    public Class<?> getRawType() {
        return rawType;
    }


    /**
     * Returns a <tt>Type</tt> object representing the type that this type
     * is a member of.  For example, if this type is <tt>O<T>.I<S></tt>,
     * return a representation of <tt>O<T></tt>.
     *
     * <p>If this type is a top-level type, <tt>null</tt> is returned.
     *
     * @return a <tt>Type</tt> object representing the type that
     *     this type is a member of. If this type is a top-level type,
     *     <tt>null</tt> is returned
     * @throws <tt>TypeNotPresentException</tt> if the owner type
     *     refers to a non-existent type declaration
     * @throws <tt>MalformedParameterizedTypeException</tt> if the owner type
     *     refers to a parameterized type that cannot be instantiated
     *     for any reason
     *
     */
    public Type getOwnerType() {
        return ownerType;
    }

    /*
     * From the JavaDoc for java.lang.reflect.ParameterizedType
     * "Instances of classes that implement this interface must
     * implement an equals() method that equates any two instances
     * that share the same generic type declaration and have equal
     * type parameters."
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ParameterizedType) {
            // Check that information is equivalent
            ParameterizedType that = (ParameterizedType) o;

            if (this == that)
                return true;

            Type thatOwner   = that.getOwnerType();
            Type thatRawType = that.getRawType();

            if (false) { // Debugging
                boolean ownerEquality = (ownerType == null ?
                    thatOwner == null :
                    ownerType.equals(thatOwner));
                boolean rawEquality = (rawType == null ?
                    thatRawType == null :
                    rawType.equals(thatRawType));

                boolean typeArgEquality = Arrays.equals(actualTypeArguments, // avoid clone
                    that.getActualTypeArguments());
                for (Type t : actualTypeArguments) {
                    System.out.printf("\t\t%s%s%n", t, t.getClass());
                }

                System.out.printf("\towner %s\traw %s\ttypeArg %s%n",
                    ownerEquality, rawEquality, typeArgEquality);
                return ownerEquality && rawEquality && typeArgEquality;
            }

            return
                Objects.equals(ownerType, thatOwner) &&
                    Objects.equals(rawType, thatRawType) &&
                    Arrays.equals(actualTypeArguments, // avoid clone
                        that.getActualTypeArguments());
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return
            Arrays.hashCode(actualTypeArguments) ^
                Objects.hashCode(ownerType) ^
                Objects.hashCode(rawType);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (ownerType != null) {
            if (ownerType instanceof Class)
                sb.append(((Class)ownerType).getName());
            else
                sb.append(ownerType.toString());

            sb.append("$");

            if (ownerType instanceof ParameterizedTypeImpl) {
                // Find simple name of nested type by removing the
                // shared prefix with owner.
                sb.append(rawType.getName().replace( ((ParameterizedTypeImpl)ownerType).rawType.getName() + "$",
                    ""));
            } else
                sb.append(rawType.getSimpleName());
        } else
            sb.append(rawType.getName());

        if (actualTypeArguments != null &&
            actualTypeArguments.length > 0) {
            sb.append("<");
            boolean first = true;
            for(Type t: actualTypeArguments) {
                if (!first)
                    sb.append(", ");
                sb.append(t.getTypeName());
                first = false;
            }
            sb.append(">");
        }

        return sb.toString();
    }
}
