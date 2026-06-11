package com.campusevent.ui;

import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

final class JavaFxTestSupport {
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private JavaFxTestSupport() {
    }

    static <T> T runOnFxThread(Callable<T> action) {
        startPlatform();

        if (Platform.isFxApplicationThread()) {
            try {
                return action.call();
            } catch (Exception exception) {
                throw new IllegalStateException("JavaFX UI test action failed", exception);
            }
        }

        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(action.call());
            } catch (Throwable throwable) {
                result.completeExceptionally(throwable);
            }
        });

        try {
            return result.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for JavaFX UI test", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("JavaFX UI test failed", exception);
        }
    }

    private static void startPlatform() {
        if (STARTED.get()) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException exception) {
            latch.countDown();
        }

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while starting JavaFX platform");
            }
            STARTED.set(true);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting JavaFX platform", exception);
        }
    }
}
