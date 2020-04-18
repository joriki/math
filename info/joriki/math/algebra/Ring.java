package info.joriki.math.algebra;

public interface Ring<T> {
	InvertibleBinaryOperation<T> addition ();
	BinaryOperationWithIdentity<T> multiplication ();
}
