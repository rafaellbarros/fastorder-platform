package br.com.rafaellbarros.observability.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

@AutoConfiguration
public class ReactorMdcConfiguration {

    @PostConstruct
    public void contextOperatorHook() {
        Hooks.onEachOperator("mdcContext", Operators.lift((scannable, subscriber) ->
            new MdcContextLifter<>(subscriber)
        ));
    }

    static class MdcContextLifter<T> implements reactor.core.CoreSubscriber<T> {

        private final reactor.core.CoreSubscriber<T> coreSubscriber;

        MdcContextLifter(reactor.core.CoreSubscriber<T> coreSubscriber) {
            this.coreSubscriber = coreSubscriber;
        }

        @Override
        public Context currentContext() {
            return coreSubscriber.currentContext();
        }

        @Override
        public void onSubscribe(org.reactivestreams.Subscription s) {
            coreSubscriber.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            copyToMdc();
            coreSubscriber.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            copyToMdc();
            coreSubscriber.onError(t);
        }

        @Override
        public void onComplete() {
            copyToMdc();
            coreSubscriber.onComplete();
        }

        private void copyToMdc() {
            var context = coreSubscriber.currentContext();
            context.stream()
                   .filter(e -> e.getKey().equals("traceId") || e.getKey().equals("spanId"))
                   .forEach(e -> MDC.put(e.getKey().toString(), e.getValue().toString()));
        }
    }
}