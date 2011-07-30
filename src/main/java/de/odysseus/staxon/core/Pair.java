package de.odysseus.staxon.core;

/**
 * Object pair.
 * @param <F> first type
 * @param <S> second type
 */
class Pair<F, S> {
	private final F first;
	private final S second;
	
	/**
	 * Create a new pair instance.
	 * @param first first object
	 * @param second second object
	 */
	Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
	
	/**
	 * @return first object
	 */
	F getFirst() {
		return first;
	}
	
	/**
	 * @return second object
	 */
	S getSecond() {
		return second;
	}
	
	@Override
	public int hashCode() {
		return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "Pair(" + first + "," + second + ")";
	}
}
