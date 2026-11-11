package lk.jiat.smartcity360.securitystream.decorator;

import java.util.Objects;

public abstract class StreamDecorator implements StreamProcessor {
    private final StreamProcessor delegate;

    protected StreamDecorator(StreamProcessor delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    protected byte[] processDelegate(byte[] input) {
        return delegate.process(input);
    }
}
