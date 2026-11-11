package lk.jiat.smartcity360.securitystream.decorator;

@FunctionalInterface
public interface StreamProcessor {
    byte[] process(byte[] input);
}
