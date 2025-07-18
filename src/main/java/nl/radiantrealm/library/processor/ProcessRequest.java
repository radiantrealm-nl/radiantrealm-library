package nl.radiantrealm.library.processor;

public record ProcessRequest<T>(int processID, T data) {}
