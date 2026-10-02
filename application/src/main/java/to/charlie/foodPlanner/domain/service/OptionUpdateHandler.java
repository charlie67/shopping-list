package to.charlie.foodPlanner.domain.service;

import to.charlie.foodPlanner.domain.model.internal.options.Option;

/**
 * Work that follows an option being updated.
 *
 * <p>An option is a setting other parts of the app read, and changing one can mean reshaping what
 * they hold - the household size changes how many meals a pot makes, for instance. That work belongs
 * to whichever feature owns it, not to {@link OptionService}, which should know how to store a
 * setting and nothing about what any particular setting means.
 *
 * <p>So a feature declares its interest by implementing this as a bean: Spring collects them, and
 * {@code OptionService} calls the ones whose {@link #option()} matches, with no branch of its own.
 * An option with no handler is simply stored.
 *
 * <p>Handlers run <em>after</em> the new value is saved, and are expected to be tolerant: the value
 * is free text that another client may have written, so a handler that cannot read it should do
 * nothing rather than fail the update that stored it.
 */
public interface OptionUpdateHandler {

    /** The option this handler reacts to. */
    Option option();

    /**
     * Called once the new value has been stored.
     *
     * @param value the value as stored, which is not guaranteed to be meaningful for this option.
     */
    void onOptionUpdated(String value);
}
