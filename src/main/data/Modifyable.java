package main.data;

import arc.struct.Seq;

public class Modifyable<C, T> {
    private T value;

    public Seq<Modifier<C, T>> modifiers = new Seq<>();

    public Modifyable(T t){
        value = t;
    }

    T current;
    public T getModified(C context){
        current = value;
        modifiers.forEach(m -> {
            current = m.apply(current, context);
        });
        return value;
    }

    public void setBase(T t){
        value = t;
    }

    public T getBase(){
        return value;
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
