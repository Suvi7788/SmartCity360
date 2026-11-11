package lk.jiat.smartcity360.securitystream.decorator;

public final class RawMeterStream implements StreamProcessor {
    @Override
    public byte[] process(byte[] input) {
        return input.clone();
    }
}
