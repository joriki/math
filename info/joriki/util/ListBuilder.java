package info.joriki.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListBuilder<T> implements Consumer<T> {
    List<T> list = new ArrayList<>();
    
    public void accept(T t) {
        list.add(t);
    }
    
    public List<T> build() {
        return list;
    }
}
