package nl.radiantrealm.library.util.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public record JsonNumber(Number value) implements JsonPrimitive {

    public JsonNumber(Number value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public byte getAsByte() {
        return value.byteValue();
    }

    public short getAsShort() {
        return value.shortValue();
    }

    public int getAsInteger() {
        return value.intValue();
    }

    public long getAsLong() {
        return value.longValue();
    }

    public float getAsFloat() {
        return value.floatValue();
    }

    public double getAsDouble() {
        return value.doubleValue();
    }

    public BigInteger getAsBigInteger() {
        if (value instanceof BigInteger integer) {
            return integer;
        }

        return BigInteger.valueOf(value.longValue());
    }

    public BigDecimal getAsBigDecimal() {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }

        return BigDecimal.valueOf(value.longValue());
    }
}
