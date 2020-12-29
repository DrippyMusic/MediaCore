package me.vitormac.mediacore.data;

import com.google.gson.JsonObject;

public interface Transformable<T> {

    T transform(JsonObject object);

}
