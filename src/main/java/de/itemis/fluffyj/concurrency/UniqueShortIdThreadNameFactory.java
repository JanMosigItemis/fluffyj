package de.itemis.fluffyj.concurrency;

import static com.google.common.base.Strings.padStart;
import static java.lang.Long.toHexString;
import static java.util.Objects.requireNonNull;

/**
 * <p>
 * An implementation of {@link ThreadNameFactory} that combines a static name with a running number
 * in order to create "unique" thread names.
 * </p>
 * <p>
 * Resulting thread names will be of form: &lt;prefix&gt;-Thread-&lt;id&gt;.
 * </p>
 * <p>
 * This factory uses {@link Short} to hold the id, i. e. it may generate a maximum of
 * {@value Short#MAX_VALUE} unique ids.
 * </p>
 * <p>
 * An instance of this factory will hold the last id used by this instance, so <b>be aware</b> when
 * declaring instances of this factory as static, as it may run out of ids over time.
 * </p>
 */
public final class UniqueShortIdThreadNameFactory implements ThreadNameFactory {

    private static final String ID_SEPARATOR = "-";
    private static final int CHARACTERS_PER_BYTE = 2;
    private static final int ID_LENGTH_IN_CHARACTERS = Short.BYTES * CHARACTERS_PER_BYTE;

    private final String prefix;
    private short current = 0;

    /**
     * Construct a new instance.
     *
     * @param prefix - Thread names will have this {@link String} set as prefix.
     */
    public UniqueShortIdThreadNameFactory(String prefix) {
        this.prefix = requireNonNull(prefix, "prefix") + ID_SEPARATOR;
    }

    @Override
    public String generate() {
        current = (short) ((current + 1) % Short.MAX_VALUE);
        String id = padStart(toHexString(current).toLowerCase(), ID_LENGTH_IN_CHARACTERS, '0');
        return prefix + id;
    }
}
