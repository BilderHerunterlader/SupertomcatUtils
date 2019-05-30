package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.values;

/**
 * Container class for value
 * 
 * @param <T> Value Type
 */
public abstract class OperaValue<T> {
	/**
	 * Value
	 */
	private T value;

	/**
	 * Constructor
	 */
	public OperaValue() {
	}

	/**
	 * Returns the value
	 * 
	 * @return value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the value
	 * 
	 * @param value value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "OperaValue [value=" + value + "]";
	}
}
