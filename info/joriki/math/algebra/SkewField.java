package info.joriki.math.algebra;

public interface SkewField<T> extends Ring<T> {
	InvertibleBinaryOperation<T> multiplication ();
}
