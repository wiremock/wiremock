package testsupport;

import com.github.tomakehurst.wiremock.common.Notifier;

import java.util.Arrays;
import java.util.List;

public class CompositeNotifier implements Notifier {

    private final List<Notifier> notifiers;

    public CompositeNotifier(Notifier... notifiers) {
        this(Arrays.asList(notifiers));
    }

    public CompositeNotifier(List<Notifier> notifiers) {
        this.notifiers = notifiers;
    }

    @Override
    public void info(String message) {
        notifiers.forEach(notifier -> notifier.info(message));
    }

    @Override
    public void error(String message) {
        notifiers.forEach(notifier -> notifier.error(message));
    }

    @Override
    public void error(String message, Throwable t) {
        notifiers.forEach(notifier -> notifier.error(message, t));
    }
}
