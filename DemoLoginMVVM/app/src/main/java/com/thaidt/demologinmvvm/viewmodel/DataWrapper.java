package com.thaidt.demologinmvvm.viewmodel;

public class DataWrapper<T> {
    public enum State {
        LOADING,
        SUCCESS,
        ERROR
    }

    private State state;
    private T data;
    private String message = "";

    public DataWrapper(State state, T data, String message) {
        this.state = state;
        this.data = data;
        this.message = message;
    }

    public DataWrapper(State state, T data) {
        this.state = state;
        this.data = data;
    }

    public DataWrapper(State state, String message) {
        this.state = state;
        this.message = message;
    }

    public DataWrapper(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
