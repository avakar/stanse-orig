/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.stanse.automatonchecker;

/**
 *
 * @author xstastn
 */
public class Pair<T, S> {
    private T first;
    private S second;

    /**
     * Private -- use getInstance instead
     * @param first First item
     * @param second Second item
     */
    public Pair(T first, S second) {
        this.first = first;
        this.second = second;
    }
    
    public T getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<T, S> other = (Pair<T, S>) obj;
        if (this.first != other.first && (this.first == null || !this.first.equals(other.first))) {
            return false;
        }
        if (this.second != other.second && (this.second == null || !this.second.equals(other.second))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.first != null ? this.first.hashCode() : 0);
        hash = 89 * hash + (this.second != null ? this.second.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "First: "+first+" Second: "+second;
    }
    
    

}
