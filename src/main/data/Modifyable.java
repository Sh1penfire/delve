package main.data;

import arc.struct.Seq;
import arc.util.Log;

public class Modifyable<C, T> {
    private T base;

    public Seq<Modifier<C, T>> modifiers = new Seq<>();

    public Modifyable(T t){
        base = t;
    }

    T current, modified;
    public T getModified(C context){
        current = base;
        modifiers.forEach(m -> {
            modified = m.apply(current, context);

            current = modified;
        });
        return current;
    }

    public void setBase(T t){
        base = t;
    }

    public T getBase(){
        return base;
    }

    public void reset(){
        modifiers.clear();
    }

    public void addModifier(Modifier<C, T> mod){
        modifiers.add(mod);
        modifiers.sort(Modifier::priority);
    }

    public abstract static class Modifier<C, T>{
        public abstract int priority();
        public abstract T apply(T in, C context);
    }
}
