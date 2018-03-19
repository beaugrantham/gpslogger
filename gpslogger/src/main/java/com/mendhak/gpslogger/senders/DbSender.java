package com.mendhak.gpslogger.senders;

public abstract class DbSender {

    public abstract void upload();

    /**
     * Whether the sender is enabled and ready to be used for manual uploads
     */
    public abstract boolean isAvailable();

    /**
     * Whether the user has enabled this preference for automatic sending
     */
    public abstract boolean hasUserAllowedAutoSending();

    /**
     * Whether this sender is available and allowed to automatically send files.
     * It checks both {@link #isAvailable()} and {@link #hasUserAllowedAutoSending()}
     */
    public boolean isAutoSendAvailable() {
        return hasUserAllowedAutoSending() && isAvailable();
    }

}
