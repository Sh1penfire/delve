package main.data;

import arc.Events;
import mindustry.game.EventType;

public class Resettable<T>{
    private T initial;
    private T value;

    public Resettable(T t){
        initial = t;
        value = t;
    }

    public T get(){
        return value;
    }

    public void set(T t){
        value = t;
    }

    public void reset(){
        value = initial;
    }
}
