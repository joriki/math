package info.joriki.math.algebra;

public interface InvertibleBinaryOperation<T> extends BinaryOperationWithIdentity<T> {
	T inverse (T t);
}
