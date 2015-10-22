package com.sig.rs.Blackboard;

import java.util.ArrayList;
import java.util.List;

public class BlackboardProperty<T> {
    private List<BlackboardOnChange<T>> listeners = new ArrayList<BlackboardOnChange<T>>();
    T value;

    public BlackboardProperty() {
    }

    public BlackboardProperty(T defaultValue) {
        value = defaultValue;
    }

    public T get() {
        return value;
    }
    public void set(T newValue) {
        if (value == null || newValue == null || !value.equals(newValue)) {
            T oldValue = value;

            value = newValue;

            for (BlackboardOnChange<T> listener : listeners) {
                listener.onChange(oldValue);
            }
        }
    }

    public void onChange(BlackboardOnChange<T> listener) {
        listeners.add(listener);
    }
}
